package edu.udel.irl.atlas.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class Short2BytesTest {

    @Test
    public void encodeShort() {
        short s = 32767;
        s++;
        byte[] bytes = Short2Bytes.encodeShort(s);
        System.out.println(Short.toUnsignedInt(Short2Bytes.decodeShort(bytes)));
    }

    @Test
    public void encodeShort1() {

        byte[] bytes1 = new byte[]{2,3};
        byte[] bytes2 = new byte[]{3,4};
        System.out.println(Arrays.equals(bytes1, bytes2));
    }

    @Test
    public void decodeShort() {
    }
}