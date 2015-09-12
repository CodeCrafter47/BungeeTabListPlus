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

package codecrafter47.bungeetablistplus.packet.v1_8;

import codecrafter47.bungeetablistplus.packet.PacketAccess;
import codecrafter47.bungeetablistplus.packet.v1_8.inject.TabHeaderPacket;
import com.google.common.base.Preconditions;
import gnu.trove.map.TObjectIntMap;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;

import java.lang.reflect.Field;

public class InjectedTabHeaderPacketAccess implements PacketAccess.TabHeaderPacketAccess {

    public InjectedTabHeaderPacketAccess() throws Exception {
        // register tabheaderpacket
        Class clazz = Protocol.DirectionData.class;
        Field tabListHandler = clazz.getDeclaredField("packetMap");
        tabListHandler.setAccessible(true);
        TObjectIntMap<Class<? extends DefinedPacket>> packetMap;
        packetMap = (TObjectIntMap<Class<? extends DefinedPacket>>) tabListHandler.get(Protocol.GAME.TO_CLIENT);
        packetMap.put(TabHeaderPacket.class, 0x47);
    }

    @Override
    public void setTabHeaderFooter(Connection.Unsafe connection, String header, String footer) {
        Preconditions.checkNotNull(header, "header");
        Preconditions.checkNotNull(footer, "footer");
        connection.sendPacket(new TabHeaderPacket(header, footer));
    }
}
