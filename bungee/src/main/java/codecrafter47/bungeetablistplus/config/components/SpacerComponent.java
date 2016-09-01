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

public class SpacerComponent extends Component {
    @Override
    public boolean hasConstantSize() {
        return false;
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context);
    }

    private static class Instance extends Component.Instance {

        protected Instance(Context context) {
            super(context);
        }

        @Override
        public int getMinSize() {
            return 0;
        }

        @Override
        public int getPreferredSize() {
            return 0;
        }

        @Override
        public int getMaxSize() {
            return 200;
        }

        @Override
        public boolean isBlockAligned() {
            return false;
        }
    }
}
