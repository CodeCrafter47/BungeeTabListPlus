/*
 * Copyright (C) 2014 Florian Stober
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
package codecrafter47.bungeetablistplus.bukkitbridge;

import codecrafter47.bungeetablistplus.bukkitbridge.api.BungeeTabListPlusBukkitBridge;
import codecrafter47.bungeetablistplus.bukkitbridge.api.GeneralInformationProvider;
import codecrafter47.bungeetablistplus.bukkitbridge.api.PlayerInformationProvider;
import codecrafter47.bungeetablistplus.bukkitbridge.informationhooks.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 *
 * @author Florian Stober
 */
public class BukkitBridge extends JavaPlugin implements Listener,
        BungeeTabListPlusBukkitBridge {

    GeneralInformationUpdateTask generalInformationUpdateTask = null;

    Map<Player, PlayerInformationUpdateTask> playerInformationUpdaters = new ConcurrentHashMap<>();

    Collection<GeneralInformationProvider> generalInformationProviders = new HashSet<>();

    Collection<PlayerInformationProvider> playerInformationProviders = new HashSet<>();

    Map<Plugin, Collection<GeneralInformationProvider>> pluginsGeneralInformationProviders = new HashMap<>();

    Map<Plugin, Collection<PlayerInformationProvider>> pluginsPlayerInformationProviders = new HashMap<>();

    private boolean useAsyncThreads = true;

    @Override
    public void onEnable() {
        super.saveResource("config.yml", false);

        useAsyncThreads = super.getConfig().getBoolean("useAsyncThreads",
                useAsyncThreads);

        getServer().getMessenger().registerOutgoingPluginChannel(this,
                Constants.channel);
        getServer().getMessenger().registerIncomingPluginChannel(this,
                Constants.channel, new PluginMessageListener() {

                    @Override
                    public void onPluginMessageReceived(String string,
                            Player player, byte[] bytes) {
                        try {
                            DataInputStream in = new DataInputStream(
                                    new ByteArrayInputStream(bytes));

                            String subchannel = in.readUTF();
                            if (subchannel.equals(Constants.subchannel_init)) {
                                reinitialize();
                            }
                            if (subchannel.equals(
                                    Constants.subchannel_initplayer)) {
                                addPlayer(player);
                            }
                        } catch (IOException ex) {
                            reinitialize();
                        }
                    }
                });
        getServer().getPluginManager().registerEvents(this, this);

        // register own informationhooks
        // Hook VanishNoPackets
        Plugin vanishNoPackets = getServer().getPluginManager().getPlugin(
                "VanishNoPacket");
        if (vanishNoPackets != null) {
            getLogger().info("hooked VanishNoPacket");
            this.registerPlayerInformationProvider(vanishNoPackets,
                    new VanishNoPacketHook(this));
        }

        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            getLogger().info("hooked Vault");
            this.registerPlayerInformationProvider(vault, new VaultHook(this));
        }

        Plugin factions = getServer().getPluginManager().getPlugin("Factions");
        if (factions != null) {
            getLogger().info("hooked Factions");
            // new factions
            this.registerPlayerInformationProvider(factions,
                    new FactionHook_7_3_0());
        }

        BukkitHook bukkitHook = new BukkitHook(this);
        this.registerInformationProvider(this, bukkitHook);
        this.registerPlayerInformationProvider(this, bukkitHook);

        // start generalInformation update task
        this.generalInformationUpdateTask = new GeneralInformationUpdateTask(
                this);
        if (useAsyncThreads) {
            this.generalInformationUpdateTask.
                    runTaskTimerAsynchronously(this, 0,
                            Constants.updateDelay);
        } else {
            this.generalInformationUpdateTask.runTaskTimer(this, 0,
                    Constants.updateDelay);
        }

        // add all players yet on the server
        for (Player player : getServer().getOnlinePlayers()) {
            addPlayer(player);
        }

        // start autoreinitialize every 5 minutes
        new BukkitRunnable() {

            @Override
            public void run() {
                reinitialize();
            }

        }.runTaskTimer(this, Constants.completeUpdateDelay,
                Constants.completeUpdateDelay);
    }

    private void reinitialize() {
        BukkitBridge.this.generalInformationUpdateTask.setInitialized(false);
        for (PlayerInformationUpdateTask task
                : BukkitBridge.this.playerInformationUpdaters.values()) {
            task.setInitialized(false);
        }
    }

    private void addPlayer(Player player) {
        if (playerInformationUpdaters.containsKey(player)) {
            try {
                playerInformationUpdaters.get(player).cancel();
            } catch (Exception ex) {
                // TODO do something
            } finally {
                playerInformationUpdaters.remove(player);
            }
        }
        PlayerInformationUpdateTask playerInformationUpdateTask = new PlayerInformationUpdateTask(
                this, player);
        if (this.useAsyncThreads) {
            playerInformationUpdateTask.runTaskTimerAsynchronously(this, 0,
                    Constants.updateDelay);
        } else {
            playerInformationUpdateTask.runTaskTimer(this, 0,
                    Constants.updateDelay);
        }
        playerInformationUpdaters.put(player, playerInformationUpdateTask);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerInformationUpdaters.containsKey(player)) {
            try {
                playerInformationUpdaters.get(player).cancel();
            } catch (Exception ex) {
                // TODO do something
            } finally {
                playerInformationUpdaters.remove(player);
            }
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();

        if (this.pluginsPlayerInformationProviders.containsKey(plugin)) {
            this.playerInformationProviders.removeAll(
                    this.pluginsPlayerInformationProviders.get(plugin));
            this.pluginsPlayerInformationProviders.remove(plugin);
        }

        if (this.pluginsGeneralInformationProviders.containsKey(plugin)) {
            this.generalInformationProviders.removeAll(
                    this.pluginsGeneralInformationProviders.get(plugin));
            this.pluginsGeneralInformationProviders.remove(plugin);
        }

        reinitialize();
    }

    @Override
    public void registerInformationProvider(Plugin pl,
            GeneralInformationProvider ip) {
        this.generalInformationProviders.add(ip);
        if (!this.pluginsGeneralInformationProviders.containsKey(pl)) {
            this.pluginsGeneralInformationProviders.put(pl,
                    new HashSet<GeneralInformationProvider>());
        }
        this.pluginsGeneralInformationProviders.get(pl).add(ip);
    }

    @Override
    public void registerPlayerInformationProvider(Plugin pl,
            PlayerInformationProvider ip) {
        this.playerInformationProviders.add(ip);
        if (!this.pluginsPlayerInformationProviders.containsKey(pl)) {
            this.pluginsPlayerInformationProviders.put(pl,
                    new HashSet<PlayerInformationProvider>());
        }
        this.pluginsPlayerInformationProviders.get(pl).add(ip);
    }

    protected void sendInformation(String subchannel,
            Map<String, Object> information, Player player) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(os);
            out.writeUTF(subchannel);
            out.writeInt(information.size());
            for (Entry<String, Object> entry : information.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeUTF(entry.getValue().toString());
            }
            player.sendPluginMessage(this, Constants.channel, os.toByteArray());
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    protected Collection<GeneralInformationProvider> getGeneralInformationProviders() {
        return this.generalInformationProviders;
    }

    protected Collection<PlayerInformationProvider> getPlayerInformationProviders() {
        return this.playerInformationProviders;
    }

    public void reportError(Throwable th) {
        getLogger().log(Level.WARNING,
                ChatColor.RED + "An internal error occured! Please send the "
                + "following stacktrace to the developer in order to help"
                + " resolving the problem",
                th);
    }
}
