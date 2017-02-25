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
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;

public class PermissionManager {

    private final BungeeTabListPlus plugin;

    public PermissionManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }

        try {
            DataKey<Boolean> dataKey = MinecraftData.permission(permission);
            ConnectedPlayer player = plugin.getConnectedPlayerManager().getPlayerIfPresent((ProxiedPlayer) sender);
            if (player != null) {
                Optional<Boolean> has = player.getOpt(dataKey);
                if (has.isPresent()) return has.get();
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }

        return false;
    }
}
