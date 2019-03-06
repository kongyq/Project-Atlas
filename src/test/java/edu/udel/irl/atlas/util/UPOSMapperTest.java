package edu.udel.irl.atlas.util;

import com.babelscape.util.UniversalPOS;
import org.junit.Test;

import static org.junit.Assert.*;

public class UPOSMapperTest {

    @Test
    public void convert() {
        UPOSMapper uposMapper = new UPOSMapper("en-ptb");
        System.out.println(uposMapper.convert("VBZ"));
        UniversalPOS.valueOf(uposMapper.convert("VBZ"));
    }
}