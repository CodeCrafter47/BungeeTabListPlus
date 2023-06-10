/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.bukkitbridge.placeholderapi;

import com.google.common.collect.Lists;
import de.codecrafter47.data.api.DataAccess;
import de.codecrafter47.data.bukkit.AbstractBukkitDataAccess;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PlaceholderAPIHook {
    private final Plugin plugin;

    public PlaceholderAPIHook(Plugin plugin) {
        this.plugin = plugin;
    }

    public AbstractBukkitDataAccess<Player> getDataAccess() {
        return new PlaceholderAPIDataAccess(plugin.getLogger(), plugin);
    }

    public List<String> getRegisteredPlaceholderPlugins() {
        return Lists.newArrayList(PlaceholderAPI.getRegisteredPlaceholderPlugins());
    }
}
