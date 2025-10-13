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

package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.api.velocity.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.cache.Cache;
import codecrafter47.bungeetablistplus.command.CommandBungeeTabListPlus;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.config.MainConfig;
import codecrafter47.bungeetablistplus.config.PlayersByServerComponentConfiguration;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.data.PermissionDataProvider;
import codecrafter47.bungeetablistplus.listener.TabListListener;
import codecrafter47.bungeetablistplus.managers.*;
import codecrafter47.bungeetablistplus.placeholder.GlobalServerPlaceholderResolver;
import codecrafter47.bungeetablistplus.placeholder.PlayerPlaceholderResolver;
import codecrafter47.bungeetablistplus.placeholder.ServerCountPlaceholderResolver;
import codecrafter47.bungeetablistplus.placeholder.ServerPlaceholderResolver;
import codecrafter47.bungeetablistplus.player.FakePlayerManagerImpl;
import codecrafter47.bungeetablistplus.tablist.ExcludedServersTabOverlayProvider;
import codecrafter47.bungeetablistplus.updater.UpdateChecker;
import codecrafter47.bungeetablistplus.updater.UpdateNotifier;
import codecrafter47.bungeetablistplus.util.ExceptionHandlingEventExecutor;
import codecrafter47.bungeetablistplus.util.GeyserCompat;
import codecrafter47.bungeetablistplus.util.MatchingStringsCollection;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import codecrafter47.bungeetablistplus.util.VelocityPlugin;
import codecrafter47.bungeetablistplus.version.VelocityProtocolVersionProvider;
import codecrafter47.bungeetablistplus.version.ProtocolVersionProvider;
import codecrafter47.bungeetablistplus.version.ViaVersionProtocolVersionProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.velocity.api.VelocityData;
import de.codecrafter47.data.sponge.api.SpongeData;
import de.codecrafter47.taboverlay.config.ComponentSpec;
import de.codecrafter47.taboverlay.config.ConfigTabOverlayManager;
import de.codecrafter47.taboverlay.config.ErrorHandler;
import de.codecrafter47.taboverlay.config.dsl.customplaceholder.CustomPlaceholderConfiguration;
import de.codecrafter47.taboverlay.config.icon.DefaultIconManager;
import de.codecrafter47.taboverlay.config.platform.EventListener;
import de.codecrafter47.taboverlay.config.platform.Platform;
import de.codecrafter47.taboverlay.config.player.JoinedPlayerProvider;
import de.codecrafter47.taboverlay.config.player.PlayerProvider;
import de.codecrafter47.taboverlay.config.spectator.SpectatorPassthroughTabOverlayManager;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import lombok.Getter;
import lombok.val;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Main Class of BungeeTabListPlus
 *
 * @author Florian Stober
 */
public class BungeeTabListPlus {

    /**
     * Holds an INSTANCE of itself if the plugin is enabled
     */
    private static BungeeTabListPlus INSTANCE;
    @Getter
    private final VelocityPlugin plugin;

    public PlayerProvider playerProvider;
    @Getter
    private EventExecutor mainThreadExecutor;
    @Getter
    private EventExecutorGroup asyncExecutor;

    @Getter
    private RedisPlayerManager redisPlayerManager;
    @Getter
    private DataManager dataManager;
    private ServerStateManager serverStateManager;
    @Getter
    private ServerPlaceholderResolver serverPlaceholderResolver;

    private Yaml yaml;
    @Getter
    private HiddenPlayersManager hiddenPlayersManager;
    private PlayerPlaceholderResolver playerPlaceholderResolver;
    private API api;
    @Getter
    private Logger logger = Logger.getLogger(BungeeTabListPlus.class.getSimpleName());

