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
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class FakePlayerManagerImpl implements IPlayerProvider, FakePlayerManager {
    private List<FakePlayer> online = new CopyOnWriteArrayList<>();
    private List<String> offline = new ArrayList<>();
    private final Plugin plugin;
    private boolean randomJoinLeaveEventsEnabled = false;

    public FakePlayerManagerImpl(final Plugin plugin) {
        this.plugin = plugin;
        if (BungeeTabListPlus.getInstance().getConfig().fakePlayers.size() > 0) {
            randomJoinLeaveEventsEnabled = true;
            offline = new ArrayList<>(BungeeTabListPlus.getInstance().getConfig().fakePlayers);
            sanitizeFakePlayerNames();
            plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    triggerRandomEvent();
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    }

    private void triggerRandomEvent() {
        try {
            if (Math.random() < 0.3 * online.size()) {
                // do a server switch
                FakePlayer player = online.get((int) (Math.random() * online.size()));
                if (player.isRandomServerSwitchEnabled()) {
                    player.server = new ArrayList<>(plugin.getProxy().getServers().values()).get((int) (Math.random() * plugin.getProxy().getServers().values().size()));
                }
            }
            if (randomJoinLeaveEventsEnabled) {
                if (Math.random() < 0.7 && offline.size() > 0) {
                    // add player
                    String name = offline.get((int) (Math.random() * offline.size()));
                    FakePlayer player = new FakePlayer(name, new ArrayList<>(plugin.getProxy().getServers().values()).get((int) (Math.random() * plugin.getProxy().getServers().values().size())), true);
                    offline.remove(name);
                    online.add(player);
                } else if (online.size() > 0) {
                    // remove player
                    offline.add(online.remove((int) (online.size() * Math.random())).getName());
                }
            }
        } catch (Throwable th) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while processing random fake player events", th);
        }
    }

    public void reload() {
        offline = new ArrayList<>(BungeeTabListPlus.getInstance().getConfig().fakePlayers);
        sanitizeFakePlayerNames();
        online = new CopyOnWriteArrayList<>();
        for (int i = offline.size(); i > 0; i--) {
            triggerRandomEvent();
        }
    }

    private void sanitizeFakePlayerNames() {
        for (Iterator<?> iterator = offline.iterator(); iterator.hasNext(); ) {
            Object name = iterator.next();
            if (name == null || !(name instanceof String)) {
                plugin.getLogger().warning("Invalid name used for fake player, removing. (" + Objects.toString(name) + ")");
                iterator.remove();
            }
        }
    }

    @Override
    public Collection<IPlayer> getPlayers() {
        return Collections.unmodifiableCollection(online);
    }

    @Override
    public Collection<codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer> getOnlineFakePlayers() {
        return Collections.unmodifiableCollection(online);
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
    public codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer createFakePlayer(String name, ServerInfo server) {
        FakePlayer fakePlayer = new FakePlayer(name, server, false);
        online.add(fakePlayer);
        return fakePlayer;
    }

    @Override
    public void removeFakePlayer(codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer fakePlayer) {
        FakePlayer player = (FakePlayer) fakePlayer;
        if (online.remove(player)) {
            if (BungeeTabListPlus.getInstance().getConfig().fakePlayers.contains(player.getName())) {
                offline.add(player.getName());
            }
        }
    }
}
