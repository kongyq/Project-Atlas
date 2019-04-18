package edu.udel.irl.atlas.util;

public class Short2Bytes {

    public static byte[] encodeShort(short value){
        return encodeShort(value, new byte[2]);
    }

    public static byte[] encodeShort(short value, byte[] data){
        data[0] = (byte) (value >> 8);
        data[1] = (byte) value;
        return data;
    }

    /**
     * This will yield signed short and you need to call {@code Short.toUnsignedInt()} to get currect number.
     * @param bytes encoded short bytes
     * @return signed short
     */
    public static short decodeShort(byte[] bytes){
        return (short) (((bytes[0] & 0xFF) <<  8) |  (bytes[1] & 0xFF));
    }
}
