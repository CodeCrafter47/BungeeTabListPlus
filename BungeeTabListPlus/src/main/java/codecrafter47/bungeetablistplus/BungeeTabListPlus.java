/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.bridge.Constants;
import codecrafter47.bungeetablistplus.commands.OldSuperCommand;
import codecrafter47.bungeetablistplus.commands.SuperCommand;
import codecrafter47.bungeetablistplus.listener.TabListListener;
import codecrafter47.bungeetablistplus.managers.*;
import codecrafter47.bungeetablistplus.packets.TabHeaderPacket;
import codecrafter47.bungeetablistplus.packets.TeamPacket;
import codecrafter47.bungeetablistplus.player.*;
import codecrafter47.bungeetablistplus.updater.UpdateChecker;
import codecrafter47.bungeetablistplus.updater.UpdateNotifier;
import gnu.trove.map.TObjectIntMap;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Class of BungeeTabListPlus
 *
 * @author Florian Stober
 */
public class BungeeTabListPlus extends Plugin {

    /**
     * Holds an INSTANCE of itself if the plugin is enabled
     */
    private static BungeeTabListPlus INSTANCE;

    /**
     * Static getter for the current instance of the plugin
     *
     * @return the current instance of the plugin, null if the plugin is
     * disabled
     */
    public static BungeeTabListPlus getInstance() {
        return INSTANCE;

    }

    private PlayerManager players;

    /**
     * provides access to the configuration
     */
    private ConfigManager config;

    private FakePlayerManager fakePlayerManager;

    /**
     * provides access to the Variable Manager use this to add Variables
     */
    private VariablesManager variables;

    private PermissionManager pm;

    private TabListManager tabLists;
    private final TabListListener listener = new TabListListener(this);

    private final SendingQueue resendQueue = new SendingQueue();

    private ScheduledTask refreshThread = null;

    private final static Collection<String> hiddenPlayers = new HashSet<>();

    private BukkitBridge bukkitBridge;

    private UpdateChecker updateChecker = null;

    static private boolean is18 = true;

    static private boolean isAbove995 = false;

    private PacketManager packets;

    private final Map<String, PingTask> serverState = new HashMap<>();

    private SkinManager skins;

    @Getter
    private BungeePlayerProvider bungeePlayerProvider = new BungeePlayerProvider();

    public PingTask getServerState(String o) {
        return serverState.get(o);
    }

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        INSTANCE = this;
        try {
            config = new ConfigManager(this);
        } catch (InvalidConfigurationException ex) {
            getLogger().warning("Unable to load Config");
            getLogger().log(Level.WARNING, null, ex);
            getLogger().info("Disabling Plugin");
            return;
        }

        // TODO probably parsing the version string is a better idea
        try {
            Class.forName("net.md_5.bungee.tab.TabList");
        } catch (ClassNotFoundException ex) {
            is18 = false;
        }

        try {
            Class.forName("net.md_5.bungee.api.Title");
            isAbove995 = true;
        } catch (ClassNotFoundException ex) {
            isAbove995 = false;
        }

        if (!isVersion18()) {

            packets = new PacketManager();

            if (!packets.isTabModificationSupported()) {
                getLogger().warning(
                        "Your BungeeCord Version isn't supported yet");
                getLogger().info("Disabling Plugin");
                return;
            }

            if ((!packets.isScoreboardSupported()) && config.getMainConfig().useScoreboardToBypass16CharLimit) {
                getLogger().warning(
                        "Your BungeeCord Version does not support the following option: 'useScoreboardToBypass16CharLimit'");
                getLogger().warning("This option will be disabled");
                config.getMainConfig().useScoreboardToBypass16CharLimit = false;
            }
        } else {
            packets = null;
            skins = new SkinManager(this);
        }

        // start server ping tasks
        if (config.getMainConfig().pingDelay > 0) {
            for (ServerInfo server : getProxy().getServers().values()) {
                PingTask task = new PingTask(server);
                serverState.put(server.getName(), task);
                getProxy().getScheduler().schedule(this, task, config.
                                getMainConfig().pingDelay,
                        config.getMainConfig().pingDelay, TimeUnit.SECONDS);
            }
        }

        fakePlayerManager = new FakePlayerManager(this);

        Collection<IPlayerProvider> playerProviders = new ArrayList<>();

        playerProviders.add(fakePlayerManager);

        if (getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            playerProviders.add(new RedisPlayerProvider());
            getLogger().info("Hooked RedisBungee");
        } else {
            playerProviders.add(bungeePlayerProvider);
        }

        players = new PlayerManager(this, playerProviders);

        tabLists = new TabListManager(this);
        if (!tabLists.loadTabLists()) {
            return;
        }

