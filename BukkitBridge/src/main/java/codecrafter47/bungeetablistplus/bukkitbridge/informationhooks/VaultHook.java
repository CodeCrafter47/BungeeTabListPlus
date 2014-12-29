/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.bukkitbridge.informationhooks;

import codecrafter47.bungeetablistplus.bukkitbridge.BukkitBridge;
import codecrafter47.bungeetablistplus.bukkitbridge.api.PlayerInformationProvider;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Florian Stober
 */
public class VaultHook implements PlayerInformationProvider {

    BukkitBridge plugin;

    public VaultHook(BukkitBridge plugin) {
        this.plugin = plugin;
        setupPermissions();
        setupChat();
        setupEconomy();
    }

    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chat = null;

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.
                getServer().getServicesManager().getRegistration(
                        net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().
                getServicesManager().getRegistration(
                        net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().
                getServicesManager().getRegistration(
                        net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    @Override
    public Map<String, Object> getInformation(Player player) {
        Map<String, Object> map = new HashMap<>();
        try {
            if (permission != null && permission.hasGroupSupport() && permission.getPrimaryGroup(player) != null) {
                map.put("group", permission.getPrimaryGroup(player));
            }
        } catch (Throwable th) {
            plugin.reportError(th);
        }
        try {
            if (chat != null) {
                map.put("prefix", chat.getPlayerPrefix(player));
                map.put("suffix", chat.getPlayerSuffix(player));
            }
        } catch (Throwable th) {
            plugin.reportError(th);
        }
        try {
            if (economy != null) {
                map.put("balance", economy.getBalance(player.getName()));
                map.put("currency", economy.currencyNameSingular());
                map.put("currencyPl", economy.currencyNamePlural());
            }
        } catch (Throwable th) {
            plugin.reportError(th);
        }
        return map;
    }
}
