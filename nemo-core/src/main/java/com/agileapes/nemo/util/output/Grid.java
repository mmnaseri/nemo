package com.agileapes.nemo.util.output;

import com.agileapes.couteau.basics.api.Filter;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

/**
 * This is a visual asset that will facilitate the display of tabular information. The grid must be instantiated
 * by specifying a pattern denoting its row-by-row structure.
 *
 * @see #Grid(String)
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/17/13, 4:13 PM)
 */
public class Grid {

    private static final Container LINE = new StaticContainer(" | ");
    private static final Container SPACE = new StaticContainer("  ");

    /**
     * The container interface is used internally to abstract the process of drawing cells with different
     * rendering schemes without having to write in their rendering code.
     */
    private static interface Container {
        String draw(String input);
    }

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

    /**
     * Here pattern is the definition of the table's rows.
     * @param pattern    this parameter indicates how each row of the table should be organized.
     *                   The parser will go over the pattern to discover the how each cell should be
     *                   displayed.
     *                   <ul>
     *                   <li><strong><code>' '</code> (single space character)</strong> means a space of two between
     *                   the two cells around it. It can be also used to add padding around cells. The padding size is
     *                   constant (2) however.</li>
     *                   <li><strong><code>|</code></strong> denotes a vertical line. Vertical lines are drawn with a padding
     *                   of one on either side.</li>
     *                   <li><strong><code>*</code></strong> means that the width of the cell is determined by the longest
     *                   string in all the rows in this column. The width is not limited, so be careful to not use this
     *                   option when displaying strings that are expected to be long, naturally, over display terminals
     *                   with a limited width.</li>
     *                   <li><strong><code>c\d+</code></strong> (the character 'c' followed by an integer number) means a cell of the
     *                   specified width, with its content centered.</li>
     *                   <li><strong><code>l\d+</code></strong> (the character 'l' followed by an integer number) means a cell of the
     *                   indicated width, with its content aligned to the left</li>
     *                   <li><strong><code>r\d+</code></strong> (the character 'r' followed by an integer number) means a cell of the
     *                   indicated width, with its content aligned to the right</li>
     *                   <li><strong><code>w\d+</code></strong> (the character 'w' followed by an integer number) means a cell of the
     *                   specified width, with its content wrapped and justified. Note that using this option is the only
     *                   way for having cells that span across multiple rows.</li>
     *                   </ul>
     *                   With the exception of the <code>w</code> modifier, all cells with a fixed width can only contain
     *                   strings of a fixed width, with the rest of them truncated.
     *                   You should build the grid by adding cells to it through {@link #add(String...)} and then calling to
     *                   {@link #draw()} to render it into a String object that can be further manipulated, processed, or
     *                   just printed to the output.
     *                   You can use {@link #line()} to draw horizontal lines between cells or arbitrarily and whenever needed.
     */
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
        try {
            this.expected = with(containers).filter(new Filter<Container>() {
                @Override
                public boolean accepts(Container item) {
                    return !(item instanceof StaticContainer);
                }
            }).count();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * The add method will add a row to the current position in the table. Each element of input will
     * denote the contents of a single cell.
     * @param input    the cells. Do note that the number of cells must match that of the cells specified
     *                 when writing the pattern for the grid through {@link #Grid(String)}
     */
    public void add(String ... input) {
        if (input.length != expected) {
            throw new IllegalArgumentException("Expected " + expected + " cells while got " + input.length);
        }
        rows.add(Arrays.asList(input));
    }

    /**
     * Will add a horizontal line to the current position in the table.
     */
    public void line() {
        rows.add(null);
    }

    /**
     * @return the lengths of the contents in each column
     */
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

    /**
     * Will replace each cell's content with a rendered version of how its contents should be
     * @param lengths    the lengths of the columns
     * @return the rendered grid, without support for row spanning
     * @see #getLengths()
     */
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

    /**
     * Will add support for row spanning to a grid that has already been rendered.
     * @param lengths      the lengths of each column
     * @param processed    the processed table (rendered)
     * @return the rendered table with support for row spanning
     * @see #getLengths()
     * @see #processRows(java.util.List)
     */
    private List<List<String>> addRowSpanning(List<Integer> lengths, List<List<String>> processed) {
        final List<List<String>> list = new ArrayList<List<String>>();
        for (List<String> row : processed) {
            if (row == null) {
                final ArrayList<String> newRow = new ArrayList<String>();
                list.add(newRow);
                for (int i = 0; i < lengths.size(); i++) {
                    if (containers[i] instanceof StaticContainer) {
                        newRow.add(StringUtils.center("+", containers[i].draw(null).length()).replace(' ', '-'));
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
        return list;
    }

    /**
     * Will render the final grid as it is at the moment this method is called. Note that subsequent calls to this
     * method will result in a freshly rendered String.
     * @return the rendered grid
     */
    public String draw() {
        final List<Integer> lengths = getLengths();
        List<List<String>> processed;
        processed = addRowSpanning(lengths, processRows(lengths));
        final StringBuilder builder = new StringBuilder();
        for (List<String> row : processed) {
            for (String cell : row) {
                builder.append(cell);
            }
            builder.append("\n");
        }
        return builder.toString();
    }

}
