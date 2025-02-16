/*
 *     Copyright (C) 2025 proferabg
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.velocity.FakePlayerManager;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.util.ProxyServer;
import codecrafter47.bungeetablistplus.util.VelocityPlugin;
import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.codecrafter47.taboverlay.config.icon.IconManager;
import de.codecrafter47.taboverlay.config.player.PlayerProvider;
import io.netty.util.concurrent.EventExecutor;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.SneakyThrows;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FakePlayerManagerImpl implements FakePlayerManager, PlayerProvider {
    private List<FakePlayer> online = new ArrayList<>();
    private List<String> offline;
    private boolean randomJoinLeaveEventsEnabled;

    private final Set<Listener> listeners = new ReferenceOpenHashSet<>();

    private final VelocityPlugin plugin;
    private final IconManager iconManager;
    private final EventExecutor mainThread;

    public FakePlayerManagerImpl(final VelocityPlugin plugin, IconManager iconManager, EventExecutor mainThread) {
        this.plugin = plugin;
        this.iconManager = iconManager;
        this.mainThread = mainThread;

        randomJoinLeaveEventsEnabled = true;
        if (BungeeTabListPlus.getInstance().getConfig().fakePlayers.size() > 0) {
            offline = new ArrayList<>(BungeeTabListPlus.getInstance().getConfig().fakePlayers);
            sanitizeFakePlayerNames();
        } else {
            offline = new ArrayList<>();
        }
        mainThread.scheduleAtFixedRate(this::triggerRandomEvent, 10, 10, TimeUnit.SECONDS);
    }

    private void triggerRandomEvent() {
        try {
            if (Math.random() <= 0.5 && online.size() > 0) {
                // do a server switch
                FakePlayer player = online.get((int) (Math.random() * online.size()));
                if (player.isRandomServerSwitchEnabled()) {
                    player.changeServer(getRandomServer());
                }
            } else if (randomJoinLeaveEventsEnabled) {
                if (Math.random() < 0.8 && offline.size() > 0) {
                    // add player
                    String name = offline.remove((int) (Math.random() * offline.size()));
                    createFakePlayer(name, getRandomServer(), true, true);
                } else if (online.size() > 0) {
                    // remove player
                    FakePlayer fakePlayer = online.get((int) (online.size() * Math.random()));
                    if (BungeeTabListPlus.getInstance().getConfig().fakePlayers.contains(fakePlayer.getName())) {
                        removeFakePlayer(fakePlayer);
                    }
                }
            }
        } catch (Throwable th) {
            plugin.getLogger().error("An error occurred while processing random fake player events", th);
        }
    }

    private static ServerInfo getRandomServer() {
        ArrayList<ServerInfo> servers = new ArrayList<>();
        for(RegisteredServer server : ProxyServer.getInstance().getAllServers()){
            servers.add(server.getServerInfo());
        }
        return servers.get((int) (Math.random() * servers.size()));
    }

    public void removeConfigFakePlayers() {
        Set<String> configFakePlayers = new HashSet<>(BungeeTabListPlus.getInstance().getConfig().fakePlayers);
        mainThread.execute(() -> {
            offline.clear();
            for (FakePlayer fakePlayer : ImmutableList.copyOf(online)) {
                if (configFakePlayers.contains(fakePlayer.getName())) {
                    removeFakePlayer(fakePlayer);
                }
            }
        });
    }

    public void reload() {
        mainThread.execute(() -> {
            offline = new ArrayList<>(BungeeTabListPlus.getInstance().getConfig().fakePlayers);
            sanitizeFakePlayerNames();
            for (int i = offline.size() * 4; i > 0; i--) {
                triggerRandomEvent();
            }
        });
    }

    private void sanitizeFakePlayerNames() {
        for (Iterator<?> iterator = offline.iterator(); iterator.hasNext(); ) {
            Object name = iterator.next();
            if (!(name instanceof String)) {
                plugin.getLogger().warn("Invalid name used for fake player, removing. (" + name + ")");
                iterator.remove();
            }
        }
    }

    @Override
    @SneakyThrows
    public Collection<FakePlayer> getPlayers() {
        if (!mainThread.inEventLoop()) {
            return mainThread.submit(this::getPlayers).get();
        }
        return ImmutableList.copyOf(online);
    }

    @Override
    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    @SneakyThrows
    public Collection<codecrafter47.bungeetablistplus.api.velocity.tablist.FakePlayer> getOnlineFakePlayers() {
        if (!mainThread.inEventLoop()) {
            return mainThread.submit(this::getOnlineFakePlayers).get();
        }
        return ImmutableList.copyOf(online);
    }

    @Override
    public boolean isRandomJoinLeaveEnabled() {
        return randomJoinLeaveEventsEnabled;
    }

    @Override
    public void setRandomJoinLeaveEnabled(boolean value) {
        this.randomJoinLeaveEventsEnabled = value;
    }

    @Override
    @SneakyThrows
    public codecrafter47.bungeetablistplus.api.velocity.tablist.FakePlayer createFakePlayer(String name, ServerInfo server) {
        return createFakePlayer(name, server, false, false);
    }

    @SneakyThrows
    public FakePlayer createFakePlayer(String name, ServerInfo server, boolean randomServerSwitch, boolean skinFromName) {
        if (!mainThread.inEventLoop()) {
            return mainThread.submit(() -> createFakePlayer(name, server, randomServerSwitch, skinFromName)).get();
        }
        FakePlayer fakePlayer = new FakePlayer(name, server, randomServerSwitch, mainThread);
        online.add(fakePlayer);
        listeners.forEach(listener -> listener.onPlayerAdded(fakePlayer));
        if (skinFromName) {
            iconManager.createIconFromName(fakePlayer.getName()).thenAcceptAsync(icon -> {
                if (null != fakePlayer.get(BTLPVelocityDataKeys.DATA_KEY_ICON)) {
                    fakePlayer.data.updateValue(BTLPVelocityDataKeys.DATA_KEY_ICON, icon);
                }
            }, mainThread);
        }
        return fakePlayer;
    }

    @Override
    @SneakyThrows
    public void removeFakePlayer(codecrafter47.bungeetablistplus.api.velocity.tablist.FakePlayer fakePlayer) {
        if (!mainThread.inEventLoop()) {
            mainThread.submit(() -> removeFakePlayer(fakePlayer)).sync();
            return;
        }
        FakePlayer player = (FakePlayer) fakePlayer;
        if (online.remove(player)) {
            if (BungeeTabListPlus.getInstance().getConfig().fakePlayers.contains(player.getName())) {
                offline.add(player.getName());
            }
            listeners.forEach(listener -> listener.onPlayerRemoved(player));
        }
    }
}
