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

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;

import java.util.Arrays;

public class PlayerCountPlaceholder extends PlaceholderProvider {
    @Override
    public void setup() {
        bind("server_player_count").to(context -> String.format("%d", context.getServerGroup().map(group -> context.getPlayerManager().getPlayerCount(group.getServerNames(), context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3)).orElse(0)));
        bind("player_count").alias("gcout").to(context -> String.format("%d", context.getPlayerManager().getGlobalPlayerCount(context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3)));
        bind("players").alias("rplayers").withArgs().to((context, args) -> {
            if (args == null) {
                return Integer.toString(context.getPlayerManager().getGlobalPlayerCount(context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3));
            } else
                return Integer.toString(context.getPlayerManager().getPlayerCount(Arrays.asList(args.split(",|\\+")), context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3));
        });
    }
}
