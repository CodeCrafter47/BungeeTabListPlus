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

package codecrafter47.bungeetablistplus.data.permissionsex;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsExPrefixProvider extends PermissionsExDataProvider<Player, String> {
	public String apply0(Player player) {
		Plugin permissionsex = Bukkit.getPluginManager().getPlugin("PermissionsEx");

		if (permissionsex != null && PermissionsEx.isAvailable()) {
			PermissionUser pu = PermissionsEx.getUser(player);

			if (pu == null)
				return null;

			String pprefix = pu.getOwnPrefix();

			if (pprefix != null && !pprefix.equals(""))
				return pu.getOwnPrefix();

			PermissionGroup maingroup = PermissionsExHelper.getMainPermissionGroupFromRank(pu);

			if (maingroup == null)
				return null;
			return maingroup.getPrefix();
		}
		return null;
	}
}
