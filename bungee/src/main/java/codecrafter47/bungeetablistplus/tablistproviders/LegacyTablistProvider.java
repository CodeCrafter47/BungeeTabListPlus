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

package codecrafter47.bungeetablistplus.tablistproviders;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListProvider;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.tablist.GenericTabList;
import codecrafter47.bungeetablistplus.tablist.GenericTabListContext;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class LegacyTablistProvider implements TablistProvider {
    public static LegacyTablistProvider INSTANCE = new LegacyTablistProvider();

    private LegacyTablistProvider() {
    }

    @Override
    public void onActivated(PlayerTablistHandler tablist) {
        BungeeTabListPlus.getInstance().updateTabListForPlayer(tablist.getPlayer());
    }

    @Override
    public void onDeactivated(PlayerTablistHandler tablist) {
    }

    public void update(PlayerTablistHandler playerTablistHandler) {
        ProxiedPlayer player = playerTablistHandler.getPlayer();
        ConnectedPlayer connectedPlayer = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayerIfPresent(player);
        if (connectedPlayer == null) {
            return;
        }
        Server server = player.getServer();
        if (server != null && (BungeeTabListPlus.getInstance().getConfig().excludeServers.contains(server.getInfo().getName()))) {
            playerTablistHandler.runInEventLoop(() -> {
                playerTablistHandler.setPassThrough(true);
            });
            return;
        }

        TabListProvider tlp = BungeeTabListPlus.getInstance().
                getTabListManager().getTabListForPlayer(player);
        if (tlp == null) {
            playerTablistHandler.runInEventLoop(() -> {
                playerTablistHandler.setPassThrough(true);
            });
            return;
        }

        TabList tabList;

        if (BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player)) {
            int wishedTabListSize = tlp.getWishedTabListSize();
            if (wishedTabListSize < 1) {
                wishedTabListSize = 1;
            }
            if (wishedTabListSize > 80) {
                wishedTabListSize = 80;
            }
            int columns = (wishedTabListSize + 19) / 20;
            tabList = new GenericTabList(wishedTabListSize / columns, columns);
        } else {
            tabList = new GenericTabList();
        }

        TabListContext context = new GenericTabListContext(tabList.getRows(), tabList.getColumns(), player, BungeeTabListPlus.getInstance().constructPlayerManager(player));

        context = context.setPlayer(connectedPlayer);

        tlp.fillTabList(player, tabList, context);

        playerTablistHandler.runInEventLoop(() -> {
            playerTablistHandler.sendTabList(tabList);
        });
    }
}
