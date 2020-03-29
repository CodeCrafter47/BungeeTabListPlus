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

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.protocol.AbstractPacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import com.google.common.base.MoreObjects;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewriteLogic extends AbstractPacketHandler {

    private final Map<UUID, UUID> rewriteMap = new HashMap<>();

    public RewriteLogic(PacketHandler parent) {
        super(parent);
    }

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {

        if (packet.getAction() == PlayerListItem.Action.ADD_PLAYER) {
            for (PlayerListItem.Item item : packet.getItems()) {
                UUID uuid = item.getUuid();
                UserConnection player = BungeeCord.getInstance().getPlayerByOfflineUUID(uuid);
                if (player != null) {
                    rewriteMap.put(uuid, player.getUniqueId());
                }
            }
        }

        boolean modified = false;

        if (packet.getAction() == PlayerListItem.Action.REMOVE_PLAYER) {
            for (PlayerListItem.Item item : packet.getItems()) {
                UUID uuid = rewriteMap.remove(item.getUuid());
                modified |= uuid != null;
                item.setUuid(MoreObjects.firstNonNull(uuid, item.getUuid()));
            }
        } else {
            for (PlayerListItem.Item item : packet.getItems()) {
                UUID uuid = rewriteMap.get(item.getUuid());
                if (uuid != null) {
                    modified = true;
                    if (packet.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                        UserConnection player = BungeeCord.getInstance().getPlayerByOfflineUUID(item.getUuid());
                        if (player != null) {
                            LoginResult loginResult = player.getPendingConnection().getLoginProfile();
                            if (loginResult != null) {
                                String[][] props = new String[loginResult.getProperties().length][];
                                for (int i = 0; i < props.length; i++) {
                                    props[i] = new String[]
                                            {
                                                    loginResult.getProperties()[i].getName(),
                                                    loginResult.getProperties()[i].getValue(),
                                                    loginResult.getProperties()[i].getSignature()
                                            };
                                }
                                item.setProperties(props);
                            } else {
                                item.setProperties(new String[0][0]);
                            }
                        }
                    }
                    item.setUuid(uuid);
                }
            }
        }

        PacketListenerResult result = super.onPlayerListPacket(packet);
        return result == PacketListenerResult.PASS && modified ? PacketListenerResult.MODIFIED : result;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        rewriteMap.clear();

        super.onServerSwitch(is13OrLater);
    }
}
