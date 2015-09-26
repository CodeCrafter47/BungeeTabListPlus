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

package codecrafter47.bungeetablistplus.data.bukkit;

import codecrafter47.bungeetablistplus.data.AbstractDataAccess;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.data.vault.VaultCurrencyNamePluralProvider;
import codecrafter47.bungeetablistplus.data.vault.VaultCurrencyNameSingularProvider;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class ServerDataAccess extends AbstractDataAccess<Server> {

    private final Plugin plugin;

    public ServerDataAccess(Plugin plugin) {
        super(plugin.getLogger());
        this.plugin = plugin;
        init();
    }

    protected void init() {
        bind(DataKeys.MinecraftVersion, Server::getVersion);
        bind(DataKeys.ServerModName, Server::getName);
        bind(DataKeys.ServerModVersion, Server::getBukkitVersion);
        bind(DataKeys.TPS, ServerTPSProvider.getInstance(plugin));

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            bind(DataKeys.Vault_CurrencyNamePlural, new VaultCurrencyNamePluralProvider());
            bind(DataKeys.Vault_CurrencyNameSingular, new VaultCurrencyNameSingularProvider());
        }
    }

}
