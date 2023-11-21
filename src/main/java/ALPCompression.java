import java.util.*;

public class ALPCompression {
    static ALPCompressionState state = new ALPCompressionState();
    static final double MAGIC_NUMBER = Math.pow(2,51) + Math.pow(2,52); // 对应文章中的sweet值，用于消除小数部分
    static final byte MAX_EXPONENT = 18;
    static final byte EXACT_TYPE_BITSIZE = Double.SIZE;
//    private static final short EXCEPTION_POSITION_SIZE = Short.SIZE;    // ALP用16位整数记录异常值的下标
    private static final double[] EXP_ARR = {
            1.0,
            10.0,
            100.0,
            1000.0,
            10000.0,
            100000.0,
            1000000.0,
            10000000.0,
            100000000.0,
            1000000000.0,
            10000000000.0,
            100000000000.0,
            1000000000000.0,
            10000000000000.0,
            100000000000000.0,
            1000000000000000.0,
            10000000000000000.0,
            100000000000000000.0,
            1000000000000000000.0,
            10000000000000000000.0,
            100000000000000000000.0,
            1000000000000000000000.0,
            10000000000000000000000.0,
            100000000000000000000000.0
    };
    private static final double[] FRAC_ARR = {
            1.0,
            0.1,
            0.01,
            0.001,
            0.0001,
            0.00001,
            0.000001,
            0.0000001,
            0.00000001,
            0.000000001,
            0.0000000001,
            0.00000000001,
            0.000000000001,
            0.0000000000001,
            0.00000000000001,
            0.000000000000001,
            0.0000000000000001,
            0.00000000000000001,
            0.000000000000000001,
            0.0000000000000000001,
            0.00000000000000000001
    };


    /**
     * 用于将double转为long，来自文章中的fast rounding部分
     * @param db
     * @return n
     */
    static long doubleToLong (double db) {

        double n = db + MAGIC_NUMBER - MAGIC_NUMBER;
////        暂不考虑 Special values which cannot be casted to int64 without an undefined behaviour
//        if (!Double.isFinite(num) || Double.isNaN(num) || num > ENCODING_UPPER_LIMIT || num < ENCODING_LOWER_LIMIT) {
//            return (long) ENCODING_UPPER_LIMIT;
//        }
        return (long) n;
    }

    static void findTopKCombinations(Vector<Vector<Double>> vectorsSampled){
        state.bestKCombinations.clear();
        Map<Map.Entry<Byte, Byte>, Integer> bestKCombinationsHash = new HashMap<>();    // 记录每个组合出现的次数
        for (Vector<Double> sampledVector : vectorsSampled) {
            int nSamples = sampledVector.size();
            byte bestFactor = MAX_EXPONENT;
            byte bestExponent = MAX_EXPONENT;

            // Initialize bestTotalBits using the worst possible total bits obtained from compression【异常值大小+正常值大小】
            long bestTotalBits = (long) nSamples * (EXACT_TYPE_BITSIZE + ALPConstants.EXCEPTION_POSITION_SIZE) + (long) nSamples * EXACT_TYPE_BITSIZE;

            // Try all combinations in search for the one which minimize the compression size
            for (byte expIdx = MAX_EXPONENT; expIdx >= 0; expIdx--) {
                for (byte factorIdx = expIdx; factorIdx >= 0; factorIdx--) {
                    int exceptionsCnt = 0;  // 记录异常值出现次数
                    int nonExceptionsCnt = 0;   // 记录能够正常编码的次数
                    int estimatedBitsPerValue = 0;  // 预计单值正常编码所需要的位宽
                    long estimatedCompressionSize = 0;  // 预计编码后所需的总位数
                    long maxEncodedValue = Long.MIN_VALUE;
                    long minEncodedValue = Long.MAX_VALUE;

                    for (Double db : sampledVector) {
                        //                        Double decoded_value;
//                        Double tmp_encoded_value;
//                        Long encoded_value;
                        double tmp_encoded_value = db * EXP_ARR[expIdx] * FRAC_ARR[factorIdx];
                        long encoded_value = doubleToLong(tmp_encoded_value);    // 对应ALPenc

                        // The cast to double is needed to prevent a signed integer overflow
                        double decoded_value = (double) (encoded_value) * ALPConstants.FACT_ARR[factorIdx] * FRAC_ARR[expIdx];    // 对应Pdec
                        if (decoded_value == db) {
                            nonExceptionsCnt++;
                            maxEncodedValue = Math.max(encoded_value, maxEncodedValue);
                            minEncodedValue = Math.min(encoded_value, minEncodedValue);
                        } else {
                            exceptionsCnt++;
                        }
                    }
                    // Skip combinations which yields to almost all exceptions
                    if (nonExceptionsCnt < 2) {
                        continue;
                    }
                    // Evaluate factor/exponent compression size (we optimize for FOR)
                    long delta = maxEncodedValue - minEncodedValue;
                    estimatedBitsPerValue = (int) Math.ceil( Math.log(delta + 1) / Math.log(2) );
                    estimatedCompressionSize += (long) nSamples * estimatedBitsPerValue;    // 正常编码的部分
                    estimatedCompressionSize += (long) exceptionsCnt * (EXACT_TYPE_BITSIZE + ALPConstants.EXCEPTION_POSITION_SIZE);   // 异常值部分

                    // 更新单个向量中的最佳组合
                    if ((estimatedCompressionSize < bestTotalBits) ||
                            // We prefer bigger exponents
                            (estimatedCompressionSize == bestTotalBits && (bestExponent < expIdx)) ||
                            // We prefer bigger factors
                            ((estimatedCompressionSize == bestTotalBits && bestExponent == expIdx) && (bestFactor < factorIdx))) {
                                bestTotalBits = estimatedCompressionSize;
                                bestExponent = expIdx;
                                bestFactor = factorIdx;
                    }
                }
            }
            // 更新行组中的最佳组合
            Map.Entry<Byte, Byte> bestCombination = new AbstractMap.SimpleEntry<>(bestExponent, bestFactor);
            int cnt = bestKCombinationsHash.getOrDefault(bestCombination,0);
            bestKCombinationsHash.put(bestCombination, cnt + 1);
        }

        // Convert our hash pairs to a Combination vector to be able to sort
        Vector<ALPCombination> bestKCombinations = new Vector<>();
        bestKCombinationsHash.forEach((key,value)->{
            bestKCombinations.add(new ALPCombination(key.getKey(),  // Exponent
                                                    key.getValue(), // Factor
                                                    value           // N of times it appeared (hash value)
            ));
        });

        // 使用 List.sort() 进行排序，传入自定义的比较器     等效于C++中的 sort(bestKCombinations.begin(), bestKCombinations.end(), compareALPCombinations);
        bestKCombinations.sort((c1, c2) -> {
            if (ALPCombination.compareALPCombinations(c1, c2)) {
                return -1; // 返回负值表示 c1 应排在 c2 前面
            } else {
                return 1;
            }
        });

        // Save k' best combinations
        for (int i = 0; i < Math.min(ALPConstants.MAX_COMBINATIONS, (byte)bestKCombinations.size()); i++) {
            state.bestKCombinations.add(bestKCombinations.get(i));
        }
    }

