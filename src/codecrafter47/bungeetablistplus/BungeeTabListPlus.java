package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.bridge.Constants;
import codecrafter47.bungeetablistplus.commands.OldSuperCommand;
import codecrafter47.bungeetablistplus.commands.SuperCommand;
import codecrafter47.bungeetablistplus.listener.TabListListener;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.managers.PacketManager;
import codecrafter47.bungeetablistplus.managers.PermissionManager;
import codecrafter47.bungeetablistplus.managers.PlayerManager;
import codecrafter47.bungeetablistplus.managers.TabListManager;
import codecrafter47.bungeetablistplus.managers.VariablesManager;
import codecrafter47.bungeetablistplus.updater.UpdateChecker;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

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
     * provides acces to the configuration
     */
    private ConfigManager config;

    /**
     * provides acces to the Variable Manager use this to add Variables
     */
    private VariablesManager variables;

    private PermissionManager pm;

    private PacketManager packets;

    /**
     * LOL
     */
    private TabListManager tabLists;
    private final TabListListener listener = new TabListListener(this);

    private final SendingQueue resendQueue = new SendingQueue();
    private ResendThread resendThread;

    private ScheduledTask refreshThread;

    private final static Collection<String> hiddenPlayers = new HashSet<>();

    BukkitBridge bukkitBridge;

    UpdateChecker updateChecker = null;

    // Changes:
    // validate config( check sortrules, check for server filter in groupLines, valid showTo(valid first parameter, valid server)
    // improve tablistprovider using timsort
    // vanish no packets support
    // fixed bug: if you put 7 or more characters in bold, the 7th or so character will lose its boldness
    // max for fillplayers use [MAXPLAYERS=11]
    // fixed bug with [ALIGN LEFT][COLUMN=0]{fillplayers}
    // added config entry to select where from to take informations like permissions/prefix/suffix/group
    // new variables {world} and {balance}
    // bukkit bridge
    // - worldplayercount
    // - support for bukkitside tabcolor plugins
    // - support for bukkitside permission plugins
    // - {health}, {level}
    // - faction support: {factionsWhere}, {factionName}, {factionMembersOnline}
    // world fillplayers: {fillplayers:server#world}
    // allow {time:format}
    // more performant variable replacement
    // /BTLP shows whether a update is available
    // added {color} variable which randomly inserts a beautiful colorcode changing every few seconds, the variable inserts the same color in all slots
    // {color:green,red} will insert green or red colorcodes. this changes every second
    // world playercount: {players:server#world}
    // world player count, variable to show number of players on multiple servers like {players:lobby+survival}
    //      --> allow + / - operations in {players:*} variable
    // rework commands (klickable links)
    // EDIT:
    // Also is there a way to sort the automatic {fillplayers} by player count?
    // So that the server with the highest player count always gets displayed at
    // the top? Atm it just puts the servers randomly which makes everything look
    // butchered, since there are big gaps inbetween sometimes
    // ---------------------------------------------
    // 1.7
    // TODO scrolling text
    // TODO multislot scrolling text
    // TODO tabComplete support
    // TODO use bridge version to check whether the plugin is uptodate (newer bridge version)
    // TODO nested variables - actually that is very difficult - and I don't need that
    // TODO Developer api
    // TODO add isOnline
    // TODO better reload (restart resend-thread) - no solution available that fits all bungee versions using this plugin
    // TODO howto use BungeeTablistplus without bungeeperms
    // TODO improve performance in sortrules
    // TODO showTo more flexible
    // TODO uuid support for showTo
    // TODO improve automatic {fillplayers}: It would be nice to have a feature
    // to combine several servers into 1 server, this would be very usefull for
    // a minigames type of server setting, where you have different servers acting
    // for different minigames. This would be helpful to categorize all players
    // under 1 player count
    // TODO two servers share one column.
    // TODO group rank from vault - vault doesn't offer this - maybe from pex?
    // TODO if we have a max for {fillplayers} we might want to have a minimun too! - we don't want this yet; It would be too confusing
    // 1.8
    // TODO connect economy databases
    // TODO add spaces and canIhazmoreSpaces
    // TODO groupplayers:permgroup
    // TODO symbols
    // TODO easter eggs
    // TODO custom config library for more flexibitity
    // TODO [IF ...]
    // TODO lowtraffic mode (no i dont like this)
    // TODO mc1.8
    // TODO tabsize per server
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

        players = new PlayerManager(this);

        tabLists = new TabListManager(this);
        if (!tabLists.loadTabLists()) {
            return;
        }

        packets = new PacketManager();

        if (!packets.isTabModificationSupported()) {
            getLogger().warning("Your BungeeCord Version isn't supported yet");
            getLogger().info("Disabling Plugin");
            return;
        }

        if ((!packets.isScoreboardSupported()) && config.getMainConfig().useScoreboardToBypass16CharLimit) {
            getLogger().warning(
                    "Your BungeeCord Version does not support the following option: 'useScoreboardToBypass16CharLimit'");
            getLogger().warning("This option will be disabled");
            config.getMainConfig().useScoreboardToBypass16CharLimit = false;
        }

        getProxy().registerChannel(Constants.channel);
        bukkitBridge = new BukkitBridge(this);
        bukkitBridge.enable();

        pm = new PermissionManager(this);
        variables = new VariablesManager();

        ProxyServer.getInstance().getPluginManager().registerListener(this,
                listener);

        resendThread = new ResendThread(resendQueue,
                config.getMainConfig().tablistUpdateIntervall);
        getProxy().getScheduler().schedule(this, resendThread, 1,
                TimeUnit.SECONDS);
        startRefreshThread();

        // register commands
        try {
            Thread.currentThread().getContextClassLoader().loadClass(
                    "net.md_5.bungee.api.chat.ComponentBuilder");
            ProxyServer.getInstance().getPluginManager().registerCommand(
                    INSTANCE,
                    new SuperCommand(this));
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
    }

    private void startRefreshThread() {
        if (config.getMainConfig().tablistUpdateIntervall > 0) {
            refreshThread = ProxyServer.getInstance().getScheduler().schedule(
                    INSTANCE, new Runnable() {

                        @Override
                        public void run() {
                            resendTabLists();
                        }
                    },
                    (long) (config.getMainConfig().tablistUpdateIntervall * 1000),
                    (long) (config.getMainConfig().tablistUpdateIntervall * 1000),
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Reloads most settings of the plugin
     */
    public void reload() {
        //getProxy().getScheduler().cancel(refreshThread);
        try {
            config = new ConfigManager(this);
            tabLists = new TabListManager(this);
            if (!tabLists.loadTabLists()) {
                return;
            }
        } catch (InvalidConfigurationException ex) {
            getLogger().warning("Unable to reload Config");
            getLogger().log(Level.WARNING, null, ex);
        }
        //startRefreshThread();
    }

    /**
     * Disables the plugin
     */
    @Override
    public void onDisable() {

        ProxyServer.getInstance().getPluginManager().
                unregisterCommands(INSTANCE);
        ProxyServer.getInstance().getPluginManager().unregisterListeners(
                INSTANCE);
        ProxyServer.getInstance().getScheduler().cancel(INSTANCE);

        tabLists = null;
        variables = null;
        config = null;
        INSTANCE = null;
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
     * Getter of the PacketManager. For internal use only
     *
     * @return an instance of the PacketManager or null
     */
    public PacketManager getPacketManager() {
        return packets;
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
    public static boolean isHidden(ProxiedPlayer player) {
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
        if (isHidden(player)) {
            return;
        }
        synchronized (hiddenPlayers) {
            String name = player.getName();
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
        if (!isHidden(player)) {
            return;
        }
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
        if (updateChecker != null) {
            return updateChecker.isUpdateAvailable();
        }
        return false;
    }

    public void reportError(Throwable th) {
        getLogger().log(Level.WARNING,
                ChatColor.RED + "An internal error occured! Please send the "
                + "following stacktrace to the developer in order to help"
                + " resolving the problem",
                th);
    }
}
