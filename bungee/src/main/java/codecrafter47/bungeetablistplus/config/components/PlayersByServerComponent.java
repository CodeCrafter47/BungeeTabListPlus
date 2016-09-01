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
import com.google.common.base.Preconditions;
import lombok.Data;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.*;

import static java.lang.Math.min;

@Data
public class PlayersByServerComponent extends Component {
    private PlayerSorter playerOrder = new PlayerSorter("alphabetically");
    private String playerSet;
    private Component serverHeader;
    private boolean includeEmptyServers;
    private Component playerComponent;
    private Component morePlayersComponent;
    int minSizePerServer = 0;
    int maxSizePerServer = 200;
    int minSize = 0;
    int maxSize = -1;

    public void setServerHeader(Component serverHeader) {
        Preconditions.checkArgument(serverHeader.hasConstantSize(), "serverHeader needs to have a fixed size.");
        this.serverHeader = serverHeader;
    }

    public void setPlayerComponent(Component playerComponent) {
        Preconditions.checkArgument(playerComponent.hasConstantSize(), "playerComponent needs to have a fixed size.");
        this.playerComponent = playerComponent;
    }

    public void setMorePlayersComponent(Component morePlayersComponent) {
        Preconditions.checkArgument(morePlayersComponent.hasConstantSize(), "morePlayersComponent needs to have a fixed size.");
        this.morePlayersComponent = morePlayersComponent;
    }

    public void setMinSizePerServer(int minSizePerServer) {
        Preconditions.checkArgument(minSizePerServer <= maxSizePerServer, "minSizePerServer needs to be smaller than maxSizePerServer.");
        this.minSizePerServer = minSizePerServer;
    }

    public void setMaxSizePerServer(int maxSizePerServer) {
        Preconditions.checkArgument(minSizePerServer <= maxSizePerServer, "minSizePerServer needs to be smaller than maxSizePerServer.");
        this.maxSizePerServer = maxSizePerServer;
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
    public boolean hasConstantSize() {
        return false;
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context);
    }

    public class Instance extends Component.Instance {

        private List<Component.Instance> activeComponents = new ArrayList<>();
        private Map<String, List<Player>> playersByServer = new LinkedHashMap<>();
        private int preferredSize;

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
            playersByServer.clear();
            if (includeEmptyServers) {
                for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                    playersByServer.put(server.getName(), new ArrayList<>());
                }
            }
            for (Player player : context.getPlayers(playerSet)) {
                Optional<String> server = player.get(BungeeTabListPlus.DATA_KEY_SERVER);
                if (server.isPresent()) {
                    playersByServer.computeIfAbsent(server.get(), s -> new ArrayList<>()).add(player);
                }
            }
            for (List<Player> list : playersByServer.values()) {
                playerOrder.sort(context, list);
            }

            preferredSize = 0;
            for (List<Player> list : playersByServer.values()) {
                int serverSize = serverHeader.getSize() + list.size() * playerComponent.getSize();
                serverSize = ((serverSize + context.getColumns() - 1) / context.getColumns()) * context.getColumns();
                preferredSize += min(serverSize, maxSizePerServer);
            }
            if (maxSize != -1) {
                preferredSize = min(preferredSize, PlayersByServerComponent.this.maxSize);
            }
        }

        @Override
        public void update2ndStep() {
            activeComponents.forEach(Component.Instance::deactivate);
            activeComponents.clear();
            super.update2ndStep();
            // figure out how much space each server gets
            int rows = size / context.getColumns();
            int minRowsPerServer = (minSizePerServer + context.getColumns() - 1) / context.getColumns();
            int maxRowsPerServer = maxSizePerServer / context.getColumns();
            List<String> servers = new ArrayList<>(playersByServer.keySet());
            // todo make server order configurable
            Collections.sort(servers);
            int[] serverRows = new int[servers.size()];
            Arrays.fill(serverRows, minRowsPerServer);
            rows -= minRowsPerServer * servers.size();
            boolean change;
            do {
                change = false;
                for (int i = 0; i < servers.size(); i++) {
                    if (rows > 0 && serverRows[i] < maxRowsPerServer) {
                        String server = servers.get(i);
                        int serverSize = serverHeader.getSize() + playersByServer.get(server).size() * playerComponent.getSize();
                        if (min(serverSize, maxRowsPerServer) > serverRows[i] * context.getColumns()) {
                            serverRows[i]++;
                            change = true;
                            rows--;
                        }
                    }
                }
            } while (rows > 0 && change);
            // create the components
            int pos = 0;
            for (int i = 0; i < servers.size() && pos < size; i++) {
                String server = servers.get(i);
                pos = ((pos + context.getColumns() - 1) / context.getColumns()) * context.getColumns();
                List<Player> players = playersByServer.get(server);
                // Header
                Context serverContext = context.derived().setServer(server).setServerPlayerCount(players.size());
                Component.Instance header = serverHeader.toInstance(serverContext);
                header.activate();
                header.update1stStep();
                header.setPosition(row + (pos / context.getColumns()), column, serverHeader.getSize());
                header.update2ndStep();
                activeComponents.add(header);
                pos += serverHeader.getSize();
                // Players
                int playersMaxSize = playersByServer.get(server).size() * playerComponent.getSize();
                int serverSize = min(serverRows[i] * context.getColumns() - serverHeader.getSize(), playersMaxSize);
                boolean allFit = serverSize >= playersMaxSize;
                int j;
                int pos2 = 0;
                for (j = 0; (allFit || pos2 + morePlayersComponent.getSize() < serverSize) && j < players.size(); j++) {
                    Player player = players.get(j);
                    Component.Instance component = playerComponent.toInstance(serverContext.derived().setPlayer(player));
                    component.activate();
                    component.update1stStep();
                    component.setPosition(row + ((pos + pos2) / context.getColumns()), column + ((pos + pos2) % context.getColumns()), playerComponent.getSize());
                    component.update2ndStep();
                    activeComponents.add(component);
                    pos2 += playerComponent.getSize();
                }
                if (!allFit) {
                    Component.Instance component = morePlayersComponent.toInstance(serverContext.derived().setOtherPlayersCount(players.size() - j));
                    component.activate();
                    component.update1stStep();
                    component.setPosition(row + ((pos + pos2) / context.getColumns()), column + ((pos + pos2) % context.getColumns()), morePlayersComponent.getSize());
                    component.update2ndStep();
                    activeComponents.add(component);
                    pos2 += morePlayersComponent.getSize();
                }
                pos += pos2;
            }
        }

        @Override
        public int getMinSize() {
            return minSize;
        }

        @Override
        public int getPreferredSize() {
            return preferredSize;
        }

        @Override
        public int getMaxSize() {
            return preferredSize;
        }

        @Override
        public boolean isBlockAligned() {
            return true;
        }
    }
}
