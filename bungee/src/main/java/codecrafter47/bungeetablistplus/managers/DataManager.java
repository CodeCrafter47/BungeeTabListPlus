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
import codecrafter47.bungeetablistplus.data.DataKey;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import net.md_5.bungee.api.ProxyServer;

import java.util.Objects;
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
            String bungeecord_group = permissionManager.getMainGroupFromBungeeCord(player.getPlayer());
            int bungeecord_rank = permissionManager.getBungeeCordRank(player.getPlayer());

            String bungeeperms_group = permissionManager.getMainGroupFromBungeePerms(player.getPlayer());
            String bungeeperms_prefix = permissionManager.getPrefixFromBungeePerms(player.getPlayer());
            String bungeeperms_displayprefix = permissionManager.getDisplayPrefix(player.getPlayer());
            String bungeeperms_suffix = permissionManager.getSuffixFromBungeePerms(player.getPlayer());
            Integer bungeeperms_rank = permissionManager.getBungeePermsRank(player.getPlayer());

            updateIfNecessary(player, DataKeys.BungeeCord_PrimaryGroup, bungeecord_group);
            updateIfNecessary(player, DataKeys.BungeeCord_Rank, bungeecord_rank);

            updateIfNecessary(player, DataKeys.BungeePerms_PrimaryGroup, bungeeperms_group);
            updateIfNecessary(player, DataKeys.BungeePerms_Prefix, bungeeperms_prefix);
            updateIfNecessary(player, DataKeys.BungeePerms_DisplayPrefix, bungeeperms_displayprefix);
            updateIfNecessary(player, DataKeys.BungeePerms_Suffix, bungeeperms_suffix);
            updateIfNecessary(player, DataKeys.BungeePerms_Rank, bungeeperms_rank);
        }
    }

    private <T> void updateIfNecessary(ConnectedPlayer player, DataKey<T> key, T value) {
        DataCache data = player.getData();
        if (!Objects.equals(data.getRawValue(key), value)) {
            bungeeTabListPlus.runInMainThread(() -> data.updateValue(key, value));
        }
    }
}
