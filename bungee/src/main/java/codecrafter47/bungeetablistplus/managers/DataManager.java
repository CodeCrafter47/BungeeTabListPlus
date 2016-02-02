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

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.DataCache;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import net.md_5.bungee.api.ProxyServer;

import java.util.concurrent.TimeUnit;

public class DataManager {
    private final BungeeTabListPlus bungeeTabListPlus;

    public DataManager(BungeeTabListPlus bungeeTabListPlus) {
        this.bungeeTabListPlus = bungeeTabListPlus;
        ProxyServer.getInstance().getScheduler().schedule(bungeeTabListPlus.getPlugin(), this::updateData, 2, 2, TimeUnit.SECONDS);
    }

    private void updateData() {
        PermissionManager permissionManager = bungeeTabListPlus.getPermissionManager();
        for (ConnectedPlayer player : bungeeTabListPlus.getConnectedPlayerManager().getPlayers()) {
            DataCache data = player.getData();
            String bungeecord_group = permissionManager.getMainGroupFromBungeeCord(player.getPlayer());
            int bungeecord_rank = permissionManager.getBungeeCordRank(player.getPlayer());

            String bungeeperms_group = permissionManager.getMainGroupFromBungeePerms(player.getPlayer());
            String bungeeperms_prefix = permissionManager.getPrefixFromBungeePerms(player.getPlayer());
            String bungeeperms_displayprefix = permissionManager.getDisplayPrefix(player.getPlayer());
            String bungeeperms_suffix = permissionManager.getSuffixFromBungeePerms(player.getPlayer());
            Integer bungeeperms_rank = permissionManager.getBungeePermsRank(player.getPlayer());

            bungeeTabListPlus.runInMainThread(() -> {
                data.updateValue(DataKeys.BungeeCord_PrimaryGroup, bungeecord_group);
                data.updateValue(DataKeys.BungeeCord_Rank, bungeecord_rank);

                data.updateValue(DataKeys.BungeePerms_PrimaryGroup, bungeeperms_group);
                data.updateValue(DataKeys.BungeePerms_Prefix, bungeeperms_prefix);
                data.updateValue(DataKeys.BungeePerms_DisplayPrefix, bungeeperms_displayprefix);
                data.updateValue(DataKeys.BungeePerms_Suffix, bungeeperms_suffix);
                data.updateValue(DataKeys.BungeePerms_Rank, bungeeperms_rank);
            });
        }
    }
}
