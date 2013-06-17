package com.agileapes.nemo.util.output;

import com.agileapes.nemo.contract.Filter;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.agileapes.nemo.util.CollectionDSL.with;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 4:13 PM)
 */
public class Grid {

    private static interface Container {
        String draw(String input);
    }

    private static final Container LINE = new StaticContainer(" | ");
    private static final Container SPACE = new StaticContainer("   ");

    private static abstract class LimitedContainer implements Container {

        protected final int width;

        protected LimitedContainer(int width) {
            this.width = width;
        }

        private int getWidth() {
            return width;
        }

    }

    private static class StaticContainer extends LimitedContainer {

        private final String text;

        protected StaticContainer(String text) {
            super(text.length());
            this.text = text;
        }

        @Override
        public String draw(String input) {
            return text;
        }

    }

    private static class CenterContainer extends LimitedContainer {

        protected CenterContainer(int width) {
            super(width);
        }

        @Override
        public String draw(String input) {
            return StringUtils.center(input.length() > width ? input.substring(0, width) : input, width);
        }

    }

    private static class LeftContainer extends LimitedContainer {

        protected LeftContainer(int width) {
            super(width);
        }

        @Override
        public String draw(String input) {
            return StringUtils.rightPad(input.length() > width ? input.substring(0, width) : input, width);
        }

    }

    private static class RightContainer extends LimitedContainer {

        protected RightContainer(int width) {
            super(width);
        }

        @Override
        public String draw(String input) {
            return StringUtils.left(input.length() > width ? input.substring(0, width) : input, width);
        }

    }

    private static class WrapperContainer extends LimitedContainer {

        protected WrapperContainer(int width) {
            super(width);
        }

