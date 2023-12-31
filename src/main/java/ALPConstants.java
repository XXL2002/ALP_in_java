public class ALPConstants {
    public static final int ALP_VECTOR_SIZE = 1024; // 每个向量所含的值的数量
    public static final int RG_SAMPLES = 8; // 每行组采样的向量数
    public static final short SAMPLES_PER_VECTOR = 32;  // 每向量采样的浮点数数量
    public static final byte EXCEPTION_POSITION_SIZE = Short.SIZE;// / Byte.SIZE;
    public static final byte SAMPLING_EARLY_EXIT_THRESHOLD = 2;
    public static final byte MAX_COMBINATIONS = 5;

    public static final long[] FACT_ARR = {
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000,
            100000000, 1000000000, 10000000000L, 100000000000L, 1000000000000L,
            10000000000000L, 100000000000000L, 1000000000000000L,
            10000000000000000L, 100000000000000000L, 1000000000000000000L
    };

    public static final long[] U_FACT_ARR = {
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000,
            100000000, 1000000000, 10000000000L, 100000000000L, 1000000000000L,
            10000000000000L, 100000000000000L, 1000000000000000L,
            10000000000000000L, 100000000000000000L, 1000000000000000000L
    };
}
