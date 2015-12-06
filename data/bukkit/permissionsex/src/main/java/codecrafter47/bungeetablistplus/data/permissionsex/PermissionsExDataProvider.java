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

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.function.Function;

public abstract class PermissionsExDataProvider<B, R> implements Function<B, R> {
    @Override
    public final R apply(B b) {
        try {
            return apply0(b);
        } catch (Throwable th) {
            throw new RuntimeException("Exception while querying data from PermissionsEx\n" + getPermissionsExInfo(), th);
        }
    }

    protected abstract R apply0(B b);

    private String getPermissionsExInfo() {
        StringBuilder info = new StringBuilder();
        Plugin permissionsex = Bukkit.getPluginManager().getPlugin("PermissionsEx");
        if (permissionsex != null) {
            info.append("PermissionsEx ").append(permissionsex.getDescription().getVersion()).append("\n");
            try {
                addPermissionInfo(info);
            } catch (Throwable th) {
                info.append(th.toString());
            }
        }
        return info.toString();
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
