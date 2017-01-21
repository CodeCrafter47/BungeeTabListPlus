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
import codecrafter47.bungeetablistplus.yamlconfig.Factory;
import codecrafter47.bungeetablistplus.yamlconfig.Subtype;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.List;

@Subtype(type = AnimatedComponent.class, tag = "!animated")
@Subtype(type = ConditionalComponent.class, tag = "!conditional")
@Subtype(type = TableComponent.class, tag = "!table")
@Subtype(type = PlayersByServerComponent.class, tag = "!players_by_server")
@Subtype(type = PlayersComponent.class, tag = "!players")
@Subtype(type = SpacerComponent.class, tag = "!spacer")
@Subtype(type = BasicComponent.class)
public abstract class Component {

    @Factory
    public static Component fromList(List<Component> list) {
        return new ListComponent(list);
    }

    public abstract boolean hasConstantSize();

    /**
     * Must be implemented when hasConstantSize returns true
     *
     * @return
     */
    public int getSize() {
        return 0;
    }

    public abstract Instance toInstance(Context context);

    public abstract static class Instance {
        protected final Context context;
        protected boolean active = false;
        protected boolean hasValidPosition = false;
        private ComponentTablistAccess cta;

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

        public void setPosition(ComponentTablistAccess cta) {
            hasValidPosition = true;
            this.cta = cta;
        }

        @Nullable
        protected final ComponentTablistAccess getTablistAccess() {
            if (active && hasValidPosition && this.cta != null) {
                return cta;
            }
            return null;
        }

        public abstract int getMinSize();

        public abstract int getPreferredSize();

        public abstract int getMaxSize();

        public abstract boolean isBlockAligned();
    }
}
