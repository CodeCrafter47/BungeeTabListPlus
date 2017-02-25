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
import codecrafter47.bungeetablistplus.config.Config;
import codecrafter47.bungeetablistplus.config.ITabListConfig;
import codecrafter47.bungeetablistplus.config.UnsupportedConfig;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.expression.ExpressionResult;
import codecrafter47.bungeetablistplus.yamlconfig.YamlConfig;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class TabListManager {

    private final BungeeTabListPlus plugin;
    private final List<Config> configs = new ArrayList<>();

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

        for (File file : tablistFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                try {
                    plugin.getLogger().log(Level.INFO, "Loading {0}", file.getName());

                    ITabListConfig tabListConfig = Objects.requireNonNull(YamlConfig.read(new FileInputStream(file), ITabListConfig.class));

                    if (tabListConfig instanceof UnsupportedConfig) {
                        plugin.getLogger().log(Level.WARNING, "Failed to load {0}. Still using the old format? https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating", file.getName());
                    } else {
                        configs.add((Config) tabListConfig);
                    }
                } catch (Throwable ex) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load " + file.getName(), ex);
                }
            }
        }
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
}
