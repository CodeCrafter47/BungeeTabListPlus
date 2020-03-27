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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.FakePlayerManager;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import com.google.common.collect.ImmutableList;
import de.codecrafter47.taboverlay.config.icon.IconManager;
import de.codecrafter47.taboverlay.config.player.PlayerProvider;
import io.netty.util.concurrent.EventExecutor;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FakePlayerManagerImpl implements FakePlayerManager, PlayerProvider {
    private List<FakePlayer> online = new ArrayList<>();
    private List<String> offline;
    private boolean randomJoinLeaveEventsEnabled;

    private final Set<Listener> listeners = new ReferenceOpenHashSet<>();

    private final Plugin plugin;
    private final IconManager iconManager;
    private final EventExecutor mainThread;

    public FakePlayerManagerImpl(final Plugin plugin, IconManager iconManager, EventExecutor mainThread) {
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
                if (Math.random() < 0.7 && offline.size() > 0) {
                    // add player
                    String name = offline.get((int) (Math.random() * offline.size()));
                    FakePlayer player = createFakePlayer(name, getRandomServer(), true, true);
                    offline.remove(name);
                    online.add(player);
                    listeners.forEach(listener -> listener.onPlayerAdded(player));
                } else if (online.size() > 0) {
                    // remove player
                    FakePlayer fakePlayer = online.get((int) (online.size() * Math.random()));
                    if (BungeeTabListPlus.getInstance().getConfig().fakePlayers.contains(fakePlayer.getName())) {
                        removeFakePlayer(fakePlayer);
                    }
                }
            }
        } catch (Throwable th) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while processing random fake player events", th);
        }
    }

    private static ServerInfo getRandomServer() {
        ArrayList<ServerInfo> servers = new ArrayList<>(ProxyServer.getInstance().getServers().values());
        return servers.get((int) (Math.random() * servers.size()));
    }

    public void removeConfigFakePlayers() {
        Set<String> configFakePlayers = new HashSet<>(BungeeTabListPlus.getInstance().getConfig().fakePlayers);
        mainThread.execute(() -> {
            offline.clear();
            online.removeIf(fakePlayer -> configFakePlayers.contains(fakePlayer.getName()));
        });
    }

    public void reload() {
        mainThread.execute(() -> {
            offline = new ArrayList<>(BungeeTabListPlus.getInstance().getConfig().fakePlayers);
            sanitizeFakePlayerNames();
            for (int i = offline.size(); i > 0; i--) {
                triggerRandomEvent();
            }
        });
    }

    private void sanitizeFakePlayerNames() {
        for (Iterator<?> iterator = offline.iterator(); iterator.hasNext(); ) {
            Object name = iterator.next();
            if (name == null || !(name instanceof String)) {
                plugin.getLogger().warning("Invalid name used for fake player, removing. (" + name + ")");
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
    public Collection<codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer> getOnlineFakePlayers() {
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
    public codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer createFakePlayer(String name, ServerInfo server) {
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
                if (null != fakePlayer.get(BTLPBungeeDataKeys.DATA_KEY_ICON)) {
                    fakePlayer.data.updateValue(BTLPBungeeDataKeys.DATA_KEY_ICON, icon);
                }
            }, mainThread);
        }
        return fakePlayer;
    }

    @Override
    @SneakyThrows
    public void removeFakePlayer(codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer fakePlayer) {
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
