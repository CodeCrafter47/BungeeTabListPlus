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
import codecrafter47.bungeetablistplus.template.IconTemplate;
import codecrafter47.bungeetablistplus.template.PingTemplate;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import de.codecrafter47.data.bungee.api.BungeeData;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;

@Getter
@Setter
public class PlayersByServerComponent extends Component implements Validate {
    private PlayerSorter playerOrder = new PlayerSorter("alphabetically");
    private String serverOrder = "alphabetically";
    private ServerOptions showServers = null;
    private String playerSet;
    private Component serverHeader;
    private Component serverFooter;
    private Component serverSeparator;
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

    public void setServerFooter(Component serverFooter) {
        if (serverFooter != null) {
            Preconditions.checkArgument(serverFooter.hasConstantSize(), "serverFooter needs to have a fixed size.");
        }
        this.serverFooter = serverFooter;
    }

    public void setServerSeparator(Component serverSeparator) {
        if (serverSeparator != null) {
            Preconditions.checkArgument(serverSeparator.hasConstantSize(), "serverSeparator needs to have a fixed size.");
        }
        this.serverSeparator = serverSeparator;
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
        if (showServers == null) {
            showServers = includeEmptyServers ? ServerOptions.ALL : ServerOptions.NON_EMPTY;
        }
        return new Instance(context);
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull(playerSet, "playerSet is null");
        Preconditions.checkNotNull(serverHeader, "serverHeader is null");
        Preconditions.checkNotNull(playerComponent, "playerComponent is null");
        Preconditions.checkNotNull(morePlayersComponent, "morePlayersComponent is null");
    }

    public class Instance extends Component.Instance {

        private List<Component.Instance> activeComponents = new ArrayList<>();
        private Map<String, List<Player>> playersByServer = new LinkedHashMap<>();
        private List<String> sortedServerList;
        private Ordering<String> serverComparator;
        private int preferredSize;

        protected Instance(Context context) {
            super(context);
            List<Comparator<String>> list = Stream.of(serverOrder)
                    .filter(Objects::nonNull)
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .filter((s1) -> !s1.isEmpty())
                    .map(String::toLowerCase)
                    .map(this::getServerComparator)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (list.isEmpty()) {
                serverComparator = null;
            } else {
                serverComparator = Ordering.compound(list);
            }
        }

        private Comparator<String> getServerComparator(String rule) {
            switch (rule) {
                case "alphabetically":
                    return Comparator.naturalOrder();
                case "playercount":
                    return Comparator.comparing(server -> playersByServer.get(server).size(), Comparator.reverseOrder());
                case "online":
                    return Comparator.comparing(server -> BungeeTabListPlus.getInstance().getServerState(server).isOnline(), Comparator.reverseOrder());
                default:
                    return null;
            }
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
            if (showServers == ServerOptions.ALL) {
                for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                    playersByServer.put(server.getName(), new ArrayList<>());
                }
            }
            if (showServers == ServerOptions.ONLINE) {
                for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                    if (BungeeTabListPlus.getInstance().getServerState(server.getName()).isOnline()) {
                        playersByServer.put(server.getName(), new ArrayList<>());
                    }
                }
            }
            List<Player> players = context.get(Context.KEY_PLAYER_SETS).get(playerSet);
            if (players == null) {
                players = Collections.emptyList();
                BungeeTabListPlus.getInstance().getLogger().info("Missing player set " + playerSet);
            }
            for (Player player : players) {
                Optional<String> server = player.getOpt(BungeeData.BungeeCord_Server);
                if (server.isPresent()) {
                    playersByServer.computeIfAbsent(server.get(), s -> new ArrayList<>()).add(player);
                }
            }
            for (List<Player> list : playersByServer.values()) {
                playerOrder.sort(context, list);
            }

            preferredSize = 0;
            for (List<Player> list : playersByServer.values()) {
                int serverSize = serverHeader.getSize() + list.size() * playerComponent.getSize() + (serverFooter != null ? serverFooter.getSize() : 0);
                serverSize = ((serverSize + context.get(Context.KEY_COLUMNS) - 1) / context.get(Context.KEY_COLUMNS)) * context.get(Context.KEY_COLUMNS);
                preferredSize += min(serverSize, maxSizePerServer);
            }
            if (serverSeparator != null && playersByServer.size() > 1) {
                preferredSize += ((serverSeparator.getSize() + context.get(Context.KEY_COLUMNS) - 1) / context.get(Context.KEY_COLUMNS)) * context.get(Context.KEY_COLUMNS) * (playersByServer.size() - 1);
            }
            if (maxSize != -1) {
                preferredSize = min(preferredSize, PlayersByServerComponent.this.maxSize);
            }