    public static  void findBestFactorAndExponent(Vector<Double> inputVector, int nValues, ALPCompressionState state) {
        // We sample equidistant values within a vector; to do this we skip a fixed number of values
        Vector<Double> vectorSample = new Vector<>();
        int idxIncrements = Math.max(1, (int) Math.ceil((double) nValues / ALPConstants.SAMPLES_PER_VECTOR));
        for (int i = 0; i < nValues; i += idxIncrements) {
            vectorSample.add(inputVector.get(i));
        }

        byte bestExponent = 0;
        byte bestFactor = 0;
        long bestTotalBits = 0;
        int worseTotalBitsCounter = 0;
        int nSamples = vectorSample.size();

        // We try each K combination in search for the one which minimize the compression size in the vector
        for (int combinationIdx = 0; combinationIdx < state.bestKCombinations.size(); combinationIdx++) {
            int exponentIdx = state.bestKCombinations.get(combinationIdx).e;
            int factorIdx = state.bestKCombinations.get(combinationIdx).f;
            int exceptionsCount = 0;
            long estimatedCompressionSize = 0;
            long maxEncodedValue = Long.MIN_VALUE;
            long minEncodedValue = Long.MAX_VALUE;

            for (int sampleIdx = 0; sampleIdx < nSamples; ++sampleIdx) {
                double db = vectorSample.get(sampleIdx);

                double tmpEncodedValue = db * EXP_ARR[exponentIdx] * FRAC_ARR[factorIdx];
                long encodedValue = (long) tmpEncodedValue;

                double decodedValue = encodedValue * ALPConstants.FACT_ARR[factorIdx] * FRAC_ARR[exponentIdx];
                if (decodedValue == db) {
                    maxEncodedValue = Math.max(encodedValue, maxEncodedValue);
                    minEncodedValue = Math.min(encodedValue, minEncodedValue);
                }else {
                    exceptionsCount++;
                }
            }

            long delta = Math.abs(maxEncodedValue - minEncodedValue);
            int estimatedBitsPerValue = (int) Math.ceil(Math.log(delta + 1) / Math.log(2));
            estimatedCompressionSize += (long) nSamples * estimatedBitsPerValue;
            estimatedCompressionSize += exceptionsCount * (ALPConstants.EXCEPTION_POSITION_SIZE * 8 + EXACT_TYPE_BITSIZE);

            if (combinationIdx == 0) {
                bestTotalBits = estimatedCompressionSize;
                bestFactor = (byte) factorIdx;
                bestExponent = (byte) exponentIdx;
                continue;
            }

            if (estimatedCompressionSize >= bestTotalBits) {
                worseTotalBitsCounter += 1;
                // 贪婪提前推出【连续两个组合未优于之前的最佳组合】
                if (worseTotalBitsCounter == ALPConstants.SAMPLING_EARLY_EXIT_THRESHOLD) {
                    break;
                }
                continue;
            }

            bestTotalBits = estimatedCompressionSize;
            bestFactor = (byte) factorIdx;
            bestExponent = (byte) exponentIdx;
            worseTotalBitsCounter = 0;
        }
        state.vectorExponent = bestExponent;
        state.vectorFactor = bestFactor;
    }

