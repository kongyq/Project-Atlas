package edu.udel.irl.atlas.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SynsetFormatCheckerTest {

    @Test
    public void check() {
        System.out.println(SynsetFormatChecker.check("00033020-n"));
        System.out.println(SynsetFormatChecker.check("yu00033020-n"));
        System.out.println(SynsetFormatChecker.check("00033020-h"));
        System.out.println(SynsetFormatChecker.check("000330209-n"));
        System.out.println(SynsetFormatChecker.check("000330209-ne"));
    }
}