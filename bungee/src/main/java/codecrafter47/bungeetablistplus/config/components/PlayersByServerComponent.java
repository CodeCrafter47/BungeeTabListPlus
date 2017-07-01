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
import codecrafter47.bungeetablistplus.util.ContextAwareOrdering;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.codecrafter47.data.bungee.api.BungeeData;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;

@Getter
@Setter
public class PlayersByServerComponent extends Component implements Validate {
    private PlayerSorter playerOrder = new PlayerSorter("alphabetically");
    private String serverOrder = "alphabetically";
    private Map<String, Integer> customServerOrder = ImmutableMap.of();
    private ServerOptions showServers = null;
    private String playerSet;
    private Component serverHeader;
    private Component serverFooter;
    private Component serverSeparator;
    private boolean includeEmptyServers;
    private Component playerComponent;
    private Component morePlayersComponent;
    private boolean fillSlotsVertical = false;
    int minSizePerServer = 0;
    int maxSizePerServer = 200;
    int minSize = 0;
    int maxSize = -1;
    private List<String> hiddenServers = null;

    public List<String> getCustomServerOrder() {
        // dummy method for snakeyaml to detect property type
        return null;
    }

    public void setCustomServerOrder(List<String> customServerOrder) {
        Preconditions.checkNotNull(customServerOrder);
        val builder = ImmutableMap.<String, Integer>builder();
        int rank = 0;
        for (String server : customServerOrder) {
            builder.put(server, rank++);
        }
        this.customServerOrder = builder.build();
    }

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
        private ContextAwareOrdering<Context, String> serverComparator;
        private int preferredSize;

