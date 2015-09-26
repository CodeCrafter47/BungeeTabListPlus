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

package codecrafter47.bungeetablistplus.data.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class VaultBalanceProvider extends VaultDataProvider<Player, Double> {
    private final Plugin plugin;

    public VaultBalanceProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    public Double apply0(Player player) {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            Economy economy = rsp.getProvider();
            if (economy != null && economy.isEnabled()) {
                if (economy.getName().equals("Gringotts")) {
                    try {
                        return Bukkit.getScheduler().callSyncMethod(plugin, () -> economy.getBalance(player)).get();
                    } catch (InterruptedException | ExecutionException e) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to query balance for player " + player.getName(), e);
                    }
                } else {
                    return economy.getBalance(player);
                }
            }
        }
        return null;
    }
}
