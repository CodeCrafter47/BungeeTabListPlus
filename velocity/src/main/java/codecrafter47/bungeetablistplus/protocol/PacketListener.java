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
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItem;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfo;
import com.velocitypowered.proxy.protocol.packet.Team;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

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
        try {
            if (connection.isActive()) {
                if (packet != null) {

                    PacketListenerResult result = PacketListenerResult.PASS;
                    boolean handled = false;

                    if (packet instanceof Team) {
                        result = handler.onTeamPacket((Team) packet);
                        if (result == PacketListenerResult.MODIFIED) {
                            ReflectionUtil.getChannelWrapper(player).getChannel().write(packet);
                        }
                        if (result != PacketListenerResult.PASS) {
                            return;
                        }
                    } else if (packet instanceof LegacyPlayerListItem) {
                        result = handler.onPlayerListPacket((LegacyPlayerListItem) packet);
                        handled = true;
                    } else if (packet instanceof HeaderAndFooter) {
                        result = handler.onPlayerListHeaderFooterPacket((HeaderAndFooter) packet);
                        handled = true;
                    } else if (packet instanceof UpsertPlayerInfo) {
                        result = handler.onPlayerListUpdatePacket((UpsertPlayerInfo) packet);
                        handled = true;
                    } else if (packet instanceof RemovePlayerInfo) {
                        result = handler.onPlayerListRemovePacket((RemovePlayerInfo) packet);
                        handled = true;
                    }

                    if (handled) {
                        if (result != PacketListenerResult.CANCEL) {
                            ReflectionUtil.getChannelWrapper(player).getChannel().write(packet);
                        }
                        return;
                    }
                }
            }
            out.add(packet);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }
}
