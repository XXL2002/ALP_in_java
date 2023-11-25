public class ALPDecompression {
    private ALPrdDecompression ALPrdDe = new ALPrdDecompression();
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
    private long[] encodedValue;
    private double[] output;
    private int count;
    private byte vectorFactor;
    private byte vectorExponent;
    private short exceptionsCount;
    private double[] exceptions;
    private short[] exceptionsPositions;
    private long frameOfReference;
    private short bitWidth;

    public ALPDecompression(){}

    public ALPDecompression(byte e, byte f, short bitWidth, long frameOfReference, int count, long[]encodedValue, short exceptionsCount, double[] exceptions, short[] exceptionsPositions){
        // 仅供测试使用
        this.vectorExponent = e;
        this.vectorFactor = f;
        this.bitWidth = bitWidth;
        this.frameOfReference = frameOfReference;
        this.count = count;
        this.encodedValue = encodedValue;
        this.exceptionsCount = exceptionsCount;
        this.exceptions = exceptions;
        this.exceptionsPositions = exceptionsPositions;
    }

    public void deserialize(){
        /*
        TODO: read
            ALPCombination      最佳<e,f>组合       byte + byte                 -> vectorExponent, vectorFactor
            bitWidth            FOR单值所需位宽      short                      -> bitWidth
            frameOfReference    FOR基准值           long                       -> frameOfReference
            nValues             向量长度            int                         -> count
            ForValues           FOR偏移值           bits<bitWidth>[nValues]    -> encodedValue
            exceptionsCount     异常值数量           short                      -> exceptionsCount
            exceptions          异常值原值           double[exceptionsCount]    -> exceptions
            exceptionsPositions 异常值位置           short[exceptionsCount]     -> exceptionsPositions
         */
    }
    public double[] decompress() {
        output = new double[count];

        long factor = ALPConstants.U_FACT_ARR[vectorFactor];
        double exponent = FRAC_ARR[vectorExponent];

        // unFOR
        for (int i = 0; i < count; i++) {
            encodedValue[i] = frameOfReference + encodedValue[i];
        }

        // Decoding
        for (int i = 0; i < count; i++) {
            double encodedInteger = encodedValue[i];
            output[i] = (double)((long) (encodedInteger)) * factor * exponent;
        }

        // Exceptions Patching
        for (int i = 0; i < exceptionsCount; i++) {
            output[exceptionsPositions[i]] = exceptions[i];
        }

        return output;
    }

    public double[] entry(){
        boolean useALP = true;
        /*
        TODO: read 1 bit - >useALP
         */
        if (useALP){
            deserialize();
            return decompress();
        }else{
            ALPrdDe.deserialize();
            return ALPrdDe.decompress();
        }

    }
}