        @Override
        public String draw(String input) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            new TextWrapper(new PrintStream(out), width, 0).print(input);
            return out.toString().trim();
        }

    }

    private static class GenericContainer implements Container {

        @Override
        public String draw(String input) {
            return input;
        }

    }

    private final Container[] containers;
    private List<List<String>> rows = new ArrayList<List<String>>();
    private final int expected;

    public Grid(Container[] containers) {
        this.containers = containers;
        this.expected = with(containers).filter(new Filter<Container>() {
            @Override
            public boolean accepts(Container item) {
                return !(item instanceof StaticContainer);
            }
        }).count();
    }

    public Grid(String pattern) {
        final List<Container> containers = new ArrayList<Container>();
        int i = 0;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == '|') {
                containers.add(LINE);
                i++;
            } else if (pattern.charAt(i) == ' ') {
                containers.add(SPACE);
                i++;
            } else if (pattern.substring(i).matches("c\\d+.*")) {
                final Matcher matcher = Pattern.compile("^c(\\d+)").matcher(pattern.substring(i));
                matcher.find();
                containers.add(new CenterContainer(Integer.parseInt(matcher.group(1))));
                i += matcher.group(0).length();
            } else if (pattern.substring(i).matches("l\\d+.*")) {
                final Matcher matcher = Pattern.compile("^l(\\d+)").matcher(pattern.substring(i));
                matcher.find();
                containers.add(new LeftContainer(Integer.parseInt(matcher.group(1))));
                i += matcher.group(0).length();
            } else if (pattern.substring(i).matches("r\\d+.*")) {
                final Matcher matcher = Pattern.compile("^r(\\d+)").matcher(pattern.substring(i));
                matcher.find();
                containers.add(new RightContainer(Integer.parseInt(matcher.group(1))));
                i += matcher.group(0).length();
            } else if (pattern.substring(i).matches("w\\d+.*")) {
                final Matcher matcher = Pattern.compile("^w(\\d+)").matcher(pattern.substring(i));
                matcher.find();
                containers.add(new WrapperContainer(Integer.parseInt(matcher.group(1))));
                i += matcher.group(0).length();
            } else if (pattern.charAt(i) == '*') {
                containers.add(new GenericContainer());
                i++;
            }
        }
        this.containers = containers.toArray(new Container[containers.size()]);
        this.expected = with(containers).filter(new Filter<Container>() {
            @Override
            public boolean accepts(Container item) {
                return !(item instanceof StaticContainer);
            }
        }).count();
    }

    public void add(String ... input) {
        if (input.length != expected) {
            throw new IllegalArgumentException("Expected " + expected + " cells while got " + input.length);
        }
        rows.add(Arrays.asList(input));
    }

    public void addLine() {
        rows.add(null);
    }

    private List<Integer> getLengths() {
        final List<Integer> lengths = new ArrayList<Integer>();
        int column = 0;
        for (final Container container : containers) {
            if (container instanceof LimitedContainer) {
                lengths.add(((LimitedContainer) container).getWidth());
            } else {
                int length = 0;
                for (List<String> row : rows) {
                    if (row == null) {
                        continue;
                    }
                    length = Math.max(container.draw(row.get(column)).length(), length);
                }
                lengths.add(length);
            }
            if (!(container instanceof StaticContainer)) {
                column++;
            }
        }
        return lengths;
    }

    private List<List<String>> processRows(List<Integer> lengths) {
        final List<List<String>> processed = new ArrayList<List<String>>();
        for (List<String> row : rows) {
            if (row == null) {
                processed.add(null);
                continue;
            }
            final ArrayList<String> newRow = new ArrayList<String>();
            processed.add(newRow);
            int column = 0;
            for (int i = 0; i < containers.length; i++) {
                Container container = containers[i];
                if (container instanceof StaticContainer) {
                    newRow.add(container.draw(null));
                } else {
                    String cell = row.get(column);
                    if (!(container instanceof LimitedContainer)) {
                        final Integer length = lengths.get(i);
                        cell = cell.length() > length ? cell.substring(0, length) : cell;
                        cell = StringUtils.rightPad(cell, cell.length() - length);
                    }
                    newRow.add(container.draw(cell));
                    column++;
                }
            }
        }
        return processed;
    }

    public String draw() {
        final List<Integer> lengths = getLengths();
        final List<List<String>> processed = processRows(lengths);
        final List<List<String>> list = new ArrayList<List<String>>();
        for (List<String> row : processed) {
            if (row == null) {
                final ArrayList<String> newRow = new ArrayList<String>();
                list.add(newRow);
                for (int i = 0; i < lengths.size(); i++) {
                    if (containers[i] instanceof StaticContainer) {
                        newRow.add("-+-");
                    } else {
                        newRow.add(StringUtils.repeat("-", lengths.get(i)));
                    }
                }
                continue;
            }
            int rowSpan = 1;
            for (String cell : row) {
                rowSpan = Math.max(rowSpan, cell.split("(\r\n|\n\r|\n|\r)").length);
            }
            while (rowSpan -- > 0) {
                final List<String> newRow = new ArrayList<String>();
                for (int i = 0; i < row.size(); i++) {
                    String cell = row.get(i);
                    if (containers[i] instanceof StaticContainer) {
                        StaticContainer container = (StaticContainer) containers[i];
                        newRow.add(container.draw(null));
                    } else {
                        if (cell == null) {
                            newRow.add(StringUtils.repeat(" ", lengths.get(i)));
                        } else {
                            final String[] split = cell.split("(\r\n|\n\r|\n|\r)");
                            newRow.add(StringUtils.rightPad(split[0], lengths.get(i)));
                            split[0] = "";
                            cell = StringUtils.join(split, "\n").trim();
                            if (cell.isEmpty()) {
                                cell = null;
                            }
                            row.set(i, cell);
                        }
                    }
                }
                list.add(newRow);
            }
        }
        final StringBuilder builder = new StringBuilder();
        for (List<String> row : list) {
            for (String cell : row) {
                builder.append(cell);
            }
            builder.append("\n");
        }
        return builder.toString();
    }

}