        protected Instance(Context context) {
            super(context);
            List<ContextAwareOrdering<Context, String>> list = Stream.of(serverOrder)
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
                serverComparator = ContextAwareOrdering.compound(list);
            }
        }

        private ContextAwareOrdering<Context, String> getServerComparator(String rule) {
            switch (rule) {
                case "alphabetically":
                    return ContextAwareOrdering.from(Comparator.<String>naturalOrder());
                case "playercount":
                    return ContextAwareOrdering.from(Comparator.comparing(server -> playersByServer.get(server).size(), Comparator.reverseOrder()));
                case "online":
                    return ContextAwareOrdering.from(Comparator.comparing(server -> BungeeTabListPlus.getInstance().getServerState(server).isOnline(), Comparator.reverseOrder()));
                case "custom":
                    return ContextAwareOrdering.from(Comparator.comparing(server -> customServerOrder.getOrDefault(server, Integer.MAX_VALUE)));
                case "yourserverfirst":
                    return new ContextAwareOrdering<Context, String>() {
                        @Override
                        public int compare(Context context, String first, String second) {
                            String server = context.get(Context.KEY_VIEWER).get(BungeeData.BungeeCord_Server);
                            if (first.equals(server)) {
                                return -1;
                            } else if (second.equals(server)) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    };
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
            if (hiddenServers != null) {
                for (String hiddenServer : hiddenServers) {
                    playersByServer.remove(hiddenServer);
                }
            }
            for (List<Player> list : playersByServer.values()) {
                playerOrder.sort(context, list);
            }

            int columns = fillSlotsVertical ? 1 : context.get(Context.KEY_COLUMNS);
            preferredSize = 0;
            for (List<Player> list : playersByServer.values()) {
                int serverSize = serverHeader.getSize() + list.size() * playerComponent.getSize() + (serverFooter != null ? serverFooter.getSize() : 0);
                serverSize = ((serverSize + columns - 1) / columns) * columns;
                preferredSize += min(serverSize, maxSizePerServer);
            }
            if (serverSeparator != null && playersByServer.size() > 1) {
                preferredSize += ((serverSeparator.getSize() + columns - 1) / columns) * columns * (playersByServer.size() - 1);
            }

            if (fillSlotsVertical) {
                preferredSize = preferredSize * context.get(Context.KEY_COLUMNS);
            }

            if (maxSize != -1) {
                preferredSize = min(preferredSize, PlayersByServerComponent.this.maxSize);
            }

            if (serverComparator != null) {
                sortedServerList = serverComparator.immutableSortedCopy(context, playersByServer.keySet());
            } else {
                sortedServerList = ImmutableList.copyOf(playersByServer.keySet());
            }
        }

        @Override
        public void setPosition(ComponentTablistAccess cta) {
            if (fillSlotsVertical) {
                int columns = context.get(Context.KEY_COLUMNS);
                int rows = (cta.getSize() + columns - 1) / columns;
                super.setPosition(ComponentTablistAccess.createChild(cta, cta.getSize(), index ->
                        (index / rows) + (index % rows) * columns
                ));
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
                // figure out how much space each server gets
                int columns = fillSlotsVertical ? 1 : context.get(Context.KEY_COLUMNS);
                int rows = cta.getSize() / columns;
                int minRowsPerServer = (minSizePerServer + columns - 1) / columns;
                int maxRowsPerServer = maxSizePerServer / columns;
                List<String> servers = sortedServerList;
                int[] serverRows = new int[servers.size()];
                Arrays.fill(serverRows, minRowsPerServer);
                rows -= minRowsPerServer * servers.size();
                if (serverSeparator != null && playersByServer.size() > 1) {
                    rows -= ((serverSeparator.getSize() + columns - 1) / columns) * (playersByServer.size() - 1);
                }
                boolean change;
                do {
                    change = false;
                    for (int i = 0; i < servers.size(); i++) {
                        if (rows > 0 && serverRows[i] < maxRowsPerServer) {
                            String server = servers.get(i);
                            int serverSize = serverHeader.getSize() + playersByServer.get(server).size() * playerComponent.getSize() + (serverFooter != null ? serverFooter.getSize() : 0);
                            if (min(serverSize, maxRowsPerServer) > serverRows[i] * columns) {
                                serverRows[i]++;
                                change = true;
                                rows--;
                            }
                        }
                    }
                } while (rows > 0 && change);
                // create the components
                int pos = 0;
                for (int i = 0; i < servers.size() && pos < cta.getSize(); i++) {
                    if (serverSeparator != null && i > 0) {
                        // Separator
                        pos = ((pos + columns - 1) / columns) * columns;
                        Component.Instance separator = serverSeparator.toInstance(context);
                        separator.activate();
                        separator.update1stStep();
                        separator.setPosition(ComponentTablistAccess.createChild(cta, serverSeparator.getSize(), pos));
                        separator.update2ndStep();
                        activeComponents.add(separator);
                        pos += serverSeparator.getSize();
                    }
                    String server = servers.get(i);
                    pos = ((pos + columns - 1) / columns) * columns;
                    List<Player> players = playersByServer.get(server);
                    // Header
                    Context serverContext = context.derived().put(Context.KEY_SERVER, server).put(Context.KEY_SERVER_PLAYER_COUNT, players.size());
                    Component.Instance header = serverHeader.toInstance(serverContext);
                    header.activate();
                    header.update1stStep();
                    header.setPosition(ComponentTablistAccess.createChild(cta, serverHeader.getSize(), pos));
                    header.update2ndStep();
                    activeComponents.add(header);
                    pos += serverHeader.getSize();
                    // Players
                    int playersMaxSize = playersByServer.get(server).size() * playerComponent.getSize();
                    int serverSize = min(serverRows[i] * columns - serverHeader.getSize() - (serverFooter != null ? serverFooter.getSize() : 0), playersMaxSize);
                    boolean allFit = serverSize >= playersMaxSize;
                    int j;
                    int pos2 = 0;
                    for (j = 0; (allFit || pos2 + morePlayersComponent.getSize() < serverSize) && j < players.size(); j++) {
                        Player player = players.get(j);
                        Component.Instance component = playerComponent.toInstance(serverContext.derived().put(Context.KEY_PLAYER, player).put(Context.KEY_DEFAULT_ICON, IconTemplate.PLAYER_ICON).put(Context.KEY_DEFAULT_PING, PingTemplate.PLAYER_PING));
                        component.activate();
                        component.update1stStep();
                        component.setPosition(ComponentTablistAccess.createChild(cta, playerComponent.getSize(), pos + pos2));
                        component.update2ndStep();
                        activeComponents.add(component);
                        pos2 += playerComponent.getSize();
                    }
                    if (!allFit) {
                        Component.Instance component = morePlayersComponent.toInstance(serverContext.derived().put(Context.KEY_OTHER_PLAYERS_COUNT, players.size() - j));
                        component.activate();
                        component.update1stStep();
                        component.setPosition(ComponentTablistAccess.createChild(cta, morePlayersComponent.getSize(), pos + pos2));
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
                        footer.setPosition(ComponentTablistAccess.createChild(cta, serverFooter.getSize(), pos));
                        footer.update2ndStep();
                        activeComponents.add(footer);
                        pos += serverFooter.getSize();
                    } else {
                        pos += pos2;
                    }
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
