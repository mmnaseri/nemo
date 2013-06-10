package com.agileapes.nemo.util;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:05)
 */
public abstract class StringUtils {

    public static String uncapitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (Character.isUpperCase(text.charAt(0))) {
            return Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }
        return text;
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (Character.isLowerCase(text.charAt(0))) {
            return Character.toUpperCase(text.charAt(0)) + text.substring(1);
        }
        return text;
    }
}
