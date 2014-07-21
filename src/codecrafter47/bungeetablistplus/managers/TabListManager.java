package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.config.ConfigParser;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import codecrafter47.bungeetablistplus.config.TabListProvider;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TabListManager {

    private final BungeeTabListPlus plugin;
    TabListProvider defaultTab;
    List<TabListProvider> tabLists = new ArrayList<>();

    public TabListManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    // returns true on succes
    public boolean loadTabLists() {
        try {
            if (!plugin.getConfigManager().defaultTabList.showTo.
                    equalsIgnoreCase("all")) {
                plugin.getLogger().warning(
                        "The default tabList is configured not to be shown by default");
                plugin.getLogger().warning(
                        "This is not recommended and you should not do this if you're not knowing exaclty what you are doing");
            }
            validateShowTo(plugin.getConfigManager().defaultTabList);
            defaultTab = new ConfigParser(
                    plugin.getConfigManager().defaultTabList, plugin).parse();
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not load default tabList");
            plugin.getLogger().log(Level.WARNING, null, ex);
            plugin.getLogger().log(Level.WARNING, "Disabling plugin");
            return false;
        }
        for (TabListConfig c : plugin.getConfigManager().tabLists) {
            try {
                validateShowTo(c);
                tabLists.add(new ConfigParser(c, plugin).parse());
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.WARNING, "Could not load {0}", c.
                        getFileName());
                plugin.getLogger().log(Level.WARNING, null, ex);
            }
        }
        return true;
    }

    public TabListProvider getTabListForPlayer(ProxiedPlayer player) {
        for (TabListProvider tabList : tabLists) {
            if (tabList.appliesTo(player)) {
                return tabList;
            }
        }
        if (defaultTab.appliesTo(player)) {
            return defaultTab;
        }
        return null;
    }

    private void validateShowTo(TabListConfig config) {
        String showTo = config.showTo;

        if (showTo.equalsIgnoreCase("ALL")) {
            return;
        }

        String s[] = showTo.split(":");

        if (s.length != 2) {
            invalidShowTo(config);
        }

        if (s[0].equalsIgnoreCase("player")) {
            if (s[1].contains(",")) {
                invalidShowTo(config);
            }
            return;
        }

        if (s[0].equalsIgnoreCase("players")) {
            return;
        }

        if (s[0].equalsIgnoreCase("server")) {
            if (s[1].contains(",")) {
                invalidShowTo(config);
            }
            if (plugin.getPlayerManager().isServer(s[1])) {
                return;
            } else {

                invalidShowTo(config);
            }
        }

        if (s[0].equalsIgnoreCase("servers")) {
            for (String sv : s[1].split(",")) {
                if (!plugin.getPlayerManager().isServer(sv)) {

                    invalidShowTo(config);
                }
            }
            return;
        }

        if (s[0].equalsIgnoreCase("group")) {
            if (s[1].contains(",")) {
                invalidShowTo(config);
            }
            return;
        }

        if (s[0].equals("groups")) {
            return;
        }

        invalidShowTo(config);
    }

    private void invalidShowTo(TabListConfig config) {
        plugin.getLogger().warning(
                ChatColor.RED + config.getFileName() + ": showTo is partly or completly invalid");
    }
}
