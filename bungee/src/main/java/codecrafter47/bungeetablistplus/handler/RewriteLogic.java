/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.protocol.AbstractPacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.util.Property119Handler;
import com.google.common.base.MoreObjects;
import de.codecrafter47.bungeetablistplus.bungee.compat.PropertyUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewriteLogic extends AbstractPacketHandler {

    private static final boolean USE_PROTOCOL_PROPERTY_TYPE;

    static {
        boolean classPresent = false;
        try {
            Class.forName("net.md_5.bungee.protocol.Property");
            classPresent = true;
        } catch (ClassNotFoundException ignored) {
        }
        USE_PROTOCOL_PROPERTY_TYPE = classPresent;
    }

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
                                if(USE_PROTOCOL_PROPERTY_TYPE) {
                                    String[][] properties = Property119Handler.getProperties(loginResult);
                                    Property119Handler.setProperties(item, properties);
                                } else {
                                    String[][] properties = PropertyUtil.getProperties(loginResult);
                                    PropertyUtil.setProperties(item, properties);
                                }
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
    public PacketListenerResult onPlayerListUpdatePacket(PlayerListItemUpdate packet) {
        if (packet.getActions().contains(PlayerListItemUpdate.Action.ADD_PLAYER)) {
            for (PlayerListItem.Item item : packet.getItems()) {
                UUID uuid = item.getUuid();
                UserConnection player = BungeeCord.getInstance().getPlayerByOfflineUUID(uuid);
                if (player != null) {
                    rewriteMap.put(uuid, player.getUniqueId());
                    LoginResult loginResult = player.getPendingConnection().getLoginProfile();
                    if (loginResult != null) {
                        if(USE_PROTOCOL_PROPERTY_TYPE) {
                            String[][] properties = Property119Handler.getProperties(loginResult);
                            Property119Handler.setProperties(item, properties);
                        } else {
                            String[][] properties = PropertyUtil.getProperties(loginResult);
                            PropertyUtil.setProperties(item, properties);
                        }
                    }
                }
            }
        }
        boolean modified = false;
        for (PlayerListItem.Item item : packet.getItems()) {
            UUID uuid = rewriteMap.get(item.getUuid());
            if (uuid != null) {
                modified = true;
                item.setUuid(uuid);
            }
        }
        PacketListenerResult result = super.onPlayerListUpdatePacket(packet);
        return result == PacketListenerResult.PASS && modified ? PacketListenerResult.MODIFIED : result;
    }

    @Override
    public PacketListenerResult onPlayerListRemovePacket(PlayerListItemRemove packet) {
        boolean modified = false;
        
        UUID[] uuids = packet.getUuids();
        for (int i = 0; i < uuids.length; i++) {
            UUID uuid = rewriteMap.remove(uuids[i]);
            if (uuid != null) {
                modified = true;
                uuids[i] = uuid;
            }
        }

        PacketListenerResult result = super.onPlayerListRemovePacket(packet);
        return result == PacketListenerResult.PASS && modified ? PacketListenerResult.MODIFIED : result;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        rewriteMap.clear();

        super.onServerSwitch(is13OrLater);
    }
}
