package com.agileapes.nemo.util.output;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

/**
 * This class helps with hard-wrapping text so that it can be printed in an environment with
 * limited display width.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 4:10 PM)
 */
public class TextWrapper {

    private static final String STOP_CHARS = " \t\r\n,./`~=+-|\\[]{}";
    private final PrintStream output;
    private final int width;
    private final int indentation;

    public TextWrapper(PrintStream output, int width, int indentation) {
        this.output = output;
        this.width = width;
        this.indentation = indentation;
    }

    public void print(String text) {
        while (text.length() > width) {
            int i = width;
            while (i > 0 && !STOP_CHARS.contains(String.valueOf(text.charAt(i)))) {
                i --;
            }
            if (i == 0) {
                i = width - 1;
            }
            String line = text.substring(0, i);
            if (line.length() < width) {
                final String[] split = line.split("\\s+");
                final int spaces = split.length - 1;
                if (spaces > 0) {
                    int difference = width - line.length();
                    final String pad = StringUtils.repeat(" ", Math.max(1, (int) Math.round((double) difference / spaces)));
                    line = "";
                    for (int j = 0; j < split.length - 1; j ++) {
                        line += split[j] + pad;
                        difference -= pad.length();
                        if (j == split.length - 2) {
                            if (difference > 0) {
                                line += StringUtils.repeat(" ", difference);
                            }
                            line += split[split.length - 1];
                        }
                    }
                }
            }
            output.print(line);
            if (!STOP_CHARS.contains(String.valueOf(text.charAt(i)))) {
                output.print("-");
            }
            text = text.substring(i).trim();
            if (!text.isEmpty()) {
                output.println();
                output.print(StringUtils.repeat(" ", indentation));
            }
        }
        if (!text.isEmpty()) {
            output.println(text.trim());
        }
    }

}