    public static void compress(Vector<Double> inputVector, int nValues, ALPCompressionState state, boolean isEmpty) {
        if (state.bestKCombinations.size() > 1) {
            findBestFactorAndExponent(inputVector, nValues, state);
        } else {
            state.vectorExponent = state.bestKCombinations.get(0).e;
            state.vectorFactor = state.bestKCombinations.get(0).f;
        }

        // Encoding Floating-Point to Int64
        //! We encode all the values regardless of their correctness to recover the original floating-point
        //! We detect exceptions later using a predicated comparison
        Vector<Double> tmpDecodedValues = new Vector<>(nValues);  // Tmp array to check wether the encoded values are exceptions
        for (int i = 0; i < nValues; i++) {
            double db = inputVector.get(i);
            double tmpEncodedValue = db * EXP_ARR[state.vectorExponent] * FRAC_ARR[state.vectorFactor];
            long encodedValue = doubleToLong(tmpEncodedValue);
            state.encodedIntegers[i] = encodedValue;

            double decodedValue = encodedValue * ALPConstants.FACT_ARR[state.vectorFactor] * FRAC_ARR[state.vectorExponent];
            tmpDecodedValues.set(i, decodedValue);
        }

        // Detecting exceptions with predicated comparison
        int exceptionsIdx = 0;
        Vector<Long> exceptionsPositions = new Vector<>(nValues);
        for (int i = 0; i < nValues; i++) {
            double decodedValue = tmpDecodedValues.get(i);
            double actualValue = inputVector.get(i);
            boolean isException = (decodedValue != actualValue);
            exceptionsPositions.set(exceptionsIdx, (long) i);
            exceptionsIdx += isException?1:0;
        }

        // Finding first non exception value
        long aNonExceptionValue = 0;
        for (int i = 0; i < nValues; i++) {
            if (i != exceptionsPositions.get(i)) {
                aNonExceptionValue = state.encodedIntegers[i];
                break;
            }
        }

        // Replacing that first non exception value on the vector exceptions
        short exceptionsCount = 0;
        for (long exceptionPos : exceptionsPositions) {
            double actualValue = inputVector.get((int) exceptionPos);
            state.encodedIntegers[(int) exceptionPos] = aNonExceptionValue;
            state.exceptions[exceptionsCount] = actualValue;
            state.exceptionsPositions[exceptionsCount] = (short) exceptionPos;
            exceptionsCount++;
        }
        state.exceptionsCount = exceptionsCount;

        // Analyze FFOR
        long minValue = Long.MAX_VALUE;
        long maxValue = Long.MIN_VALUE;
        for (int i = 0; i < nValues; i++) {
            long encodedValue = state.encodedIntegers[i];
            maxValue = Math.max(maxValue, encodedValue);
            minValue = Math.min(minValue, encodedValue);
        }
        long minMaxDiff = maxValue - minValue;

        // 意义不明
//        auto *u_encoded_integers = reinterpret_cast<uint64_t *>(state.encoded_integers);
//        auto const u_min_value = static_cast<uint64_t>(min_value);

        // Subtract FOR
        if (!isEmpty) { //! We only execute the FOR if we are writing the data
            for (int i = 0; i < nValues; i++) {
                state.encodedIntegers[i] -= minValue;
            }
        }

//        int bitWidth = BitpackingPrimitives.minimumBitWidth(minMaxDiff);
//        int bpSize = BitpackingPrimitives.getRequiredSize(nValues, bitWidth);
//
//        if (!isEmpty && bitWidth > 0) { //! We only execute the BP if we are writing the data
//            long[] uEncodedIntegers = Arrays.stream(state.encodedIntegers).mapToLong(i -> i).toArray();
//            BitpackingPrimitives.packBuffer(state.valuesEncoded, uEncodedIntegers, nValues, bitWidth);
//        }
//
//        state.bitWidth = (short) bitWidth;
//        state.bpSize = bpSize;
//        state.frameOfReference = minValue;
    }




    public static void main(String[] args) {
        System.out.println(MAGIC_NUMBER);
    }
}
