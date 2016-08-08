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
import codecrafter47.bungeetablistplus.template.IconTemplate;
import codecrafter47.bungeetablistplus.template.PingTemplate;
import codecrafter47.bungeetablistplus.template.TextTemplate;
import lombok.Data;

@Data
public class BasicComponent extends Component {

    private TextTemplate text;
    private IconTemplate icon;
    private PingTemplate ping;

    @Override
    protected boolean hasConstantSize() {
        return true;
    }

    @Override
    protected int getSize() {
        return 1;
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context);
    }

    public class Instance extends Component.Instance {

        public Instance(Context context) {
            super(context);
        }

        @Override
        public void update2ndStep() {
            super.update2ndStep();
            context.getTablist().setSlot(row, column, getIcon().evaluate(context), getText().evaluate(context), getPing().evaluate(context));
        }

        @Override
        public int getMinSize() {
            return 1;
        }

        @Override
        public int getMaxSize() {
            return 1;
        }

        @Override
        public boolean isBlockAligned() {
            return false;
        }
    }
}
