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

package codecrafter47.bungeetablistplus.tablisthandler.logic;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import lombok.AllArgsConstructor;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

@AllArgsConstructor
public class TabListHandler implements PacketHandler {
    private final TabListHandler parent;

    public void onConnected() {
        if (parent != null) {
            parent.onConnected();
        }
    }

    public void onDisconnected() {
        if (parent != null) {
            parent.onDisconnected();
        }
    }

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
        return parent != null ? parent.onPlayerListPacket(packet) : PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        return parent != null ? parent.onTeamPacket(packet) : PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
        return parent != null ? parent.onPlayerListHeaderFooterPacket(packet) : PacketListenerResult.PASS;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        if (parent != null) {
            parent.onServerSwitch(is13OrLater);
        }
    }

    public void setPassThrough(boolean passTrough) {
        if (parent != null) {
            parent.setPassThrough(passTrough);
        }
    }

    public void setSize(int size) {
        if (parent != null) {
            parent.setSize(size);
        }
    }

    public void setSlot(int index, Icon icon, String text, int ping) {
        if (parent != null) {
            parent.setSlot(index, icon, text, ping);
        }
    }

    public void updateText(int index, String text) {
        if (parent != null) {
            parent.updateText(index, text);
        }
    }

    public void updatePing(int index, int ping) {
        if (parent != null) {
            parent.updatePing(index, ping);
        }
    }

    public void setHeaderFooter(String header, String footer) {
        if (parent != null) {
            parent.setHeaderFooter(header, footer);
        }
    }
}
