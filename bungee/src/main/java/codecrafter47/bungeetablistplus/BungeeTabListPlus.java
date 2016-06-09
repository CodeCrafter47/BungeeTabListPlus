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
package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.bungee.FakePlayerManager;
import codecrafter47.bungeetablistplus.api.bungee.PlayerManager;
import codecrafter47.bungeetablistplus.api.bungee.Skin;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListProvider;
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.bridge.PlaceholderAPIHook;
import codecrafter47.bungeetablistplus.commands.SuperCommand;
import codecrafter47.bungeetablistplus.common.BugReportingService;
import codecrafter47.bungeetablistplus.common.Constants;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.listener.TabListListener;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.managers.ConnectedPlayerManager;
import codecrafter47.bungeetablistplus.managers.DataManager;
import codecrafter47.bungeetablistplus.managers.PermissionManager;
import codecrafter47.bungeetablistplus.managers.PlaceholderManagerImpl;
import codecrafter47.bungeetablistplus.managers.PlayerManagerImpl;
import codecrafter47.bungeetablistplus.managers.RedisPlayerManager;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.managers.SkinManagerImpl;
import codecrafter47.bungeetablistplus.managers.TabListManager;
import codecrafter47.bungeetablistplus.placeholder.BasicPlaceholders;
import codecrafter47.bungeetablistplus.placeholder.BukkitPlaceholders;
import codecrafter47.bungeetablistplus.placeholder.ColorPlaceholder;
import codecrafter47.bungeetablistplus.placeholder.ConditionalPlaceholders;
import codecrafter47.bungeetablistplus.placeholder.OnlineStatePlaceholder;
import codecrafter47.bungeetablistplus.placeholder.PlayerCountPlaceholder;
import codecrafter47.bungeetablistplus.placeholder.RedisBungeePlaceholders;
import codecrafter47.bungeetablistplus.placeholder.TimePlaceholders;
import codecrafter47.bungeetablistplus.player.FakePlayerManagerImpl;
import codecrafter47.bungeetablistplus.player.IPlayerProvider;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.protocol.ProtocolManager;
import codecrafter47.bungeetablistplus.tablistproviders.CheckedTabListProvider;
import codecrafter47.bungeetablistplus.updater.UpdateChecker;
import codecrafter47.bungeetablistplus.updater.UpdateNotifier;
import codecrafter47.bungeetablistplus.util.PingTask;
import codecrafter47.bungeetablistplus.version.BungeeProtocolVersionProvider;
import codecrafter47.bungeetablistplus.version.ProtocolSupportVersionProvider;
import codecrafter47.bungeetablistplus.version.ProtocolVersionProvider;
import com.google.common.base.Preconditions;
import de.sabbertran.proxysuite.ProxySuiteAPI;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Main Class of BungeeTabListPlus
 *
 * @author Florian Stober
 */
public class BungeeTabListPlus extends BungeeTabListPlusAPI {

    /**
     * Holds an INSTANCE of itself if the plugin is enabled
     */
    private static BungeeTabListPlus INSTANCE;
    @Getter
    private final Plugin plugin;
    private Collection<IPlayerProvider> playerProviders;
    private ResendThread resendThread;

    @Getter
    private RedisPlayerManager redisPlayerManager;
    @Getter
    private DataManager dataManager;

    public BungeeTabListPlus(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Static getter for the current instance of the plugin
     *
     * @return the current instance of the plugin, null if the plugin is
     * disabled
     */
    public static BungeeTabListPlus getInstance(Plugin plugin) {
        if (INSTANCE == null) {
            INSTANCE = new BungeeTabListPlus(plugin);
        }
        return INSTANCE;
    }

    public static BungeeTabListPlus getInstance() {
        return INSTANCE;
    }

    /**
     * provides access to the configuration
     */
    private ConfigManager config;

    private FakePlayerManagerImpl fakePlayerManager;

    /**
     * provides access to the Placeholder Manager use this to add Placeholders
     */
    private PlaceholderManagerImpl placeholderManager;

    private PermissionManager pm;

    private TabListManager tabLists;
    private final TabListListener listener = new TabListListener(this);

    private ScheduledTask refreshThread = null;

    private final static Collection<String> hiddenPlayers = new HashSet<>();

    private BukkitBridge bukkitBridge;

    private UpdateChecker updateChecker = null;

    private final Map<String, PingTask> serverState = new HashMap<>();

    private SkinManager skins;

    @Getter
    private ConnectedPlayerManager connectedPlayerManager = new ConnectedPlayerManager();

    @Getter
    private PlaceholderAPIHook placeholderAPIHook;

    public PingTask getServerState(String serverName) {
        if (serverState.containsKey(serverName)) {
            return serverState.get(serverName);
        }
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);
        if (serverInfo != null) {
            // start server ping tasks
            int delay = config.getMainConfig().pingDelay;
            if (delay <= 0 || delay > 10) {
                delay = 10;
            }
            PingTask task = new PingTask(serverInfo);
            serverState.put(serverName, task);
            plugin.getProxy().getScheduler().schedule(plugin, task, delay, delay, TimeUnit.SECONDS);
        }
        return serverState.get(serverName);
    }

