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
import codecrafter47.bungeetablistplus.api.PlaceholderProvider;
import codecrafter47.bungeetablistplus.common.Constants;
import codecrafter47.bungeetablistplus.common.PlaceholderAPIDataKey;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.tablist.SlotBuilder;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderAPIHook implements Listener {
    private final BungeeTabListPlus bungeeTabListPlus;
    private final Set<String> registeredPlaceholders = Sets.newConcurrentHashSet();
    private final Set<String> placeholdersToCheck = Sets.newConcurrentHashSet();

    private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("%((\\p{Alnum}|_|.|-|@|-)+)%");

    public PlaceholderAPIHook(BungeeTabListPlus bungeeTabListPlus) {
        this.bungeeTabListPlus = bungeeTabListPlus;
        bungeeTabListPlus.getPlugin().getProxy().getPluginManager().registerListener(bungeeTabListPlus.getPlugin(), this);
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        Server server = event.getServer();
        askForPlaceholders(server);
    }

    public void askForPlaceholders(Server server) {
        for (String placeholder : placeholdersToCheck) {
            if (!registeredPlaceholders.contains(placeholder)) {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(os);
                    out.writeUTF(Constants.subchannelPlaceholder);
                    out.writeUTF(placeholder);
                    out.close();
                    server.sendData(Constants.channel, os.toByteArray());
                } catch (Throwable th) {
                    bungeeTabListPlus.reportError(th);
                }
            }
        }
    }

    public void onPlaceholderConfirmed(String placeholder) {
        if (!registeredPlaceholders.contains(placeholder)) {
            registeredPlaceholders.add(placeholder);
            bungeeTabListPlus.registerPlaceholderProvider(new PlaceholderProvider() {
                @Override
                public void setup() {
                    bindRegex(Pattern.quote(placeholder)).to((placeholderManager, matcher) -> new SlotTemplate() {
                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return builder.appendText(bungeeTabListPlus.getBridge().get(context.getPlayer(), new PlaceholderAPIDataKey(placeholder)).orElse(""));
                        }
                    });
                }
            });
        }
    }

    public void onLoad() {
        ConfigManager configManager = bungeeTabListPlus.getConfigManager();
        if (configManager != null) {
            searchTabList(configManager.defaultTabList);
            for (TabListConfig tabList : configManager.tabLists) {
                searchTabList(tabList);
            }
        }
        for (ProxiedPlayer player : bungeeTabListPlus.getProxy().getPlayers()) {
            Server server = player.getServer();
            if (server != null) {
                askForPlaceholders(server);
            }
        }

    }

    private void searchTabList(TabListConfig config) {
        config.header.forEach(this::searchString);
        config.footer.forEach(this::searchString);
        config.playerLines.forEach(this::searchString);
        config.morePlayersLines.forEach(this::searchString);
        config.groupLines.forEach(this::searchString);
        config.tabList.forEach(this::searchString);

    }

    private void searchString(String s) {
        Matcher matcher = PATTERN_PLACEHOLDER.matcher(s);
        while (matcher.find()) {
            placeholdersToCheck.add(matcher.group());
        }
    }
}
