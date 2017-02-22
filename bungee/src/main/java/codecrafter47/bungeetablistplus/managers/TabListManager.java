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
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListProvider;
import codecrafter47.bungeetablistplus.config.Config;
import codecrafter47.bungeetablistplus.config.ITabListConfig;
import codecrafter47.bungeetablistplus.config.old.ConfigParser;
import codecrafter47.bungeetablistplus.config.old.TabListConfig;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.expression.ExpressionResult;
import codecrafter47.bungeetablistplus.tablistproviders.legacy.CheckedTabListProvider;
import codecrafter47.bungeetablistplus.tablistproviders.legacy.IConfigTabListProvider;
import codecrafter47.bungeetablistplus.yamlconfig.YamlConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.val;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class TabListManager {

    private final BungeeTabListPlus plugin;
    private final List<IConfigTabListProvider> tabLists = new ArrayList<>();
    private final List<Config> configs = new ArrayList<>();
    @Getter
    private ImmutableList<String> filesNeedingUpgrade = ImmutableList.of();

    public Map<ProxiedPlayer, TabListProvider> customTabLists = new HashMap<>();

    public TabListManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    // returns true on success
    public boolean loadTabLists() {
        File tablistFolder = new File(plugin.getPlugin().getDataFolder(), "tabLists");
        if (!tablistFolder.exists()) {
            tablistFolder.mkdirs();
            try {
                FileOutputStream outputStream = new FileOutputStream(new File(tablistFolder, "default.yml"));
                ByteStreams.copy(getClass().getClassLoader().getResourceAsStream("default.yml"), outputStream);
                outputStream.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save default config.", e);
            }
        }

        val needUpgrade = ImmutableList.<String>builder();
        for (File file : tablistFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                try {
                    plugin.getLogger().log(Level.INFO, "Loading {0}", file.getName());

                    ITabListConfig tabListConfig = Objects.requireNonNull(YamlConfig.read(new FileInputStream(file), ITabListConfig.class));

                    if (tabListConfig instanceof TabListConfig) {
                        needUpgrade.add(file.getName());
                        plugin.getLogger().log(Level.WARNING, "{0} needs to be updated, see https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating", file.getName());
                        TabListConfig c = (TabListConfig) tabListConfig;
                        plugin.getPlaceholderAPIHook().searchTabList(c);
                        validateShowTo(c);
                        this.tabLists.add(new ConfigParser(plugin, c.tab_size).parse(c));
                    } else {
                        configs.add((Config) tabListConfig);
                    }
                } catch (Throwable ex) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load " + file.getName(), ex);
                }
            }
        }
        this.filesNeedingUpgrade = needUpgrade.build();
        return true;
    }

    public Config getNewConfigForContext(Context context) {
        Config config = null;
        int priority = Integer.MIN_VALUE;
        for (Config config1 : configs) {
            if (config1.getPriority() > priority && config1.getShowTo().evaluate(context, ExpressionResult.BOOLEAN)) {
                config = config1;
                priority = config.getPriority();
            }
        }
        return config;
    }

    public TabListProvider getTabListForPlayer(ProxiedPlayer player) {
        if (customTabLists.get(player) != null) return customTabLists.get(player);
        TabListProvider provider = null;
        int priority = Integer.MIN_VALUE;
        for (IConfigTabListProvider tabList : tabLists) {
            if (tabList.appliesTo(player)) {
                if (tabList.getPriority() > priority) {
                    priority = tabList.getPriority();
                    provider = tabList;
                }
            }
        }
        if (provider != null) {
            return provider;
        }
        return null;
    }

    private void validateShowTo(TabListConfig config) {
        String showTo = config.showTo;

        if (showTo.equalsIgnoreCase("ALL")) {
            return;
        }

        if (showTo.equalsIgnoreCase("1.7")) {
            return;
        }

        if (showTo.equalsIgnoreCase("1.8")) {
            return;
        }

        String[] s = showTo.split(":");

        if (s.length != 2) {
            invalidShowTo(config);
            return;
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
            if (plugin.isServer(s[1])) {
                return;
            } else {

                invalidShowTo(config);
            }
        }

        if (s[0].equalsIgnoreCase("servers")) {
            for (String sv : s[1].split(",")) {
                if (!plugin.isServer(sv)) {

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
        plugin.getLogger().log(
                Level.WARNING, "{0}{1}: showTo is partly or completely invalid",
                new Object[]{ChatColor.RED,
                        config.getName()});
    }

    public void setCustomTabList(ProxiedPlayer player, TabListProvider tabList) {
        if (!(tabList instanceof CheckedTabListProvider)) {
            setCustomTabList(player, new CheckedTabListProvider(tabList));
        }
        customTabLists.put(player, tabList);
    }

    public void removeCustomTabList(ProxiedPlayer player) {
        customTabLists.remove(player);
    }
}