    public BungeeTabListPlus(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Static getter for the current instance of the plugin
     *
     * @return the current instance of the plugin, null if the plugin is
     * disabled
     */
    public static BungeeTabListPlus getInstance(VelocityPlugin plugin) {
        if (INSTANCE == null) {
            INSTANCE = new BungeeTabListPlus(plugin);
        }
        return INSTANCE;
    }

    public static BungeeTabListPlus getInstance() {
        return INSTANCE;
    }

    @Getter
    private MainConfig config;

    private Cache cache;
    @Getter
    MatchingStringsCollection excludedServers;

    @Getter
    private FakePlayerManagerImpl fakePlayerManagerImpl;

    private BukkitBridge bukkitBridge;

    private UpdateChecker updateChecker = null;

    @Getter
    private DefaultIconManager iconManager;

    @Getter
    private VelocityPlayerProvider velocityPlayerProvider;

    @Getter
    private ProtocolVersionProvider protocolVersionProvider;

    @Getter
    private TabViewManager tabViewManager;

    private ConfigTabOverlayManager configTabOverlayManager;
    private SpectatorPassthroughTabOverlayManager spectatorPassthroughTabOverlayManager;

    @Getter
    private List<EventListener> listeners = new ArrayList<>();

    private transient boolean scheduledSoftReload;
    @Getter
    private final ChannelIdentifier channelIdentifier = MinecraftChannelIdentifier.from(BridgeProtocolConstants.CHANNEL);

    public void onLoad() {
        codecrafter47.bungeetablistplus.util.ProxyServer.setProxyServer(plugin.getProxy());
        if (!plugin.getDataDirectory().toFile().exists()) {
            plugin.getDataDirectory().toFile().mkdirs();
        }

        try {
            plugin.getProxy().isShuttingDown();
        } catch (NoSuchMethodError ex) {
            throw new RuntimeException("You need to run at least Velocity version #464");
        }

        INSTANCE = this;

        // Hacks to get around no Team packet in Velocity
        ReflectionUtil.injectTeamPacketRegistry();

        Executor executor = (task) -> getProxy().getScheduler().buildTask(getPlugin(), task).schedule();

        asyncExecutor = new MultithreadEventExecutorGroup(4, executor) {
            @Override
            protected EventExecutor newChild(Executor executor, Object... args) {
                return new ExceptionHandlingEventExecutor(this, executor, logger);
            }
        };
        mainThreadExecutor = new ExceptionHandlingEventExecutor(null, executor, logger);

        if (getProxy().getPluginManager().getPlugin("viaversion").isPresent()) {
            protocolVersionProvider = new ViaVersionProtocolVersionProvider();
        } else {
            protocolVersionProvider = new VelocityProtocolVersionProvider();
        }

        this.tabViewManager = new TabViewManager(this, protocolVersionProvider);

        File headsFolder = new File(plugin.getDataDirectory().toFile(), "heads");
        extractDefaultIcons(headsFolder);

        iconManager = new DefaultIconManager(asyncExecutor, mainThreadExecutor, headsFolder.toPath(), logger);

        cache = Cache.load(new File(plugin.getDataDirectory().toFile(), "cache.dat"));

        serverPlaceholderResolver = new ServerPlaceholderResolver(cache);
        playerPlaceholderResolver = new PlayerPlaceholderResolver(serverPlaceholderResolver, cache);

        api = new API(tabViewManager, iconManager, playerPlaceholderResolver, serverPlaceholderResolver, logger, this);

        try {
            Field field = BungeeTabListPlusAPI.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, api);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize API", ex);
        }
    }

