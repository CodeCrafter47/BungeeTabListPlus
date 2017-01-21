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
import codecrafter47.bungeetablistplus.tablist.component.ComponentTablistAccess;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ContainerComponent extends Component implements Validate {

    @Getter
    @Setter
    private boolean fillSlotsVertical = false;
    private ListComponent components;

    public List<Component> getComponents() {
        return components.getList();
    }

    public void setComponents(List<Component> components) {
        this.components = new ListComponent(components);
    }

    @Override
    public boolean hasConstantSize() {
        return components.hasConstantSize();
    }

    @Override
    public int getSize() {
        return components.getSize();
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context);
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull(components, "components is null");
    }

    public class Instance extends Component.Instance {
        Component.Instance delegate;

        protected Instance(Context context) {
            super(context);
            Context childContext;
            if (fillSlotsVertical) {
                childContext = context.derived().put(Context.KEY_COLUMNS, 1);
            } else {
                childContext = context;
            }
            delegate = components.toInstance(childContext);
        }

        @Override
        public void activate() {
            super.activate();
            delegate.activate();
        }

        @Override
        public void deactivate() {
            super.deactivate();
            delegate.deactivate();
        }

        @Override
        public void update1stStep() {
            super.update1stStep();
            delegate.update1stStep();
        }

        @Override
        public void setPosition(ComponentTablistAccess cta) {
            super.setPosition(cta);
            if (fillSlotsVertical) {
                int columns = context.get(Context.KEY_COLUMNS);
                int rows = (cta.getSize() + columns - 1) / columns;
                delegate.setPosition(ComponentTablistAccess.createChild(cta, cta.getSize(), index ->
                        (index / rows) + (index % rows) * columns
                ));
            } else {
                delegate.setPosition(cta);
            }
        }

        @Override
        public void update2ndStep() {
            super.update2ndStep();
            delegate.update2ndStep();
        }

        @Override
        public int getMinSize() {
            return delegate.getMinSize();
        }

        @Override
        public int getPreferredSize() {
            return fillSlotsVertical ? delegate.getPreferredSize() * context.get(Context.KEY_COLUMNS) : delegate.getPreferredSize();
        }

        @Override
        public int getMaxSize() {
            return fillSlotsVertical ? delegate.getMaxSize() * context.get(Context.KEY_COLUMNS) : delegate.getMaxSize();
        }

        @Override
        public boolean isBlockAligned() {
            return fillSlotsVertical || delegate.isBlockAligned();
        }
    }
}