            if (serverComparator != null) {
                sortedServerList = serverComparator.immutableSortedCopy(playersByServer.keySet());
            } else {
                sortedServerList = ImmutableList.copyOf(playersByServer.keySet());
            }
        }

        @Override
        public void update2ndStep() {
            activeComponents.forEach(Component.Instance::deactivate);
            activeComponents.clear();
            super.update2ndStep();
            // figure out how much space each server gets
            int rows = size / context.get(Context.KEY_COLUMNS);
            int minRowsPerServer = (minSizePerServer + context.get(Context.KEY_COLUMNS) - 1) / context.get(Context.KEY_COLUMNS);
            int maxRowsPerServer = maxSizePerServer / context.get(Context.KEY_COLUMNS);
            List<String> servers = sortedServerList;
            int[] serverRows = new int[servers.size()];
            Arrays.fill(serverRows, minRowsPerServer);
            rows -= minRowsPerServer * servers.size();
            if (serverSeparator != null && playersByServer.size() > 1) {
                rows -= ((serverSeparator.getSize() + context.get(Context.KEY_COLUMNS) - 1) / context.get(Context.KEY_COLUMNS)) * (playersByServer.size() - 1);
            }
            boolean change;
            do {
                change = false;
                for (int i = 0; i < servers.size(); i++) {
                    if (rows > 0 && serverRows[i] < maxRowsPerServer) {
                        String server = servers.get(i);
                        int serverSize = serverHeader.getSize() + playersByServer.get(server).size() * playerComponent.getSize() + (serverFooter != null ? serverFooter.getSize() : 0);
                        if (min(serverSize, maxRowsPerServer) > serverRows[i] * context.get(Context.KEY_COLUMNS)) {
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
                if (serverSeparator != null && i > 0) {
                    // Separator
                    pos = ((pos + context.get(Context.KEY_COLUMNS) - 1) / context.get(Context.KEY_COLUMNS)) * context.get(Context.KEY_COLUMNS);
                    Component.Instance separator = serverSeparator.toInstance(context);
                    separator.activate();
                    separator.update1stStep();
                    separator.setPosition(leftMostColumn, row + (pos / context.get(Context.KEY_COLUMNS)), column, serverSeparator.getSize());
                    separator.update2ndStep();
                    activeComponents.add(separator);
                    pos += serverSeparator.getSize();
                }
                String server = servers.get(i);
                pos = ((pos + context.get(Context.KEY_COLUMNS) - 1) / context.get(Context.KEY_COLUMNS)) * context.get(Context.KEY_COLUMNS);
                List<Player> players = playersByServer.get(server);
                // Header
                Context serverContext = context.derived().put(Context.KEY_SERVER, server).put(Context.KEY_SERVER_PLAYER_COUNT, players.size());
                Component.Instance header = serverHeader.toInstance(serverContext);
                header.activate();
                header.update1stStep();
                header.setPosition(leftMostColumn, row + (pos / context.get(Context.KEY_COLUMNS)), column, serverHeader.getSize());
                header.update2ndStep();
                activeComponents.add(header);
                pos += serverHeader.getSize();
                // Players
                int playersMaxSize = playersByServer.get(server).size() * playerComponent.getSize();
                int serverSize = min(serverRows[i] * context.get(Context.KEY_COLUMNS) - serverHeader.getSize() - (serverFooter != null ? serverFooter.getSize() : 0), playersMaxSize);
                boolean allFit = serverSize >= playersMaxSize;
                int j;
                int pos2 = 0;
                for (j = 0; (allFit || pos2 + morePlayersComponent.getSize() < serverSize) && j < players.size(); j++) {
                    Player player = players.get(j);
                    Component.Instance component = playerComponent.toInstance(serverContext.derived().put(Context.KEY_PLAYER, player).put(Context.KEY_DEFAULT_ICON, IconTemplate.PLAYER_ICON).put(Context.KEY_DEFAULT_PING, PingTemplate.PLAYER_PING));
                    component.activate();
                    component.update1stStep();
                    component.setPosition(leftMostColumn, row + ((pos + pos2) / context.get(Context.KEY_COLUMNS)), leftMostColumn + ((pos + pos2) % context.get(Context.KEY_COLUMNS)), playerComponent.getSize());
                    component.update2ndStep();
                    activeComponents.add(component);
                    pos2 += playerComponent.getSize();
                }
                if (!allFit) {
                    Component.Instance component = morePlayersComponent.toInstance(serverContext.derived().put(Context.KEY_OTHER_PLAYERS_COUNT, players.size() - j));
                    component.activate();
                    component.update1stStep();
                    component.setPosition(leftMostColumn, row + ((pos + pos2) / context.get(Context.KEY_COLUMNS)), leftMostColumn + ((pos + pos2) % context.get(Context.KEY_COLUMNS)), morePlayersComponent.getSize());
                    component.update2ndStep();
                    activeComponents.add(component);
                    pos2 += morePlayersComponent.getSize();
                }
                // Footer
                if (serverFooter != null) {
                    pos += serverSize;
                    Component.Instance footer = serverFooter.toInstance(serverContext);
                    footer.activate();
                    footer.update1stStep();
                    footer.setPosition(leftMostColumn, row + (pos / context.get(Context.KEY_COLUMNS)), leftMostColumn + (pos % context.get(Context.KEY_COLUMNS)), serverFooter.getSize());
                    footer.update2ndStep();
                    activeComponents.add(footer);
                    pos += serverFooter.getSize();
                } else {
                    pos += pos2;
                }
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

    public enum ServerOptions {
        ALL, ONLINE, NON_EMPTY
    }
}
