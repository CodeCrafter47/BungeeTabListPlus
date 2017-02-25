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

package codecrafter47.bungeetablistplus.bridge;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.placeholder.Placeholder;
import com.google.common.collect.Sets;
import de.codecrafter47.data.api.DataHolder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PlaceholderAPIHook implements Listener {
    private final BungeeTabListPlus bungeeTabListPlus;
    private final Set<String> registeredPlaceholders = Sets.newConcurrentHashSet();
    private final Set<String> placeholdersToCheck = Sets.newConcurrentHashSet();

    public PlaceholderAPIHook(BungeeTabListPlus bungeeTabListPlus) {
        this.bungeeTabListPlus = bungeeTabListPlus;
        bungeeTabListPlus.getPlugin().getProxy().getScheduler().schedule(bungeeTabListPlus.getPlugin(), () -> bungeeTabListPlus.runInMainThread(this::askServersForPlaceholders), 2, 2, TimeUnit.SECONDS);
    }

    public void addMaybePlaceholder(String s) {
        placeholdersToCheck.add("%" + s + "%");
    }

    public void askServersForPlaceholders() {
        try {
            bungeeTabListPlus.getProxy().getServers().values().stream().filter(Objects::nonNull).forEach(this::askForPlaceholders);
        } catch (ConcurrentModificationException ignored) {

        }
    }

    public void askForPlaceholders(ServerInfo server) {
        DataHolder dataHolder = bungeeTabListPlus.getBridge().getServerDataHolder(server.getName());
        Boolean b;
        if (dataHolder != null && null != (b = dataHolder.get(BTLPDataKeys.PLACEHOLDERAPI_PRESENT)) && b) {
            List<String> plugins = dataHolder.get(BTLPDataKeys.PAPI_REGISTERED_PLACEHOLDER_PLUGINS);
            if (plugins != null) {
                for (String placeholder : placeholdersToCheck) {
                    if (!registeredPlaceholders.contains(placeholder)) {
                        String pl = placeholder.split("_")[0].substring(1);
                        if (plugins.stream().anyMatch(s -> s.equalsIgnoreCase(pl))) {
                            if (!registeredPlaceholders.contains(placeholder)) {
                                registeredPlaceholders.add(placeholder);
                                Placeholder.placeholderAPIDataKeys.put(placeholder.substring(1, placeholder.length() - 1), BTLPDataKeys.createPlaceholderAPIDataKey(placeholder));
                                bungeeTabListPlus.reload();
                            }
                        }
                    }
                }
            }
        }
    }
}
