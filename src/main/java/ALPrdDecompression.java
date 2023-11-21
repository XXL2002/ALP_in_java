import java.util.Arrays;

public class ALPrdDecompression {
    static void decompress(byte[] leftEncoded, byte[] rightEncoded, short[] leftPartsDict, long[] output,
                               int valuesCount, short exceptionsCount, short[] exceptions,
                               short[] exceptionsPositions, byte rightBitWidth) {

        byte[] leftDecoded = new byte[ALPrdConstants.ALP_VECTOR_SIZE * 8];
        byte[] rightDecoded = new byte[ALPrdConstants.ALP_VECTOR_SIZE * 8];
        byte leftBitWidth = ALPrdConstants.DICTIONARY_BW;

//        // Bitunpacking left and right parts
//        BitpackingPrimitives.unpackBuffer(leftDecoded, leftEncoded, valuesCount, leftBitWidth);
//        BitpackingPrimitives.unpackBuffer(rightDecoded, rightEncoded, valuesCount, rightBitWidth);

        short[] leftParts = Arrays.copyOfRange(leftPartsDict, 0, valuesCount);
        long[] rightParts = new long[valuesCount];
        for (int i = 0; i < valuesCount; i++) {
            rightParts[i] = rightDecoded[i];
        }

        // Decoding 拼接
        for (int i = 0; i < valuesCount; i++) {
            short left = leftPartsDict[leftParts[i]];
            long right = rightParts[i];
            output[i] = ((long) left << rightBitWidth) | right;
        }

        // Exceptions Patching (exceptions only occur in left parts) 处理异常值【字典外的值】
        for (int i = 0; i < exceptionsCount; i++) {
            long right = rightParts[exceptionsPositions[i]];
            short left = exceptions[i];
            output[exceptionsPositions[i]] =  (((long) left << rightBitWidth) | right);
        }
    }
}