        getProxy().registerChannel(Constants.channel);
        bukkitBridge = new BukkitBridge(this);
        bukkitBridge.enable();

        pm = new PermissionManager(this);
        variables = new VariablesManager();

        ProxyServer.getInstance().getPluginManager().registerListener(this,
                listener);

        ResendThread resendThread = new ResendThread(resendQueue,
                config.getMainConfig().tablistUpdateIntervall);
        getProxy().getScheduler().schedule(this, resendThread, 1,
                TimeUnit.SECONDS);
        startRefreshThread();

        // register commands and update Notifier
        try {
            Thread.currentThread().getContextClassLoader().loadClass(
                    "net.md_5.bungee.api.chat.ComponentBuilder");
            ProxyServer.getInstance().getPluginManager().registerCommand(
                    INSTANCE,
                    new SuperCommand(this));
            ProxyServer.getInstance().getScheduler().schedule(this,
                    new UpdateNotifier(this), 15, 15, TimeUnit.MINUTES);
        } catch (Exception ex) {
            ProxyServer.getInstance().getPluginManager().registerCommand(
                    INSTANCE,
                    new OldSuperCommand(this));
        }

        // Start metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().warning("Failed to initialize Metrics");
            getLogger().warning(e.getLocalizedMessage());
        }

        // Load updateCheck thread
        if (config.getMainConfig().checkForUpdates) {
            updateChecker = new UpdateChecker(this);
        }

        if (isVersion18() && !isAbove995) {
            try {
                // register tabheaderpacket
                Class clazz = Protocol.DirectionData.class;
                Field tabListHandler = clazz.getDeclaredField("packetMap");
                tabListHandler.setAccessible(true);
                TObjectIntMap<Class<? extends DefinedPacket>> packetMap = (TObjectIntMap<Class<? extends DefinedPacket>>) tabListHandler.
                        get(Protocol.GAME.TO_CLIENT);
                packetMap.put(TabHeaderPacket.class, 0x47);
            } catch (IllegalArgumentException | IllegalAccessException |
                    NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(BungeeTabListPlus.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }

        if (isVersion18()) {
            try {
                // register team packet
                Class clazz = Protocol.DirectionData.class;
                Method registerPacket = clazz.getDeclaredMethod("registerPacket", int.class, Class.class);
                registerPacket.setAccessible(true);
                registerPacket.invoke(Protocol.GAME.TO_CLIENT, 62, TeamPacket.class);
            } catch (IllegalArgumentException | NoSuchMethodException |
                    SecurityException | InvocationTargetException | IllegalAccessException ex) {
                Logger.getLogger(BungeeTabListPlus.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
    }

    private void startRefreshThread() {
        if (config.getMainConfig().tablistUpdateIntervall > 0) {
            try {
                refreshThread = ProxyServer.getInstance().getScheduler().
                        schedule(
                                INSTANCE, new Runnable() {

                                    @Override
                                    public void run() {
                                        resendTabLists();
                                        startRefreshThread();
                                    }
                                },
                                (long) (config.getMainConfig().tablistUpdateIntervall * 1000),
                                TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException ex) {
                // this occurs on proxy shutdown -> we can safely ignore it
            }
        } else {
            refreshThread = null;
        }
    }

    /**
     * Reloads most settings of the plugin
     */
    public boolean reload() {
        try {
            config = new ConfigManager(this);
            TabListManager tabListManager = new TabListManager(this);
            if (!tabListManager.loadTabLists()) {
                return false;
            }
            tabLists = tabListManager;
            fakePlayerManager.reload();
        } catch (InvalidConfigurationException ex) {
            getLogger().warning("Unable to reload Config");
            getLogger().log(Level.WARNING, null, ex);
        }
        if (refreshThread == null) {
            startRefreshThread();
        }
        return true;
    }

    /**
     * called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        // let the proxy do this
    }

    /**
     * updates the tabList on all connected clients
     */
    public void resendTabLists() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            resendQueue.addPlayer(player);
        }
    }

    /**
     * updates the tablist for one player; the player is put at top of the
     * resend-queue
     *
     * @param player the player whos tablist should be updated
     */
    public void sendImmediate(ProxiedPlayer player) {
        resendQueue.addFrontPlayer(player);
    }

    /**
     * updates the tablist for one player; the player is put at the end of the
     * resend-queue
     *
     * @param player the player whos tablist should be updated
     */
    public void sendLater(ProxiedPlayer player) {
        resendQueue.addPlayer(player);
    }

    /**
     * Getter for an instance of the PlayerManager. For internal use only.
     *
     * @return an instance of the PlayerManager or null
     */
    public PlayerManager getPlayerManager() {
        return this.players;
    }

    public SkinManager getSkinManager() {
        return skins;
    }

    /**
     * Getter of the PacketManager. For internal use only
     *
     * @return an instance of the PacketManager or null
     */
    public PacketManager getPacketManager() {
        return packets;
    }

    /**
     * Getter for the PermissionManager. For internal use only.
     *
     * @return an instance of the PermissionManager or null
     */
    public PermissionManager getPermissionManager() {
        return pm;
    }

    /**
     * Getter for the ConfigManager. For internal use only.
     *
     * @return an instance of the ConfigManager or null
     */
    public ConfigManager getConfigManager() {
        return config;
    }

    /**
     * Getter for the VariableManager. The VariableManager can be used to add
     * custom Variables.
     *
     * @return an instance of the VariableManager or null
     */
    public VariablesManager getVariablesManager() {
        return variables;
    }

    /**
     * Getter for the TabListManager. For internal use only
     *
     * @return an instance of the TabListManager
     */
    public TabListManager getTabListManager() {
        return tabLists;
    }

    /**
     * checks whether a player is hidden from the tablist
     *
     * @param player the player object for which the check should be performed
     * @return true if the player is hidden, false otherwise
     */
    public static boolean isHidden(IPlayer player, ProxiedPlayer viewer) {
        if (getInstance().getPermissionManager().hasPermission(viewer, "bungeetablistplus.seevanished")) return false;
        boolean hidden;
        synchronized (hiddenPlayers) {
            String name = player.getName();
            hidden = hiddenPlayers.contains(name);
        }
        String s = getInstance().bukkitBridge.getPlayerInformation(player,
                "isVanished");
        if (s != null) {
            hidden |= Boolean.valueOf(s);
        }
        return hidden;
    }

    /**
     * checks whether a player is hidden from the tablist
     *
     * @param player the player object for which the check should be performed
     * @return true if the player is hidden, false otherwise
     */
    public static boolean isHidden(IPlayer player) {
        boolean hidden;
        synchronized (hiddenPlayers) {
            String name = player.getName();
            hidden = hiddenPlayers.contains(name);
        }
        String s = getInstance().bukkitBridge.getPlayerInformation(player,
                "isVanished");
        if (s != null) {
            hidden |= Boolean.valueOf(s);
        }
        return hidden;
    }

    /**
     * Hides a player from the tablist
     *
     * @param player The player which should be hidden.
     */
    public static void hidePlayer(ProxiedPlayer player) {
        synchronized (hiddenPlayers) {
            String name = player.getName();
            if (!hiddenPlayers.contains(name))
                hiddenPlayers.add(name);
        }
    }

    /**
     * Unhides a previously hidden player from the tablist. Only works if the
     * playe has been hidden via the hidePlayer method. Not works for players
     * hidden by VanishNoPacket
     *
     * @param player the player on which the operation should be performed
     */
    public static void unhidePlayer(ProxiedPlayer player) {
        synchronized (hiddenPlayers) {
            String name = player.getName();
            hiddenPlayers.remove(name);
        }
    }

    /**
     * Getter for BukkitBridge. For internal use only.
     *
     * @return an instance of BukkitBridge
     */
    public BukkitBridge getBridge() {
        return this.bukkitBridge;
    }

    /**
     * Checks whether an update for BungeeTabListPlus is available. Acctually
     * the check is performed in a background task and this only returns the
     * result.
     *
     * @return true if an newer version of BungeeTabListPlus is available
     */
    public boolean isUpdateAvailable() {
        return updateChecker != null && updateChecker.isUpdateAvailable();
    }

    public void reportError(Throwable th) {
        getLogger().log(Level.WARNING,
                ChatColor.RED + "An internal error occurred! Please send the "
                        + "following StackTrace to the developer in order to help"
                        + " resolving the problem",
                th);
    }

    public static boolean isVersion18() {
        return is18;
    }

    public static Object getTabList(ProxiedPlayer player) throws
            IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException {
        Class cplayer = UserConnection.class;
        Field tabListHandler = cplayer.getDeclaredField(
                isVersion18() ? "tabListHandler" : "tabList");
        tabListHandler.setAccessible(true);
        return tabListHandler.get(player);
    }

    public static void setTabList(ProxiedPlayer player, Object tabList) throws
            IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException {
        Class cplayer = UserConnection.class;
        Field tabListHandler = cplayer.getDeclaredField(
                isVersion18() ? "tabListHandler" : "tabList");
        tabListHandler.setAccessible(true);
        tabListHandler.set(player, tabList);
    }

    public static boolean isAbove995() {
        return isAbove995;
    }
}
