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

package codecrafter47.bungeetablistplus.spongebridge.placeholderapi;

import codecrafter47.bungeetablistplus.spongebridge.SpongePlugin;
import de.codecrafter47.data.api.DataAccess;
import me.rojo8399.placeholderapi.impl.placeholder.Store;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

public class PlaceholderAPIHook {

    private final SpongePlugin plugin;

    public PlaceholderAPIHook(SpongePlugin plugin) {
        this.plugin = plugin;
    }

    public DataAccess<Player> getDataAccess() {
        return new PlaceholderAPIDataAccess(plugin.getLogger());
    }

    public List<String> getRegisteredPlaceholderPlugins() {
        return Store.get().allIds();
    }
}
