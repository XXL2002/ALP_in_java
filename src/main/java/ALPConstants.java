public class ALPConstants {
    public static final int ALP_VECTOR_SIZE = 1024; // 每个向量所含的值的数量
    public static final int RG_SAMPLES = 8; // 每行组采样的向量数
    public static final short SAMPLES_PER_VECTOR = 32;  // 每向量采样的浮点数数量

//    private static final int STANDARD_ROW_GROUPS_SIZE = 100;    // 每个行组所包含的向量的个数？
//    // calculate how many equidistant vector we must jump within a rowgroup
//    public static final int RG_SAMPLES_DUCKDB_JUMP = (STANDARD_ROW_GROUPS_SIZE / RG_SAMPLES) / STANDARD_VECTOR_SIZE;

    public static final byte HEADER_SIZE = Integer.SIZE;// / Byte.SIZE;
    public static final byte EXPONENT_SIZE = Byte.SIZE;// / Byte.SIZE;
    public static final byte FACTOR_SIZE = Byte.SIZE;// / Byte.SIZE;
    public static final byte EXCEPTIONS_COUNT_SIZE = Short.SIZE;// / Byte.SIZE;
    public static final byte EXCEPTION_POSITION_SIZE = Short.SIZE;// / Byte.SIZE;
    public static final byte FOR_SIZE = Long.SIZE;// / Byte.SIZE;
    public static final byte BW_SIZE = Byte.SIZE;// / Byte.SIZE;
    public static final byte METADATA_POINTER_SIZE = Integer.SIZE;// / Byte.SIZE;

    public static final byte SAMPLING_EARLY_EXIT_THRESHOLD = 2;

    public static final double COMPACT_BLOCK_THRESHOLD = 0.80;

    public static final double ENCODING_UPPER_LIMIT = 9223372036854774784.0;
    public static final double ENCODING_LOWER_LIMIT = -9223372036854774784.0;

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
