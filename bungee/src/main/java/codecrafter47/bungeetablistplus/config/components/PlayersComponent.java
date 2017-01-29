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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;
import codecrafter47.bungeetablistplus.tablist.component.ComponentTablistAccess;
import codecrafter47.bungeetablistplus.template.IconTemplate;
import codecrafter47.bungeetablistplus.template.PingTemplate;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

@Getter
@Setter
public class PlayersComponent extends Component implements Validate {
    private PlayerSorter playerOrder = new PlayerSorter("alphabetically");
    private String playerSet;
    private Component playerComponent;
    private Component morePlayersComponent;
    private boolean fillSlotsVertical = false;
    int minSize = 0;
    int maxSize = -1;

    @Override
    public boolean hasConstantSize() {
        return false;
    }

    public void setPlayerComponent(Component component) {
        Preconditions.checkArgument(component.hasConstantSize(), "playerComponent needs to have a fixed size.");
        this.playerComponent = component;
    }

    public void setMorePlayersComponent(Component component) {
        Preconditions.checkArgument(component.hasConstantSize(), "morePlayersComponent needs to have a fixed size.");
        this.morePlayersComponent = component;
    }

    public void setMinSize(int minSize) {
        Preconditions.checkArgument(maxSize == -1 || minSize <= maxSize, "minSize needs to be smaller than maxSize.");
        this.minSize = minSize;
    }

    public void setMaxSize(int maxSize) {
        Preconditions.checkArgument(minSize <= maxSize, "minSize needs to be smaller than maxSize.");
        this.maxSize = maxSize;
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context);
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull(playerSet, "playerSet is null");
        Preconditions.checkNotNull(playerComponent, "playerComponent is null");
        Preconditions.checkNotNull(morePlayersComponent, "morePlayersComponent is null");
    }

    public class Instance extends Component.Instance {
        private List<Player> players = Collections.emptyList();
        private List<Component.Instance> activeComponents = new ArrayList<>();

        protected Instance(Context context) {
            super(context);
        }

        @Override
        public void deactivate() {
            super.deactivate();
            activeComponents.forEach(Component.Instance::deactivate);
            activeComponents.clear();
        }

        @Override
        public void update1stStep() {
            super.update1stStep();
            players = context.get(Context.KEY_PLAYER_SETS).get(playerSet);
            if (players == null) {
                players = Collections.emptyList();
                BungeeTabListPlus.getInstance().getLogger().info("Missing player set " + playerSet);
            }
            playerOrder.sort(context, players);
        }

        @Override
        public void setPosition(ComponentTablistAccess cta) {
            if (fillSlotsVertical) {
                int columns = context.get(Context.KEY_COLUMNS);
                int rows = (cta.getSize() + columns - 1) / columns;
                if (playerComponent.getSize() > 1 && columns % playerComponent.getSize() == 0) {
                    int playerComponentSize = playerComponent.getSize();
                    int scan = columns - playerComponentSize;
                    super.setPosition(ComponentTablistAccess.createChild(cta, cta.getSize(), index -> {
                                int c = index / (rows * playerComponentSize);
                                int i = index % (rows * playerComponentSize);
                                return c * playerComponentSize + i + (i / playerComponentSize) * scan;
                            }
                    ));
                } else {
                    super.setPosition(ComponentTablistAccess.createChild(cta, cta.getSize(), index ->
                            (index / rows) + (index % rows) * columns
                    ));
                }
            } else {
                super.setPosition(cta);
            }
        }

        @Override
        public void update2ndStep() {
            activeComponents.forEach(Component.Instance::deactivate);
            activeComponents.clear();
            super.update2ndStep();
            ComponentTablistAccess cta = getTablistAccess();
            if (cta != null) {
                boolean allFit = cta.getSize() >= players.size() * playerComponent.getSize();
                int pos = 0;
                int i;
                for (i = 0; (allFit || pos + playerComponent.getSize() + morePlayersComponent.getSize() <= cta.getSize()) && i < players.size(); i++) {
                    Player player = players.get(i);
                    Component.Instance component = playerComponent.toInstance(context.derived().put(Context.KEY_PLAYER, player).put(Context.KEY_DEFAULT_ICON, IconTemplate.PLAYER_ICON).put(Context.KEY_DEFAULT_PING, PingTemplate.PLAYER_PING));
                    component.activate();
                    component.update1stStep();
                    component.setPosition(ComponentTablistAccess.createChild(cta, playerComponent.getSize(), pos));
                    component.update2ndStep();
                    activeComponents.add(component);
                    pos += playerComponent.getSize();
                }
                if (!allFit) {
                    Component.Instance component = morePlayersComponent.toInstance(context.derived().put(Context.KEY_OTHER_PLAYERS_COUNT, players.size() - i));
                    component.activate();
                    component.update1stStep();
                    component.setPosition(ComponentTablistAccess.createChild(cta, morePlayersComponent.getSize(), pos));
                    component.update2ndStep();
                    activeComponents.add(component);
                }
            }
        }

        @Override
        public int getMinSize() {
            return minSize;
        }

        @Override
        public int getPreferredSize() {
            int size = players.size() * playerComponent.getSize();
            if (fillSlotsVertical) {
                size = size * context.get(Context.KEY_COLUMNS);
            }
            if (maxSize != -1) {
                size = min(maxSize, size);
            }
            return max(minSize, size);
        }

        @Override
        public int getMaxSize() {
            return getPreferredSize();
        }

        @Override
        public boolean isBlockAligned() {
            return fillSlotsVertical;
        }
    }
}
