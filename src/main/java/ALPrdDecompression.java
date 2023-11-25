import java.util.Arrays;

public class ALPrdDecompression {
    private short[] leftEncoded;
    private long[] rightEncoded;
    private short[] leftPartsDict;
    private int valuesCount;
    private short exceptionsCount;
    private short[] exceptions;
    private short[] exceptionsPositions;
    private byte rightBitWidth;

    public ALPrdDecompression(){}

    public ALPrdDecompression(short[] leftEncoded, long[] rightEncoded, short[] leftPartsDict, int valuesCount, short exceptionsCount, short[] exceptions, short[] exceptionsPositions, byte rightBitWidth){
        // 仅供测试使用
        this.leftEncoded = leftEncoded;
        this.rightEncoded = rightEncoded;
        this.leftPartsDict = leftPartsDict;
        this.valuesCount = valuesCount;
        this.exceptionsCount = exceptionsCount;
        this.exceptions = exceptions;
        this.exceptionsPositions = exceptionsPositions;
        this.rightBitWidth = rightBitWidth;
    }

    public void deserialize(){
    /*
        TODO: read
            nValues             向量长度        int                                             -> valuesCount
            rightBw             右值位宽        byte                                            -> rightBitWidth
            leftParts           左值部分        bits<ALPrdConstants.DICTIONARY_BW>[nValues]     -> leftEncoded
            rightParts          右值部分        bits<rightBw>[nValues]                          -> rightEncoded
            leftPartsDict       左值字典        bits<leftBw>[ALPrdConstants.DICTIONARY_SIZE]    -> leftPartsDict
            exceptionsCount     异常值数量      short                                           -> exceptionsCount
            exceptions          异常值原值      bits<leftBw>[exceptionsCount]                   -> exceptions
            exceptionsPositions 异常值位置      short[exceptionsCount]                          -> exceptionsPositions
         */
    }
    public double[] decompress() {
        deserialize();

        long[] outputLong = new long[valuesCount];
        double[] output = new double[valuesCount];

        // Decoding 拼接
        for (int i = 0; i < valuesCount; i++) {
            short left = leftPartsDict[leftEncoded[i]];
            long right = rightEncoded[i];
            outputLong[i] = ((long) left << rightBitWidth) | right;
        }

        // Exceptions Patching (exceptions only occur in left parts) 处理异常值【字典外的值】
        for (int i = 0; i < exceptionsCount; i++) {
            long right = rightEncoded[exceptionsPositions[i]];
            short left = exceptions[i];
            outputLong[exceptionsPositions[i]] =  (((long) left << rightBitWidth) | right);
        }

        for(int i = 0; i < valuesCount; i++) {
            output[i] = Double.longBitsToDouble(outputLong[i]);
        }
        return output;
    }
}