    public void onEnable() {

        ConfigTabOverlayManager.Options options = ConfigTabOverlayManager.Options.createBuilderWithDefaults()
                .playerIconDataKey(BTLPVelocityDataKeys.DATA_KEY_ICON)
                .playerPingDataKey(VelocityData.Velocity_Ping)
                .playerInvisibleDataKey(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN)
                .playerCanSeeInvisibleDataKey(BTLPVelocityDataKeys.permission("bungeetablistplus.seevanished"))
                .component(new ComponentSpec("!players_by_server", PlayersByServerComponentConfiguration.class))
                .build();
        yaml = ConfigTabOverlayManager.constructYamlInstance(options);

        if (readMainConfig())
            return;

        velocityPlayerProvider = new VelocityPlayerProvider(mainThreadExecutor);

        hiddenPlayersManager = new HiddenPlayersManager();
        hiddenPlayersManager.addVanishProvider("/btlp hide", BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_COMMAND);
        hiddenPlayersManager.addVanishProvider("config.yml (hiddenPlayers)", BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_CONFIG);
        hiddenPlayersManager.addVanishProvider("config.yml (hiddenServers)", BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_SERVER_CONFIG);
        hiddenPlayersManager.addVanishProvider("VanishNoPacket", BukkitData.VanishNoPacket_IsVanished);
        hiddenPlayersManager.addVanishProvider("SuperVanish", BukkitData.SuperVanish_IsVanished);
        hiddenPlayersManager.addVanishProvider("CMI", BukkitData.CMI_IsVanished);
        hiddenPlayersManager.addVanishProvider("Essentials", BukkitData.Essentials_IsVanished);
        hiddenPlayersManager.addVanishProvider("Bukkit Player Metadata `vanished`", BukkitData.BukkitPlayerMetadataVanished);
        hiddenPlayersManager.addVanishProvider("Sponge VANISH", SpongeData.Sponge_IsVanished);
        hiddenPlayersManager.enable();

        fakePlayerManagerImpl = new FakePlayerManagerImpl(plugin, iconManager, mainThreadExecutor);

        List<PlayerProvider> playerProviders = new ArrayList<>();
        if (getProxy().getPluginManager().getPlugin("redisbungee").isPresent()) {
            redisPlayerManager = new RedisPlayerManager(velocityPlayerProvider, this, logger);
            playerProviders.add(redisPlayerManager);
            plugin.getLogger().info("Hooked RedisBungee");
        }
        playerProviders.add(velocityPlayerProvider);
        playerProviders.add(fakePlayerManagerImpl);
        this.playerProvider = new JoinedPlayerProvider(playerProviders);

        getProxy().getChannelRegistrar().register(channelIdentifier);
        bukkitBridge = new BukkitBridge(asyncExecutor, mainThreadExecutor, playerPlaceholderResolver, serverPlaceholderResolver, getPlugin(), logger, velocityPlayerProvider, this, cache);
        serverStateManager = new ServerStateManager(config, plugin);
        dataManager = new DataManager(api, plugin, logger, velocityPlayerProvider, mainThreadExecutor, serverStateManager, bukkitBridge);
        dataManager.addCompositeDataProvider(hiddenPlayersManager);
        dataManager.addCompositeDataProvider(new PermissionDataProvider());

        updateExcludedAndHiddenServerLists();

        // register commands and update Notifier
        CommandManager commandManager = getProxy().getCommandManager();
        commandManager.register(new CommandBungeeTabListPlus(plugin).register("bungeetablistplus"));
        commandManager.register(new CommandBungeeTabListPlus(plugin).register("btlp"));

        getProxy().getScheduler().buildTask(plugin, new UpdateNotifier(this)).delay(15, TimeUnit.MINUTES).repeat(15, TimeUnit.MINUTES).schedule();

        // Load updateCheck thread
        if (config.checkForUpdates) {
            updateChecker = new UpdateChecker(plugin);
            plugin.getLogger().info("Starting UpdateChecker Task");
            getProxy().getScheduler().buildTask(plugin, updateChecker).delay(0, TimeUnit.MINUTES).repeat(UpdateChecker.interval, TimeUnit.MINUTES).schedule();
        }

        int[] serversHash = {getProxy().getAllServers().hashCode()};

        getProxy().getScheduler().buildTask(plugin, () -> {
            int hash = getProxy().getAllServers().hashCode();
            if (hash != serversHash[0]) {
                serversHash[0] = hash;
                scheduleSoftReload();
            }
        }).delay(1, TimeUnit.MINUTES).repeat(1, TimeUnit.MINUTES).schedule();

        MyPlatform platform = new MyPlatform();
        configTabOverlayManager = new ConfigTabOverlayManager(platform,
                playerProvider,
                playerPlaceholderResolver,
                ImmutableList.of(new ServerCountPlaceholderResolver(dataManager),
                        new GlobalServerPlaceholderResolver(dataManager, serverPlaceholderResolver)),
                yaml,
                options,
                logger,
                mainThreadExecutor,
                iconManager);
        spectatorPassthroughTabOverlayManager = new SpectatorPassthroughTabOverlayManager(platform, mainThreadExecutor, BTLPVelocityDataKeys.DATA_KEY_GAMEMODE);
        if (config.disableCustomTabListForSpectators) {
            spectatorPassthroughTabOverlayManager.enable();
        } else {
            spectatorPassthroughTabOverlayManager.disable();
        }

        updateTimeZoneAndGlobalCustomPlaceholders();

        Path tabLists = getPlugin().getDataDirectory().resolve("tabLists");
        if (!Files.exists(tabLists)) {
            try {
                Files.createDirectories(tabLists);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to create tabLists directory", e);
                return;
            }
            try {
                Files.copy(getClass().getClassLoader().getResourceAsStream("default.yml"), tabLists.resolve("default.yml"));
            } catch (IOException e) {
                plugin.getLogger().warn("Failed to save default config.", e);
            }
        }
        configTabOverlayManager.reloadConfigs(ImmutableSet.of(tabLists));

        getProxy().getEventManager().register(plugin, new TabListListener(this));

        GeyserCompat.init();
    }

