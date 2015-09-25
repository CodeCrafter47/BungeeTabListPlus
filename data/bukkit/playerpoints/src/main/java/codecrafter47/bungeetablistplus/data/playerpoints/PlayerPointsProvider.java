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

package codecrafter47.bungeetablistplus.data.playerpoints;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerPointsProvider implements Function<Player, Integer> {
    private final Logger logger;

    public PlayerPointsProvider(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Integer apply(Player player) {
        PlayerPointsAPI playerPoints = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();
        for (int i = 0; i < 5; i++) {
            try {
                return playerPoints.look(player.getUniqueId());
            } catch (Throwable th) {
                if (i == 4) {
                    logger.log(Level.SEVERE, "Failed to query PlayerPoints for " + player.getName() + ". Attempt 5", th);
                }
            }
        }
        return null;
    }
}
