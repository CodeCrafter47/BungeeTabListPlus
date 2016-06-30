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
import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.player.Player;
import net.md_5.bungee.api.connection.Server;

import java.util.Optional;

public class ConditionalPlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bind("insertIfGamemode3").withTemplateArgs().to((context, args) -> context.getPlayer().getGameMode() == 3 ? args : SlotTemplate.empty());
        bind("insertIfAFK").withTemplateArgs().to((context, args) -> ((Player) context.getPlayer()).get(DataKeys.Essentials_IsAFK).orElse(false) ? args : SlotTemplate.empty());
        bind("insertIfNotAFK").withTemplateArgs().to((context, args) -> !((Player) context.getPlayer()).get(DataKeys.Essentials_IsAFK).orElse(false) ? args : SlotTemplate.empty());
        bind("insertIfHidden").withTemplateArgs().to((context, args) -> BungeeTabListPlus.isHidden((Player) context.getPlayer()) ? args : SlotTemplate.empty());
        bind("insertIfServersSame").withTemplateArgs().to((context, args) -> {
            Optional<ServerGroup> serverGroup = context.getServerGroup();
            Server server = context.getViewer().getServer();
            if (server != null && serverGroup.isPresent() && serverGroup.get().getServerNames().contains(server.getInfo().getName()))
                return args;
            else return SlotTemplate.empty();
        });
        bind("insertIfServersDifferent").withTemplateArgs().to((context, args) -> {
            Optional<ServerGroup> serverGroup = context.getServerGroup();
            Server server = context.getViewer().getServer();
            return server != null && (!serverGroup.isPresent() || !serverGroup.get().getServerNames().contains(server.getInfo().getName())) ? args : SlotTemplate.empty();
        });
    }
}