    private void updateTimeZoneAndGlobalCustomPlaceholders() {
        configTabOverlayManager.setTimeZone(config.getTimeZone());

        if (config.customPlaceholders != null) {
            val customPlaceholders = new HashMap<String, CustomPlaceholderConfiguration>();
            for (val entry : config.customPlaceholders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    customPlaceholders.put(entry.getKey(), entry.getValue());
                }
            }
            configTabOverlayManager.setGlobalCustomPlaceholders(customPlaceholders);
        }
    }

    private void updateExcludedAndHiddenServerLists() {
        excludedServers = new MatchingStringsCollection(
                config.excludeServers != null
                        ? config.excludeServers
                        : Collections.emptyList()
        );
        ExcludedServersTabOverlayProvider.onReload();
        dataManager.setHiddenServers(new MatchingStringsCollection(
                config.hiddenServers != null
                        ? config.hiddenServers
                        : Collections.emptyList()
        ));
        dataManager.setPermanentlyHiddenPlayers(config.hiddenPlayers != null ? config.hiddenPlayers : Collections.emptyList());
    }

    // get jar file path
    private String getJarName(){
        try {
            Class<?> klass = BungeeTabListPlus.class;
            URL location = klass.getResource('/' + klass.getName().replace('.', '/') + ".class");
            if(location == null) return null;
            String jarName = URLDecoder.decode(location.getFile(), "UTF8").split("!")[0];
            return jarName.substring(jarName.lastIndexOf("/"));
        } catch (Exception e) {
            return null;
        }
    }

    private void extractDefaultIcons(File headsFolder) {
        if (!headsFolder.exists()) {
            headsFolder.mkdirs();

            try {
                String jarName = getJarName();
                if(jarName == null){
                    throw new IOException("Failed to get jar name from class path");
                }

                // copy default heads
                ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(new File("plugins/" + jarName).toPath()));

                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().startsWith("heads/")) {
                        try {
                            File targetFile = new File(plugin.getDataDirectory().toFile(), entry.getName());
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
    }

    private boolean readMainConfig() {
        try {
            File file = new File(plugin.getDataDirectory().toFile(), "config.yml");
            if (!file.exists()) {
                config = new MainConfig();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                config.writeWithComments(writer, yaml);
            } else {
                ErrorHandler.set(new ErrorHandler());
                config = yaml.loadAs(new FileInputStream(file), MainConfig.class);
                ErrorHandler errorHandler = ErrorHandler.get();
                ErrorHandler.set(null);
                if (!errorHandler.getEntries().isEmpty()) {
                    plugin.getLogger().warn(errorHandler.formatErrors(file.getName()));
                }
                if (config.needWrite) {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
                    config.writeWithComments(writer, yaml);
                }
            }
        } catch (IOException | YAMLException ex) {
            plugin.getLogger().warn("Unable to load config.yml", ex);
            return true;
        }
        return false;
    }

    public void onDisable() {
        // save cache
        cache.save();
        plugin.getProxy().getScheduler().tasksByPlugin(plugin).forEach(ScheduledTask::cancel);
        mainThreadExecutor.shutdownGracefully();
        asyncExecutor.shutdownGracefully();
    }

    /**
     * Reloads most settings of the plugin
     */
    public boolean reload() {
        fakePlayerManagerImpl.removeConfigFakePlayers();

        if (readMainConfig()) {
            plugin.getLogger().warn("Unable to reload Config");
            return false;
        } else {
            updateExcludedAndHiddenServerLists();
            updateTimeZoneAndGlobalCustomPlaceholders();

            // clear cache to force image files to be read from disk again
            iconManager.clearCache();

            Path tabLists = getPlugin().getDataDirectory().resolve("tabLists");
            configTabOverlayManager.reloadConfigs(ImmutableSet.of(tabLists));

            fakePlayerManagerImpl.reload();

            serverStateManager.updateConfig(config);

            if (config.disableCustomTabListForSpectators) {
                spectatorPassthroughTabOverlayManager.enable();
            } else {
                spectatorPassthroughTabOverlayManager.disable();
            }
            return true;
        }
    }

    public void scheduleSoftReload() {
        if (!scheduledSoftReload) {
            scheduledSoftReload = true;
            asyncExecutor.execute(this::softReload);
        }
    }

    private void softReload() {
        scheduledSoftReload = false;

        if (configTabOverlayManager != null) {
            configTabOverlayManager.refreshConfigs();

            // this is a good time to save the cache
            asyncExecutor.execute(cache::save);
        }
    }

    @Deprecated
    public final void failIfNotMainThread() {
        if (!mainThreadExecutor.inEventLoop()) {
            getLogger().log(Level.SEVERE, "Not in main thread", new IllegalStateException("Not in main thread"));
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
     * Checks whether an update for BungeeTabListPlus is available. Actually
     * the check is performed in a background task and this only returns the
     * result.
     *
     * @return true if a newer version of BungeeTabListPlus is available
     */
    public boolean isUpdateAvailable() {
        return updateChecker != null && updateChecker.isUpdateAvailable();
    }

    public boolean isNewDevBuildAvailable() {
        return updateChecker != null && updateChecker.isNewDevBuildAvailable();
    }

    public void reportError(Throwable th) {
        plugin.getLogger().error("An internal error occurred! Please send the "
                        + "following StackTrace to the developer in order to help"
                        + " resolving the problem",
                th);
    }

    public com.velocitypowered.api.proxy.ProxyServer getProxy() {
        return plugin.getProxy();
    }

    private final class MyPlatform implements Platform {

        @Override
        public void addEventListener(EventListener listener) {
            BungeeTabListPlus.this.listeners.add(listener);
        }
    }
}
