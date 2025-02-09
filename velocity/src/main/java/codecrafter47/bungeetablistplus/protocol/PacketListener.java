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

package codecrafter47.bungeetablistplus.protocol;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooterPacket;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfoPacket;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfoPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

public class PacketListener extends MessageToMessageDecoder<MinecraftPacket> {
    private final VelocityServerConnection connection;
    private final PacketHandler handler;
    private final Player player;

    public PacketListener(VelocityServerConnection connection, PacketHandler handler, Player player) {
        this.connection = connection;
        this.handler = handler;
        this.player = player;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, MinecraftPacket packet, List<Object> out) {
        boolean shouldRelease = true;
        try {
            if (connection.isActive()) {
                if (packet != null) {

                    PacketListenerResult result = PacketListenerResult.PASS;
                    boolean handled = false;

                    if (packet instanceof Team) {
                        result = handler.onTeamPacket((Team) packet);
                        if (result == PacketListenerResult.MODIFIED) {
                            sendPacket(player, packet);
                        }
                    } else if (packet instanceof LegacyPlayerListItemPacket) {
                        result = handler.onPlayerListPacket((LegacyPlayerListItemPacket) packet);
                        handled = true;
                    } else if (packet instanceof HeaderAndFooterPacket) {
                        result = handler.onPlayerListHeaderFooterPacket((HeaderAndFooterPacket) packet);
                        handled = true;
                    } else if (packet instanceof UpsertPlayerInfoPacket) {
                        result = handler.onPlayerListUpdatePacket((UpsertPlayerInfoPacket) packet);
                        handled = true;
                    } else if (packet instanceof RemovePlayerInfoPacket) {
                        result = handler.onPlayerListRemovePacket((RemovePlayerInfoPacket) packet);
                        handled = true;
                    }

                    if (handled && result != PacketListenerResult.CANCEL) {
                        sendPacket(player, packet);
                    }
                }
            }
            out.add(packet);
            shouldRelease = false;
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        } finally {
            if(!shouldRelease){
                ReferenceCountUtil.retain(packet);
            }
        }
    }

    public static void sendPacket(Player player, MinecraftPacket packet) {
        ((ConnectedPlayer) player).getConnection().write(packet);
    }
}
