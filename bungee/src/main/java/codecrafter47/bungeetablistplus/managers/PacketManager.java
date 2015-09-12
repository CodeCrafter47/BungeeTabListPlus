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

import codecrafter47.bungeetablistplus.packets.ITabHeaderPacket;
import codecrafter47.bungeetablistplus.packets.InjectedTabHeaderPacket;
import codecrafter47.bungeetablistplus.packets.TabHeaderPacket18;
import net.md_5.bungee.api.connection.Connection;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketManager {
    private ITabHeaderPacket tabHeaderPacket;

    public PacketManager(Logger logger) {
        if (isClassPresent("net.md_5.bungee.protocol.packet.PlayerListHeaderFooter")) {
            tabHeaderPacket = new TabHeaderPacket18();
        } else {
            try {
                tabHeaderPacket = new InjectedTabHeaderPacket();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to inject TabHeaderPacket", e);
            }
        }
    }

    private boolean isClassPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public boolean isTabHeaderFooterSupported(){
        return tabHeaderPacket != null;
    }

    public void setTabHeaderAndFooter(Connection.Unsafe connection, String header, String footer) {
        tabHeaderPacket.setTabHeaderFooter(connection, header, footer);
    }
}
