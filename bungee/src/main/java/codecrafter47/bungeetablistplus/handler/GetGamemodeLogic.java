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
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class GetGamemodeLogic extends AbstractPacketHandler {

    private final UserConnection userConnection;

    public GetGamemodeLogic(PacketHandler parent, UserConnection userConnection) {
        super(parent);
        this.userConnection = userConnection;
    }

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
        if (packet.getAction() == PlayerListItem.Action.ADD_PLAYER || packet.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE) {
            for (PlayerListItem.Item item : packet.getItems()) {
                if (userConnection.getUniqueId().equals(item.getUuid())) {
                    userConnection.setGamemode(item.getGamemode());
                }
            }
        }
        return super.onPlayerListPacket(packet);
    }
}
