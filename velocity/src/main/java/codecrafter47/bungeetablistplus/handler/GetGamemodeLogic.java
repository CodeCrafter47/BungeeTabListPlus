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
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItem;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfo;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GetGamemodeLogic extends AbstractPacketHandler {

    // Velocity doesn't track game mode in player's connection
    private static final Map<UUID, Integer> gameModes = new HashMap<>();

    private final UUID uuid;

    public GetGamemodeLogic(PacketHandler parent, UUID uuid) {
        super(parent);
        this.uuid = uuid;
    }

    @Override
    public PacketListenerResult onPlayerListPacket(LegacyPlayerListItem packet) {
        if (packet.getAction() == LegacyPlayerListItem.ADD_PLAYER || packet.getAction() == LegacyPlayerListItem.UPDATE_GAMEMODE) {
            for (LegacyPlayerListItem.Item item : packet.getItems()) {
                if (uuid.equals(item.getUuid())) {
                    gameModes.put(uuid, item.getGameMode());
                }
            }
        }
        return super.onPlayerListPacket(packet);
    }

    @Override
    public PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfo packet) {
        if (packet.getActions().contains(UpsertPlayerInfo.Action.UPDATE_GAME_MODE)) {
            for (UpsertPlayerInfo.Entry entry : packet.getEntries()) {
                if (uuid.equals(entry.getProfileId())) {
                    gameModes.put(uuid, entry.getGameMode());
                }
            }
        }
        return super.onPlayerListUpdatePacket(packet);
    }

    @Override
    public PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfo packet) {
        return super.onPlayerListRemovePacket(packet);
    }

    public static int getGameMode(UUID uuid){
        return gameModes.getOrDefault(uuid,0);
    }
}
