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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.config.MainConfig;
import codecrafter47.bungeetablistplus.config.Messages;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigManager {

    private MainConfig config;
    private Messages messages;
    public TabListConfig defaultTabList;
    public final List<TabListConfig> tabLists = new ArrayList<>();

    public ConfigManager(Plugin plugin) throws IOException {
        loadConfig(plugin);
        checkForInconsistentTabListSize(plugin);
    }

    private void loadConfig(Plugin plugin) throws IOException {
        setMainConfig(new MainConfig());
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (file.exists()) {
            config.read(file);
        }
        config.write(file);
        validateConfig();
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (messageFile.exists()) {
            messages = new Messages();
            messages.read(messageFile);
        }

        try {
            Class.forName("net.md_5.bungee.api.chat.ComponentBuilder");
        } catch (Exception ignored) {
            if (messages == null) {
                messages = new Messages();
                messages.read(messageFile);
                messages.write(messageFile);
            }
        }
        // Load default TabList
        defaultTabList = new TabListConfig("default.yml");
        File tabListDir = new File(plugin.getDataFolder(), "tabLists");
        if (!tabListDir.exists()) {
            tabListDir.mkdir();
        }
        file = new File(tabListDir, "default.yml");
        if (file.exists()) {
            defaultTabList.read(file);
        }
        defaultTabList.write(file);
        // Load other TabLists
        for (String s : new File("plugins" + File.separator
                + plugin.getDescription().getName() + File.separator
                + "tabLists").list()) {
            if (s.endsWith(".yml") && !s.equals("default.yml")) {
                try {
                    file = new File(tabListDir, s);
                    TabListConfig listConfig = new TabListConfig(s);
                    listConfig.read(file);
                    listConfig.write(file);
                    this.tabLists.add(listConfig);
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.WARNING, "Unable to load " + s, ex);
                }
            }
        }
    }

    private void checkForInconsistentTabListSize(Plugin plugin) {
        if (plugin.getProxy().getConfig().getListeners().stream().map(ListenerInfo::getTabListSize).collect(Collectors.toSet()).size() > 1) {
            plugin.getLogger().warning("Inconsistent tab list size detected. Please make sure to set tab_size to the same value for all listeners.");
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

    public static int getTabSize() {
        return ProxyServer.getInstance().getConfig().getListeners().iterator().next().getTabListSize();
    }

    public static int getCols() {
        return (getTabSize() + 19) / 20;
    }

    public static int getRows() {
        return getCols() == 0 ? 0 : getTabSize() / getCols();
    }

    private void validateConfig() {
        if (!config.permissionSource.equalsIgnoreCase("AUTO") && !config.permissionSource.
                equalsIgnoreCase("BUKKIT") && !config.permissionSource.
                equalsIgnoreCase("BUNGEE") && !config.permissionSource.
                equalsIgnoreCase("BUNGEEPERMS") && !config.permissionSource.
                equalsIgnoreCase("BUKKITPERMISSIONSEX") && !config.permissionSource.
                equalsIgnoreCase("CUSTOMPLUGIN")) {
            BungeeTabListPlus.getInstance().getPlugin().getLogger().warning(
                    "CONFIG-ERROR: Unknown value for 'permissionSource'");
        }
    }
}
