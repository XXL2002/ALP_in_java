import java.util.Vector;

public class ALPCompressionState {
    public byte vectorExponent;
    public byte vectorFactor;
    public short exceptionsCount;
    public short bitWidth;
    public long bpSize;
    public long frameOfReference;
    public long[] encodedIntegers = new long[ALPConstants.ALP_VECTOR_SIZE];
    public double[] exceptions =  new double[ALPConstants.ALP_VECTOR_SIZE]; // 泛型数组需要强制转换
    public short[] exceptionsPositions = new short[ALPConstants.ALP_VECTOR_SIZE];
    public Vector<ALPCombination> bestKCombinations = new Vector<>();
    public byte[] valuesEncoded = new byte[ALPConstants.ALP_VECTOR_SIZE * 8];

    public ALPCompressionState() {
        this.vectorExponent = 0;
        this.vectorFactor = 0;
        this.exceptionsCount = 0;
        this.bitWidth = 0;
    }

    public void reset() {
        this.vectorExponent = 0;
        this.vectorFactor = 0;
        this.exceptionsCount = 0;
        this.bitWidth = 0;
    }

}
