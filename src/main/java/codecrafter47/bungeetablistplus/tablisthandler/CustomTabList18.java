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
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.tab.TabList;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Florian Stober
 */
class CustomTabList18 extends TabList {

    boolean isExcluded = false;

    final Collection<String> usernames = new HashSet<>();
    final Map<UUID, Item> uuids = new HashMap<>();
    private final List<String> bukkitplayers = new ArrayList<>(100);

    CustomTabList18(ProxiedPlayer player) {
        super(player);
    }

    ProxiedPlayer getPlayer() {
        return player;
    }

    @Override
    public void onServerChange() {
        // remove all those names from the clients tab, he's on another server now
        /*
         synchronized (usernames) {
         for (String username : usernames) {
         PlayerListItem pli = new PlayerListItem();
         pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
         Item item = new Item();
         item.setUsername(username);
         item.setProperties(new String[0][0]);
         pli.setItems(new Item[]{item});
         getPlayer().unsafe().sendPacket(pli);
         }
         usernames.clear();
         }*/
        synchronized (bukkitplayers) {
            bukkitplayers.clear();
        }
        synchronized (usernames) {
            PlayerListItem packet = new PlayerListItem();
            packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
            PlayerListItem.Item[] items = new PlayerListItem.Item[uuids.size() + usernames.
                    size()];
            int i = 0;
            for (Entry<UUID, Item> entry : uuids.entrySet()) {
                items[i++] = entry.getValue();
            }
            for (String username : usernames) {
                PlayerListItem.Item item = items[i++] = new PlayerListItem.Item();
                item.setUsername(username);
                item.setDisplayName(username);
            }
            packet.setItems(items);
            if (player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_SNAPSHOT) {
                player.unsafe().sendPacket(packet);
            } else {
                // Split up the packet
                for (PlayerListItem.Item item : packet.getItems()) {
                    PlayerListItem p2 = new PlayerListItem();
                    p2.setAction(packet.getAction());
                    p2.setItems(new PlayerListItem.Item[]{
                            item
                    });
                    player.unsafe().sendPacket(p2);
                }
            }
            uuids.clear();
            usernames.clear();
        }
        isExcluded = false;
    }

    void exclude() {
        isExcluded = true;
        // only 1.7 clients
        if (player.getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_SNAPSHOT) {
            synchronized (bukkitplayers) {
                synchronized (usernames) {
                    for (String s : bukkitplayers) {
                        if (!usernames.contains(s)) {
                            PlayerListItem pli = new PlayerListItem();
                            pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                            Item item = new Item();
                            item.setPing(0);
                            item.setUsername(s);
                            item.setProperties(new String[0][0]);
                            pli.setItems(new Item[]{item});
                            getPlayer().unsafe().sendPacket(pli);
                            usernames.add(s);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate(PlayerListItem pli) {

        // TODO fillbukkitplayers
        for (Item i : pli.getItems()) {
            synchronized (bukkitplayers) {
                String name = i.getUsername();
                if (pli.getAction() == Action.ADD_PLAYER) {
                    if (!bukkitplayers.contains(name)) {
                        bukkitplayers.add(name);
                    }
                } else if (pli.getAction() == Action.REMOVE_PLAYER) {
                    bukkitplayers.remove(name);
                }
            }
        }

        if (BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().excludeServers.
                contains(getPlayer().getServer().getInfo().getName()) || isExcluded || getPlayer().
                getPendingConnection().getVersion() >= 47) {
            // Pass the Packet to the client
            player.unsafe().sendPacket(pli);
            if (((this instanceof TabList18v3) && ((pli.getAction() == Action.ADD_PLAYER) || (pli.
                    getAction() == Action.REMOVE_PLAYER)))) {
                // update list on the client
                BungeeTabListPlus.getInstance().sendImmediate(player);
            }
            // save which packets are send to the client
            synchronized (usernames) {
                for (Item item : pli.getItems()) {
                    if (pli.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                        if (item.getUuid() != null) {
                            uuids.put(item.getUuid(), item);
                            if (this instanceof TabList18v3) ((TabList18v3) this).sendUsernames.remove(item.getUuid());
                        } else {
                            usernames.add(item.getUsername());
                        }
                    } else if (pli.getAction() == Action.REMOVE_PLAYER) {
                        if (item.getUuid() != null) {
                            uuids.remove(item.getUuid());
                            if (this instanceof TabList18v3) ((TabList18v3) this).sendUsernames.remove(item.getUuid());
                        } else {
                            usernames.remove(item.getUsername());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPingChange(int i) {
        // nothing to do
    }

    @Override
    public void onConnect() {
        // nothing to do
    }

    @Override
    public void onDisconnect() {
        // nothing to do
    }

    public int size() {
        return uuids.size();
    }
}
