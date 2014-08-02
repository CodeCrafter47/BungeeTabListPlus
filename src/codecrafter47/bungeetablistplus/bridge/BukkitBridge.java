/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.bridge;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author florian
 */
public class BukkitBridge implements Listener {

    private final int currentVersion = 2;

    byte[] Cinit, Cinit_player;

    BungeeTabListPlus plugin;

    Map<String, Map<String, String>> serverInformation;
    Map<String, Map<String, String>> playerInformation;

    public BukkitBridge(BungeeTabListPlus plugin) {
        this.plugin = plugin;

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(os);
            out.writeUTF(Constants.subchannel_init);
            Cinit = os.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(BukkitBridge.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(os);
            out.writeUTF(Constants.subchannel_initplayer);
            Cinit_player = os.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(BukkitBridge.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    public void enable() {
        serverInformation = new ConcurrentHashMap<>();
        playerInformation = new ConcurrentHashMap<>();
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(Constants.channel)) {
            event.setCancelled(true);
            if (event.getReceiver() instanceof ProxiedPlayer && event.
                    getSender() instanceof Server) {
                try {
                    ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                    Server server = (Server) event.getSender();

                    DataInputStream in = new DataInputStream(
                            new ByteArrayInputStream(event.getData()));

                    String subchannel = in.readUTF();

                    Map<String, String> data = new ConcurrentHashMap<>();
                    int num = in.readInt();
                    while (num-- > 0) {
                        String key = in.readUTF();
                        String value = in.readUTF();
                        data.put(key, value);
                    }

                    if (subchannel.equals(Constants.subchannel_init)) {
                        serverInformation.put(server.getInfo().getName(), data);
                    } else if (subchannel.equals(Constants.subchannel_update)) {
                        if (serverInformation.get(server.getInfo().getName()) == null) {
                            server.sendData(Constants.channel, Cinit);
                        } else {
                            for (Entry<String, String> entry : data.entrySet()) {
                                serverInformation.get(server.getInfo().
                                        getName()).put(entry.getKey(), entry.
                                                getValue());
                            }
                        }
                    } else if (subchannel.
                            equals(Constants.subchannel_initplayer)) {
                        playerInformation.put(player.getName(), data);
                        data.put("server", server.getInfo().getName());
                    } else if (subchannel.equals(
                            Constants.subchannel_updateplayer)) {
                        if (playerInformation.get(player.getName()) == null) {
                            player.getServer().sendData(Constants.channel,
                                    Cinit_player);
                        } else {
                            for (Entry<String, String> entry : data.entrySet()) {
                                playerInformation.get(player.getName()).put(
                                        entry.getKey(), entry.getValue());
                            }
                        }
                    } else {
                        plugin.getLogger().log(Level.SEVERE,
                                "BukkitBridge on server " + server.getInfo().
                                getName() + " send an unknown packet! Is everything up-to-date?");
                    }

                } catch (IOException ex) {
                    plugin.getLogger().log(Level.SEVERE,
                            "Exception while parsing data from Bukkit", ex);
                }
            }
        }
    }

    @EventHandler
    public void onServerChange(ServerConnectedEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        if (playerInformation.get(player.getName()) != null) {
            playerInformation.remove(player.getName());
        }

        ProxyServer.getInstance().getScheduler().
                schedule(plugin,
                        new Runnable() {
                            @Override
                            public void run() {
                                requestInformationIn200Millis(player, 1);
                            }
                        }, 5, TimeUnit.MILLISECONDS);
    }

    private void requestInformationIn200Millis(final ProxiedPlayer player,
            final int tries) {
        if (tries > 50) {
            return;
        }
        if (playerInformation.get(player.
                getName()) != null) {
            return;
        }
        player.getServer().sendData(
                Constants.channel,
                Cinit_player);

        ProxyServer.getInstance().getScheduler().
                schedule(plugin,
                        new Runnable() {
                            @Override
                            public void run() {
                                requestInformationIn200Millis(player, tries + 1);
                            }
                        }, 200, TimeUnit.MILLISECONDS);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        playerInformation.remove(player.getName());
    }

    @EventHandler
    public void onPlayerKick(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();
        playerInformation.remove(player.getName());
    }

    public String getServerInformation(ServerInfo server, String key) {
        Map<String, String> map = serverInformation.get(server.getName());
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public String getPlayerInformation(ProxiedPlayer player, String key) {
        Map<String, String> map = playerInformation.get(player.getName());
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public boolean isServerInformationAvailable(ServerInfo server) {
        return serverInformation.get(server.getName()) != null;
    }

    public boolean isPlayerInformationAvailable(ProxiedPlayer player) {
        return playerInformation.get(player.getName()) != null;
    }

    public boolean isUpToDate(String server) {
        try {
            return Integer.valueOf(serverInformation.get(server).get(
                    "bridgeVersion")) >= this.currentVersion;
        } catch (Throwable ex) {
        }
        return true;
    }
}
