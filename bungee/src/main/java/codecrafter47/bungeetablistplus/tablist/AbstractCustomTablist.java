/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import codecrafter47.bungeetablistplus.util.IconUtil;
import de.codecrafter47.taboverlay.Icon;
import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.Integer.min;

/**
 * Represents a custom tab list.
 */
public abstract class AbstractCustomTablist implements CustomTablist {
    private static Icon[] EMPTY_ICON_ARRAY = new Icon[0];
    private static String[] EMPTY_STRING_ARRAY = new String[0];
    private static int[] EMPTY_INT_ARRAY = new int[0];

    private int size;
    private int columns;
    private int rows;
    private Icon[] icon;
    private String[] text;
    private int[] ping;
    private String header;
    private String footer;

    /**
     * Create a new custom tab list with size 0.
     */
    AbstractCustomTablist() {
        this.size = 0;
        this.columns = 1;
        this.rows = 0;
        this.icon = EMPTY_ICON_ARRAY;
        this.text = EMPTY_STRING_ARRAY;
        this.ping = EMPTY_INT_ARRAY;
        this.header = null;
        this.footer = null;
    }

    /**
     * Create a new custom tab list with the given size. See {@link #setSize(int)}.
     *
     * @param size the size
     * @throws IllegalArgumentException if the size is not allowed
     */
    AbstractCustomTablist(int size) {
        this();
        setSize(size);
    }

    int index(int row, int column) {
        int index = row * this.columns + column;
        if (index >= size) {
            throw new IndexOutOfBoundsException(String.format("Index [row=%s,column=%s] not inside tab list [rows=%s,columns=%s]", row, column, this.rows, this.columns));
        }
        return index;
    }

    @Override
    public synchronized void setSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size is negative");
        } else if (size == 0) {
            setSize(0, 0);
        } else {
            int columns = (size + 19) / 20;
            int rows = size / columns;
            if (columns * rows != size) {
                throw new IllegalArgumentException("size is not rectangular");
            }
            setSize(columns, rows);
        }
    }

    protected void setSize(int columns, int rows) {
        int size = columns * rows;
        if (size == 0) {
            this.size = 0;
            this.columns = 1;
            this.rows = 0;
            this.icon = EMPTY_ICON_ARRAY;
            this.text = EMPTY_STRING_ARRAY;
            this.ping = EMPTY_INT_ARRAY;
        } else {
            Icon[] icon = new Icon[size];
            String[] text = new String[size];
            int[] ping = new int[size];
            Arrays.fill(icon, Icon.DEFAULT_STEVE);
            Arrays.fill(text, "");
            Arrays.fill(ping, 0);
            for (int col = min(this.columns, columns) - 1; col >= 0; col--) {
                for (int row = min(this.rows, rows) - 1; row >= 0; row--) {
                    icon[row * columns + col] = this.icon[row * this.columns + col];
                    text[row * columns + col] = this.text[row * this.columns + col];
                    ping[row * columns + col] = this.ping[row * this.columns + col];
                }
            }
            this.size = size;
            this.columns = columns;
            this.rows = rows;
            this.icon = icon;
            this.text = text;
            this.ping = ping;
        }
        onSizeChanged();
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    @Nonnull
    public codecrafter47.bungeetablistplus.api.bungee.Icon getIcon(int row, int column) {
        return IconUtil.convert(this.icon[index(row, column)]);
    }

    Icon getIcon(int index) {
        return this.icon[index];
    }

    @Override
    @Nonnull
    public String getText(int row, int column) {
        return this.text[index(row, column)];
    }

    String getText(int index) {
        return this.text[index];
    }

    @Override
    public int getPing(int row, int column) {
        return this.ping[index(row, column)];
    }

    int getPing(int index) {
        return this.ping[index];
    }

    @Override
    public synchronized void setSlot(int row, int column, @Nonnull @NonNull codecrafter47.bungeetablistplus.api.bungee.Icon icon, @Nonnull @NonNull String text, int ping) {
        int index = index(row, column);
        this.icon[index] = IconUtil.convert(icon);
        this.text[index] = text;
        this.ping[index] = ping;
        onSlotChanged(index);
    }

    @Override
    @Nullable
    public String getHeader() {
        return this.header;
    }

    @Override
    public synchronized void setHeader(@Nullable String header) {
        if (!Objects.equals(this.header, header)) {
            this.header = header;
            onHeaderOrFooterChanged();
        }
    }

    @Override
    @Nullable
    public String getFooter() {
        return footer;
    }

    @Override
    public synchronized void setFooter(@Nullable String footer) {
        if (!Objects.equals(this.footer, footer)) {
            this.footer = footer;
            onHeaderOrFooterChanged();
        }
    }

    protected abstract void onSizeChanged();

    protected abstract void onSlotChanged(int index);

    protected abstract void onHeaderOrFooterChanged();
}
