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

package codecrafter47.bungeetablistplus.bukkitbridge.placeholderapi;

import codecrafter47.bungeetablistplus.data.DataAccess;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlaceholderAPIHook {
    private final Plugin plugin;

    public PlaceholderAPIHook(Plugin plugin) {
        this.plugin = plugin;
    }

    public DataAccess<Player> getDataAccess() {
        return new PlaceholderAPIDataAccess(plugin.getLogger(), plugin);
    }

    public boolean isPlaceholder(Player player, String placeholder) {
        return !PlaceholderAPI.setPlaceholders(player, placeholder).equals(placeholder);
    }
}
