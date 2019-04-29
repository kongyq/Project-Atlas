package edu.udel.irl.atlas.util;

import java.util.regex.Pattern;

public class SynsetFormatChecker {

    /**
     * Return true if the term is a synset
     * @param synset term string
     * @return true if term string is a synset format
     */
    public static boolean check(String synset) {
        return Pattern.matches("^[\\d]{8}-[nvra]$", synset);
    }
}
