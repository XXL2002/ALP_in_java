import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ALPrdCompression {
    static ALPrdCompressionState state = new ALPrdCompressionState();
    static final byte EXACT_TYPE_BITSIZE = Double.SIZE;

    public static double estimateCompressionSize(byte rightBw, byte leftBw, short exceptionsCount, long sampleCount) {
        double exceptionsSize = exceptionsCount * ((ALPrdConstants.EXCEPTION_POSITION_SIZE + ALPrdConstants.EXCEPTION_SIZE) * 8);
        double estimatedSize = rightBw + leftBw + (exceptionsSize / sampleCount);
        return estimatedSize;
    }

    public static double buildLeftPartsDictionary(Vector<Long> values, byte rightBw, byte leftBw,
                                                  boolean persistDict, ALPrdCompressionState state) {
        Map<Long, Integer> leftPartsHash = new HashMap<>();
        Vector<Map.Entry<Integer, Long>> leftPartsSortedRepetitions = new Vector<>();   // <出现次数，左值>

        // Building a hash for all the left parts and how many times they appear
        for (Long value : values) {
            long leftTmp = value >>> rightBw;
            leftPartsHash.put(leftTmp, leftPartsHash.getOrDefault(leftTmp, 0) + 1);
        }

        // We build a list from the hash to be able to sort it by repetition count
        for (Map.Entry<Long, Integer> entry : leftPartsHash.entrySet()) {
            leftPartsSortedRepetitions.add(new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()));
        }
        leftPartsSortedRepetitions.sort((a, b) -> Integer.compare(b.getKey(), a.getKey())); // 递减排序

        // Exceptions are left parts which do not fit in the fixed dictionary size
        int exceptionsCount = 0;
        for (int i = ALPrdConstants.DICTIONARY_SIZE; i < leftPartsSortedRepetitions.size(); i++) { // 超过字典容量的部分记为异常值
            exceptionsCount += leftPartsSortedRepetitions.get(i).getKey();
        }

        if (persistDict) {
            int dictIdx = 0;
            int dictSize = Math.min(ALPrdConstants.DICTIONARY_SIZE, leftPartsSortedRepetitions.size());
            for (; dictIdx < dictSize; dictIdx++) {
                //! The dict keys are mapped to the left part themselves
                state.leftPartsDict[dictIdx] = leftPartsSortedRepetitions.get(dictIdx).getValue().shortValue();
                state.leftPartsDictMap.put(state.leftPartsDict[dictIdx], (short) dictIdx);
            }
            //! Parallelly we store a map of the dictionary to quickly resolve exceptions during encoding
            for (int i = dictIdx; i < leftPartsSortedRepetitions.size(); i++) {
                state.leftPartsDictMap.put(leftPartsSortedRepetitions.get(i).getValue().shortValue(), (short) i);
            }
            state.leftBw = leftBw;
            state.rightBw = rightBw;
            state.exceptionsCount = (short) exceptionsCount;

//            assert state.leftBw > 0 && state.leftBw <= AlpRDConstants.CUTTING_LIMIT && state.rightBw > 0;
        }

        double estimatedSize = estimateCompressionSize(rightBw, ALPrdConstants.DICTIONARY_BW, (short) exceptionsCount, values.size());
        return estimatedSize;
    }

    public static double findBestDictionary(Vector<Long> values, ALPrdCompressionState state) {
        int lBw = ALPrdConstants.DICTIONARY_BW;
        int rBw = EXACT_TYPE_BITSIZE;
        double bestDictSize = Integer.MAX_VALUE;

        //! Finding the best position to CUT the values
        for (int i = 1; i <= ALPrdConstants.CUTTING_LIMIT; i++) {
            byte candidateLBw = (byte) i;
            byte candidateRBw = (byte) (EXACT_TYPE_BITSIZE - i);
            double estimatedSize = buildLeftPartsDictionary(values, candidateRBw, candidateLBw, false, state);
            if (estimatedSize <= bestDictSize) {
                lBw = candidateLBw;
                rBw = candidateRBw;
                bestDictSize = estimatedSize;
            }
        }

        double bestEstimatedSize = buildLeftPartsDictionary(values, (byte) rBw, (byte) lBw, true, state);
        return bestEstimatedSize;
    }

    static void compress(Vector<Long> in, int nValues, ALPrdCompressionState state) {
        long[] rightParts = new long[ALPrdConstants.ALP_VECTOR_SIZE];
        short[] leftParts = new short[ALPrdConstants.ALP_VECTOR_SIZE];

        // Cutting the floating point values
        for (int i = 0; i < nValues; i++) {
            Long tmp = in.get(i);
            rightParts[i] = tmp & ((1L << state.rightBw) - 1);
            leftParts[i] = (short) (tmp >>> state.rightBw);
        }

        // Dictionary encoding for left parts
        short exceptionsCount = 0;
        for (int i = 0; i < nValues; i++) {
            short dictionaryIndex;
            short dictionaryKey = leftParts[i];
            if (!state.leftPartsDictMap.containsKey(dictionaryKey)) {
                // If not found in the dictionary, store the smallest non-key index as an exception (the dict size)
                dictionaryIndex = ALPrdConstants.DICTIONARY_SIZE;
            } else {
                dictionaryIndex = state.leftPartsDictMap.get(dictionaryKey);
            }
            leftParts[i] = dictionaryIndex;

            // Left parts not found in the dictionary are stored as exceptions
            if (dictionaryIndex >= ALPrdConstants.DICTIONARY_SIZE) {
                leftParts[i] = 0;   // 用0替换
                state.exceptions[exceptionsCount] = dictionaryKey;
                state.exceptionsPositions[exceptionsCount] = (short) i;
                exceptionsCount++;
            }
        }

        /*
        TODO: bit pack
            useALP=0            不使用ALP压缩   bit
            nValues             向量长度        int
            rightBw             右值位宽        byte
            leftParts           左值部分        bits<ALPrdConstants.DICTIONARY_BW>[nValues]
            rightParts          右值部分        bits<rightBw>[nValues]
            leftPartsDict       左值字典        bits<leftBw>[ALPrdConstants.DICTIONARY_SIZE]
            exceptionsCount     异常值数量      short
            exceptions          异常值原值      bits<leftBw>[exceptionsCount]
            exceptionsPositions 异常值位置      short[exceptionsCount]
         */

        // 以下为模拟调用ALPrdDecompression,仅供测试使用
        ALPrdDecompression ALPrdDe = new ALPrdDecompression(leftParts,rightParts, state.leftPartsDict,nValues, state.exceptionsCount, state.exceptions, state.exceptionsPositions, state.rightBw);
        double[] out = ALPrdDe.decompress();

        String csvFile = "D:\\Code\\ALP\\src\\main\\java\\RDout.csv"; // 输出文件名

        try (FileWriter writer = new FileWriter(csvFile,true)) {
            for (double value : out) {
                writer.append(String.valueOf(value)).append("\n"); // 写入每个值并在行尾添加换行符
            }
            System.out.println("CSV file was written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ALPrd 算法入口
     * @param row 单行数据
     */
    public void entry(Vector<Double> row){
        Vector<Long> rowLong = new Vector<>();
        for (double db:row){
            rowLong.add(Double.doubleToLongBits(db));
        }
        findBestDictionary(rowLong,state);
        compress(rowLong,rowLong.size(),state);
    }
}
