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
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;
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
public class PlayersComponent extends Component {
    private PlayerSorter playerOrder = new PlayerSorter("alphabetically");
    private String playerSet;
    private Component playerComponent;
    private Component morePlayersComponent;
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
            players = context.getPlayers(playerSet);
            playerOrder.sort(context, players);
        }

        @Override
        public void update2ndStep() {
            activeComponents.forEach(Component.Instance::deactivate);
            activeComponents.clear();
            super.update2ndStep();
            boolean allFit = super.size >= players.size() * playerComponent.getSize();
            int pos = 0;
            int i;
            for (i = 0; (allFit || pos + playerComponent.getSize() + morePlayersComponent.getSize() <= super.size) && i < players.size(); i++) {
                Player player = players.get(i);
                Component.Instance component = playerComponent.toInstance(context.derived().setPlayer(player));
                component.activate();
                component.update1stStep();
                component.setPosition(row + (pos / context.getColumns()), column + (pos % context.getColumns()), playerComponent.getSize());
                component.update2ndStep();
                activeComponents.add(component);
                pos += playerComponent.getSize();
            }
            if (!allFit) {
                Component.Instance component = morePlayersComponent.toInstance(context.derived().setOtherPlayersCount(players.size() - i));
                component.activate();
                component.update1stStep();
                component.setPosition(row + (pos / context.getColumns()), column + (pos % context.getColumns()), morePlayersComponent.getSize());
                component.update2ndStep();
                activeComponents.add(component);
            }
        }

        @Override
        public int getMinSize() {
            return minSize;
        }

        @Override
        public int getPreferredSize() {
            int size = players.size() * playerComponent.getSize();
            if (maxSize != -1) {
                size = min(maxSize, size);
            }
            return max(minSize, size);
        }

        @Override
        public int getMaxSize() {
            return maxSize == -1 ? getPreferredSize() : maxSize;
        }

        @Override
        public boolean isBlockAligned() {
            return false;
        }
    }
}