    @Getter
    private ProtocolVersionProvider protocolVersionProvider;

    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        try {
            Class.forName("net.md_5.bungee.api.Title");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("You need to run at least BungeeCord version #995");
        }

        try {
            Field field = BungeeTabListPlusAPI.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, this);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize API", ex);
        }

        INSTANCE = this;

        try {
            config = new ConfigManager(plugin);
        } catch (InvalidConfigurationException ex) {
            plugin.getLogger().warning("Unable to load Config");
            plugin.getLogger().log(Level.WARNING, null, ex);
            plugin.getLogger().warning("Disabling Plugin");
            return;
        }

        if (config.getMainConfig().automaticallySendBugReports) {
            BugReportingService bugReportingService = new BugReportingService(Level.SEVERE, getPlugin().getDescription().getName(), getPlugin().getDescription().getVersion(), command -> plugin.getProxy().getScheduler().runAsync(plugin, command));
            bugReportingService.registerLogger(getLogger());
        }

        resendThread = new ResendThread();

        File headsFolder = new File(plugin.getDataFolder(), "heads");

        if (!headsFolder.exists()) {
            headsFolder.mkdirs();

            try {
                // copy default heads
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(plugin.getFile()));

                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().startsWith("heads/")) {
                        try {
                            File targetFile = new File(plugin.getDataFolder(), entry.getName());
                            targetFile.getParentFile().mkdirs();
                            if (!targetFile.exists()) {
                                Files.copy(zipInputStream, targetFile.toPath());
                                getLogger().info("Extracted " + entry.getName());
                            }
                        } catch (IOException ex) {
                            getLogger().log(Level.SEVERE, "Failed to extract file " + entry.getName(), ex);
                        }
                    }
                }

                zipInputStream.close();
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Error extracting files", ex);
            }
        }

        skins = new SkinManagerImpl(plugin, headsFolder);

        fakePlayerManager = new FakePlayerManagerImpl(plugin);

        playerProviders = new ArrayList<>();

        if (plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            redisPlayerManager = new RedisPlayerManager(connectedPlayerManager);
            playerProviders.add(redisPlayerManager);
            plugin.getLogger().info("Hooked RedisBungee");
        }

        playerProviders.add(connectedPlayerManager);

        playerProviders.add(fakePlayerManager);

        plugin.getProxy().registerChannel(Constants.channel);
        bukkitBridge = new BukkitBridge(this);

        pm = new PermissionManager(this);

        dataManager = new DataManager(this, getPermissionManager());

        placeholderManager = new PlaceholderManagerImpl();
        placeholderManager.internalRegisterPlaceholderProvider(new BasicPlaceholders());
        placeholderManager.internalRegisterPlaceholderProvider(new BukkitPlaceholders());
        placeholderManager.internalRegisterPlaceholderProvider(new ColorPlaceholder());
        placeholderManager.internalRegisterPlaceholderProvider(new ConditionalPlaceholders());
        placeholderManager.internalRegisterPlaceholderProvider(new OnlineStatePlaceholder());
        placeholderManager.internalRegisterPlaceholderProvider(new PlayerCountPlaceholder());
        if (plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            placeholderManager.internalRegisterPlaceholderProvider(new RedisBungeePlaceholders());
        }
        placeholderManager.internalRegisterPlaceholderProvider(new TimePlaceholders());

        tabLists = new TabListManager(this);
        if (!tabLists.loadTabLists()) {
            return;
        }

        if (plugin.getProxy().getPluginManager().getPlugin("ProtocolSupportBungee") != null) {
            protocolVersionProvider = new ProtocolSupportVersionProvider();
        } else {
            protocolVersionProvider = new BungeeProtocolVersionProvider();
        }

        ProxyServer.getInstance().getPluginManager().registerListener(plugin,
                listener);
        plugin.getProxy().getScheduler().runAsync(plugin, resendThread);
        restartRefreshThread();

        // register commands and update Notifier
        ProxyServer.getInstance().getPluginManager().registerCommand(
                plugin,
                new SuperCommand(this));
        ProxyServer.getInstance().getScheduler().schedule(plugin,
                new UpdateNotifier(this), 15, 15, TimeUnit.MINUTES);

        // Start metrics
        try {
            Metrics metrics = new Metrics(plugin);
            metrics.start();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize Metrics", e);
        }

        // Load updateCheck thread
        if (config.getMainConfig().checkForUpdates) {
            updateChecker = new UpdateChecker(plugin);
        }

        // Start packet listeners
        ProtocolManager protocolManager = new ProtocolManager(plugin);
        protocolManager.enable();

        int[] serversHash = {getProxy().getServers().hashCode()};
        getProxy().getScheduler().schedule(plugin, () -> {
            int hash = getProxy().getServers().hashCode();
            if (hash != serversHash[0]) {
                serversHash[0] = hash;
                getLogger().info("Network topology change detected. Reloading plugin.");
                reload();
            }
        }, 1, 1, TimeUnit.MINUTES);

        placeholderAPIHook = new PlaceholderAPIHook(this);

        placeholderAPIHook.onLoad();
    }

    private Double requestedUpdateInterval = null;

    private void restartRefreshThread() {
        if (refreshThread != null) {
            refreshThread.cancel();
        }
        double updateInterval = config.getMainConfig().tablistUpdateInterval;
        if (requestedUpdateInterval != null && (requestedUpdateInterval < updateInterval || updateInterval <= 0)) {
            updateInterval = requestedUpdateInterval;
        }
        if (updateInterval > 0) {
            try {
                refreshThread = ProxyServer.getInstance().getScheduler().
                        schedule(
                                plugin, this::resendTabLists,
                                (long) (updateInterval * 1000),
                                (long) (updateInterval * 1000),
                                TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException ignored) {
                // this occurs on proxy shutdown -> we can safely ignore it
            }
        } else {
            refreshThread = null;
        }
    }

    public void requireUpdateInterval(double updateInterval) {
        if (requestedUpdateInterval == null || updateInterval < requestedUpdateInterval) {
            requestedUpdateInterval = updateInterval;
            restartRefreshThread();
        }
    }

    /**
     * Reloads most settings of the plugin
     */
    public boolean reload() {
        if (!resendThread.isInMainThread()) {
            AtomicReference<Boolean> ref = new AtomicReference<>(null);
            resendThread.execute(() -> {
                ref.set(reload());
            });
            while (ref.get() == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                    return false;
                }
            }
            return ref.get();
        }
        failIfNotMainThread();
        try {
            // todo requestedUpdateInterval = null;
            config = new ConfigManager(plugin);
            placeholderManager.reload();
            if (reloadTablists()) return false;
            fakePlayerManager.reload();
            resendTabLists();
            restartRefreshThread();
            placeholderAPIHook.onLoad();
            skins.onReload();
        } catch (InvalidConfigurationException ex) {
            plugin.getLogger().log(Level.WARNING, "Unable to reload Config", ex);
        }
        return true;
    }

    private boolean reloadTablists() {
        failIfNotMainThread();
        TabListManager tabListManager = new TabListManager(this);
        if (!tabListManager.loadTabLists()) {
            return true;
        }
        tabListManager.customTabLists = tabLists.customTabLists;
        tabLists = tabListManager;
        return false;
    }

    public void registerPlaceholderProvider0(PlaceholderProvider placeholderProvider) {
        getPlaceholderManager0().internalRegisterPlaceholderProvider(placeholderProvider);
        runInMainThread(this::reloadTablists);
    }

    /**
     * updates the tabList on all connected clients
     */
    public void resendTabLists() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            resendThread.add(player);
        }
    }

    public void runInMainThread(Runnable runnable) {
        resendThread.execute(runnable);
    }

    public void failIfNotMainThread() {
        if (!resendThread.isInMainThread()) {
            getLogger().log(Level.SEVERE, "Not in main thread", new IllegalStateException("Not in main thread"));
        }
    }

    public void updateTabListForPlayer(ProxiedPlayer player) {
        resendThread.add(player);
    }

    /**
     * Getter for an instance of the PlayerManager. For internal use only.
     *
     * @return an instance of the PlayerManager or null
     */
    public PlayerManager constructPlayerManager(ProxiedPlayer viewer) {
        return new PlayerManagerImpl(this, playerProviders, viewer);
    }

    public SkinManager getSkinManager() {
        return skins;
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

    public PlaceholderManagerImpl getPlaceholderManager0() {
        return placeholderManager;
    }

    @Override
    protected FakePlayerManager getFakePlayerManager0() {
        return fakePlayerManager;
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
    public static boolean isHidden(Player player) {
        if (isHiddenServer(player.getServer().orElse(null))) return true;
        final boolean[] hidden = new boolean[1];
        synchronized (hiddenPlayers) {
            String name = player.getName();
            hidden[0] = hiddenPlayers.contains(name);
        }
        List<String> permanentlyHiddenPlayers = getInstance().getConfigManager().getMainConfig().hiddenPlayers;
        if (permanentlyHiddenPlayers.contains(player.getName())) {
            hidden[0] = true;
        }
        if (permanentlyHiddenPlayers.contains(player.getUniqueID().toString())) {
            hidden[0] = true;
        }
        player.get(DataKeys.VanishNoPacket_IsVanished).ifPresent(b -> hidden[0] |= b);
        player.get(DataKeys.SuperVanish_IsVanished).ifPresent(b -> hidden[0] |= b);
        player.get(DataKeys.Essentials_IsVanished).ifPresent(b -> hidden[0] |= b);

        // check ProxyCore
        if (!hidden[0]) {
            if (ProxyServer.getInstance().getPluginManager().getPlugin("ProxySuite") != null) {
                try {
                    ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(player.getName());
                    if (proxiedPlayer != null) {
                        hidden[0] |= ProxySuiteAPI.isVanished(proxiedPlayer);
                    }
                } catch (Throwable th) {
                    getInstance().getLogger().log(Level.WARNING, "An error occurred while looking up a players vanish status from ProxyCore.", th);
                }
            }
        }

        return hidden[0];
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

    public static boolean isHiddenServer(ServerInfo server) {
        if (server == null)
            return false;
        return getInstance().config.getMainConfig().hiddenServers.contains(server.getName());
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

    public boolean isNewDevBuildAvailable() {
        return updateChecker != null && updateChecker.isNewDevBuildAvailable();
    }

    public void reportError(Throwable th) {
        plugin.getLogger().log(Level.SEVERE,
                ChatColor.RED + "An internal error occurred! Please send the "
                        + "following StackTrace to the developer in order to help"
                        + " resolving the problem",
                th);
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public ProxyServer getProxy() {
        return plugin.getProxy();
    }

    public boolean isServer(String s) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if (s.equalsIgnoreCase(server.getName())) {
                return true;
            }
            int i = s.indexOf('#');
            if (i > 1) {
                if (s.substring(0, i).equalsIgnoreCase(server.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private final static Pattern PATTERN_VALID_USERNAME = Pattern.compile("(?:\\p{Alnum}|_){1,16}");

    @Override
    protected Skin getSkinForPlayer0(String nameOrUUID) {
        if (!PATTERN_VALID_USERNAME.matcher(nameOrUUID).matches()) {
            try {
                UUID.fromString(nameOrUUID); // TODO: 02.06.16 this is slow
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Given string is neither valid username nor uuid: " + nameOrUUID);
            }
        }
        Preconditions.checkState(getSkinManager() != null, "BungeeTabListPlus not initialized");
        return getSkinManager().getSkin(nameOrUUID);
    }

    @Override
    protected Skin getDefaultSkin0() {
        return SkinManager.defaultSkin;
    }

    @Override
    protected void requireTabListUpdateInterval0(double interval) {
        requireUpdateInterval(interval);
    }

    @Override
    protected void setCustomTabList0(ProxiedPlayer player, TabListProvider tabListProvider) {
        Preconditions.checkState(getTabListManager() != null, "BungeeTabListPlus not initialized");
        getTabListManager().setCustomTabList(player, new CheckedTabListProvider(tabListProvider));
    }

    @Override
    protected void removeCustomTabList0(ProxiedPlayer player) {
        Preconditions.checkState(getTabListManager() != null, "BungeeTabListPlus not initialized");
        getTabListManager().removeCustomTabList(player);
    }
}
