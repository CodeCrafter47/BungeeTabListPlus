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

package codecrafter47.bungeetablistplus.config.components;

import codecrafter47.bungeetablistplus.context.Context;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static java.lang.Integer.max;

@Getter
@Setter
public class TableComponent extends Component {
    private Map<String, Component> columns;
    private int size = -1;

    public void setColumns(Map<String, Component> columns) {
        int next = 0;
        for (String s : columns.keySet()) {
            if (s.contains("-")) {
                String[] tokens = s.split("-");
                int start = Integer.valueOf(tokens[0]);
                int end = Integer.valueOf(tokens[1]);
                if (start < next) {
                    throw new IllegalArgumentException("Column used twice.");
                }
                if (start > end) {
                    throw new IllegalArgumentException("Going backwards.");
                }
                next = end + 1;
            } else {
                int i = Integer.valueOf(s);
                if (i < next) {
                    throw new IllegalArgumentException("Column used twice.");
                }
                next = i + 1;
            }
        }

        this.columns = columns;
    }

    @Override
    public boolean hasConstantSize() {
        return size != -1;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context);
    }

    public class Instance extends Component.Instance {

        Component.Instance[] columns;
        int[] start;
        int[] width;
        int minSize;
        int maxSize;

        protected Instance(Context context) {
            super(context);
            columns = new Component.Instance[TableComponent.this.columns.size()];
            start = new int[TableComponent.this.columns.size()];
            width = new int[TableComponent.this.columns.size()];
            int j = 0;
            for (Map.Entry<String, Component> s : TableComponent.this.columns.entrySet()) {
                if (s.getKey().contains("-")) {
                    String[] tokens = s.getKey().split("-");
                    int start = Integer.valueOf(tokens[0]);
                    int end = Integer.valueOf(tokens[1]);
                    this.start[j] = start;
                    width[j] = end - start + 1;
                } else {
                    int i = Integer.valueOf(s.getKey());
                    start[j] = i;
                    width[j] = 1;
                }
                columns[j] = s.getValue().toInstance(context.derived().setColumns(width[j]));
                j += 1;
            }
        }

        @Override
        public void activate() {
            super.activate();
            for (Component.Instance component : columns) {
                component.activate();
            }
        }

        @Override
        public void deactivate() {
            super.deactivate();
            for (Component.Instance component : columns) {
                component.deactivate();
            }
        }

        @Override
        public void update1stStep() {
            super.update1stStep();
            minSize = 0;
            maxSize = Integer.MAX_VALUE;
            for (Component.Instance component : columns) {
                component.update1stStep();
                minSize = max(minSize, (component.getMinSize() + component.context.getColumns() - 1) / component.context.getColumns() * context.getColumns());
                maxSize = max(maxSize, (component.getMaxSize() + component.context.getColumns() - 1) / component.context.getColumns() * context.getColumns());
            }
        }

        @Override
        public void update2ndStep() {
            super.update2ndStep();
            for (int i = 0; i < columns.length; i++) {
                if (start[i] < context.getColumns()) {
                    Component.Instance component = columns[i];
                    component.setPosition(row, column + start[i], size / context.getColumns() * width[i]);
                    component.update2ndStep();
                }
            }
        }

        @Override
        public int getMinSize() {
            return TableComponent.this.size != -1 ? TableComponent.this.size : minSize;
        }

        @Override
        public int getMaxSize() {
            return TableComponent.this.size != -1 ? TableComponent.this.size : maxSize;
        }

        @Override
        public boolean isBlockAligned() {
            return true;
        }
    }
}
