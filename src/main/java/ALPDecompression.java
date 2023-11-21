public class ALPDecompression {
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
    public static void decompress(byte[] forEncoded, double[] output, int count, byte vectorFactor, byte vectorExponent,
                                  short exceptionsCount, double[] exceptions, short[] exceptionsPositions,
                                  long frameOfReference, short bitWidth) {

        long factor = ALPConstants.U_FACT_ARR[vectorFactor];
        double exponent = FRAC_ARR[vectorExponent];

        // Bit Unpacking
        byte[] forDecoded = new byte[ALPConstants.ALP_VECTOR_SIZE * 8];
        if (bitWidth > 0) {
            // TODO
//            BitpackingPrimitives.unPackBuffer(forDecoded, forEncoded, count, bitWidth);
        }
        long[] encodedIntegers = new long[ALPConstants.ALP_VECTOR_SIZE * 8];

        // unFOR
        for (int i = 0; i < count; i++) {
            encodedIntegers[i] = frameOfReference + forDecoded[i];
        }

        // Decoding
        for (int i = 0; i < count; i++) {
            double encodedInteger = encodedIntegers[i];
            output[i] = (double)((long) (encodedInteger)) * factor * exponent;
        }

        // Exceptions Patching
        for (int i = 0; i < exceptionsCount; i++) {
            output[exceptionsPositions[i]] = exceptions[i];
        }
    }
}
