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

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.context.Context;
import com.google.common.base.Preconditions;

public abstract class Component {

    protected abstract boolean hasConstantSize();

    /**
     * Must be implemented when hasConstantSize returns true
     *
     * @return
     */
    protected int getSize() {
        return 0;
    }

    public abstract Instance toInstance(Context context);

    public abstract static class Instance {
        protected final Context context;
        protected boolean active = false;
        protected boolean hasValidPosition = false;
        protected int row, column, size;

        protected Instance(Context context) {
            this.context = context;
        }

        public void activate() {
            active = true;
        }

        public void deactivate() {
            active = false;
            hasValidPosition = false;
        }

        public void update1stStep() {
            Preconditions.checkState(active, "Not active");
        }

        public void update2ndStep() {
            Preconditions.checkState(active, "Not active");
            Preconditions.checkState(hasValidPosition, "Position invalid");
        }

        public final void setPosition(int row, int column, int size) {
            hasValidPosition = true;
            this.row = row;
            this.column = column;
            this.size = size;
        }

        protected void setSlot(int row, int column, Icon icon, String text, int ping) {
            if (active && hasValidPosition && row * context.getColumns() + column < size) {
                context.getTablist().setSlot(this.row + row, this.column + column, icon, text, ping);
            }
        }

        protected void setSlot(int n, Icon icon, String text, int ping) {
            if (active && hasValidPosition && n < size) {
                context.getTablist().setSlot(row + n / context.getColumns(), column + n % context.getColumns(), icon, text, ping);
            }
        }

        public abstract int getMinSize();

        public abstract int getMaxSize();

        public abstract boolean isBlockAligned();
    }
}
