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

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.function.Function;

public abstract class VaultDataProvider<B, R> implements Function<B, R> {
    @Override
    public final R apply(B b) {
        try {
            return apply0(b);
        } catch (Throwable th) {
            throw new RuntimeException("Exception while querying data from Vault\n" + getVaultInfo(), th);
        }
    }

    protected abstract R apply0(B b);

    private String getVaultInfo() {
        StringBuilder info = new StringBuilder();
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null) {
            info.append("Vault ").append(vault.getDescription().getVersion()).append("\n");
            try {
                addPermissionInfo(info);
            } catch (Throwable th) {
                info.append(th.toString());
            }
            try {
                addChatInfo(info);
            } catch (Throwable th) {
                info.append(th.toString());
            }
            try {
                addEconomyInfo(info);
            } catch (Throwable th) {
                info.append(th.toString());
            }
        }
        return info.toString();
    }

    private void addEconomyInfo(StringBuilder info) {
        RegisteredServiceProvider<Economy> rsp2 = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp2 != null) {
            Economy provider = rsp2.getProvider();
            if (provider != null) {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(provider.getName());
                if (plugin != null) {
                    info.append("Economy ").append(plugin.getDescription().getName()).append(" ").append(plugin.getDescription().getVersion()).append("\n");
                }
            }
        }
    }

    private void addChatInfo(StringBuilder info) {
        RegisteredServiceProvider<Chat> rsp1 = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp1 != null) {
            Chat provider = rsp1.getProvider();
            if (provider != null) {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(provider.getName());
                if (plugin != null) {
                    info.append("Chat ").append(plugin.getDescription().getName()).append(" ").append(plugin.getDescription().getVersion()).append("\n");
                }
            }
        }
    }

    private void addPermissionInfo(StringBuilder info) {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            Permission provider = rsp.getProvider();
            if (provider != null) {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(provider.getName());
                if (plugin != null) {
                    info.append("Permissions ").append(plugin.getDescription().getName()).append(" ").append(plugin.getDescription().getVersion()).append("\n");
                }
            }
        }
    }
}
