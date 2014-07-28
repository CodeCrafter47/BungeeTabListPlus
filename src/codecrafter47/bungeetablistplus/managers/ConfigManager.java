package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.config.MainConfig;
import codecrafter47.bungeetablistplus.config.Messages;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ConfigManager {

    private MainConfig config;
    private Messages messages;
    TabListConfig defaultTabList;
    List<TabListConfig> tabLists = new ArrayList<>();

    public ConfigManager(BungeeTabListPlus bungeeTabListPlus) throws
            InvalidConfigurationException {
        loadConfig(bungeeTabListPlus);
    }

    private void loadConfig(BungeeTabListPlus plugin) throws
            InvalidConfigurationException {
        setMainConfig(new MainConfig(plugin));
        validateConfig();
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (messageFile.exists()) {
            messages = new Messages(plugin);
        }

        try {
            Thread.currentThread().getContextClassLoader().loadClass(
                    "net.md_5.bungee.api.chat.ComponentBuilder");
        } catch (Exception ex) {
            if (messages == null) {
                messages = new Messages(plugin);
            }
        }
        // Load default TabList
        defaultTabList = new TabListConfig(plugin, "default.yml");
        // Load other TabLists
        for (String s : new File("plugins" + File.separator
                + plugin.getDescription().getName() + File.separator
                + "tabLists").list()) {
            if (s.endsWith(".yml") && !s.equals("default.yml")) {
                try {
                    tabLists.add(new TabListConfig(plugin, s));
                } catch (InvalidConfigurationException ex) {
                    plugin.getLogger().log(Level.WARNING, "Unable to load {0}",
                            s);
                    plugin.getLogger().log(Level.WARNING, null, ex);
                }
            }
        }
    }

    public TabListConfig getTabList(ProxiedPlayer player) {
        for (TabListConfig tabList : tabLists) {
            if (tabList.appliesTo(player)) {
                return tabList;
            }
        }
        return defaultTabList;
    }

    public MainConfig getMainConfig() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }

    private void setMainConfig(MainConfig config) {
        this.config = config;
    }

    // TODO optimize
    public static int getTabSize() {
        //return ProxyServer.getInstance().getConfigurationAdapter().getInt("listeners.tab_size", 60);
        return ProxyServer.getInstance().getConfigurationAdapter().
                getListeners().iterator().next().getTabListSize();
    }

    public static int getCols() {
        return (getTabSize() + 19) / 20;
    }

    public static int getRows() {
        return getTabSize() / getCols();
    }

    private void validateConfig() {
        if (config.permissionSource.equalsIgnoreCase("AUTO") || config.permissionSource.
                equalsIgnoreCase("BUKKIT") || config.permissionSource.
                equalsIgnoreCase("BUNGEE") || config.permissionSource.
                equalsIgnoreCase("BUNGEEPERMS")) {

        } else {
            BungeeTabListPlus.getInstance().getLogger().warning(
                    "CONFIG-ERROR: Unknown value for 'permissionSource'");
        }
    }
}
