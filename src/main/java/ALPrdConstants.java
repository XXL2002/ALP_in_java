public class ALPrdConstants {
    public static final int ALP_VECTOR_SIZE = 1024;
    public static final byte DICTIONARY_BW = 3;
    public static final byte DICTIONARY_SIZE = 1 << DICTIONARY_BW; // 8
    public static final byte CUTTING_LIMIT = 16;
    public static final byte DICTIONARY_SIZE_BYTES = 16;

    public static final byte EXCEPTION_SIZE = Short.BYTES;
    public static final byte METADATA_POINTER_SIZE = Integer.BYTES;
    public static final byte EXCEPTIONS_COUNT_SIZE = Short.BYTES;
    public static final byte EXCEPTION_POSITION_SIZE = Short.BYTES;
    public static final byte R_BW_SIZE = Byte.BYTES;
    public static final byte HEADER_SIZE = METADATA_POINTER_SIZE + R_BW_SIZE; // Pointer to metadata + Right BW
}
