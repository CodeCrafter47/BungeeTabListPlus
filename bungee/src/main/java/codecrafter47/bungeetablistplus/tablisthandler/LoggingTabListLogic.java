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

package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.eventlog.EventLogger;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.tablisthandler.logic.LowMemoryTabListLogic;
import codecrafter47.bungeetablistplus.tablisthandler.logic.TabListHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

public class LoggingTabListLogic extends LowMemoryTabListLogic {

    private final EventLogger log = new EventLogger();

    public LoggingTabListLogic(TabListHandler parent, ProxiedPlayer player) {
        super(parent, player);
    }

    @Override
    public void onConnected() {
        log.connect(getUniqueId());
        super.onConnected();
    }

    @Override
    public void onDisconnected() {
        log.disconnect();
        super.onDisconnected();
    }

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
        log.packet(packet);
        return super.onPlayerListPacket(packet);
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        log.packet(packet);
        return super.onTeamPacket(packet);
    }

    @Override
    public void onServerSwitch() {
        log.serverSwitch();
        super.onServerSwitch();
    }

    @Override
    public void setPassThrough(boolean passThrough) {
        if (passThrough != passtrough) {
            log.passThrough(passThrough);
            super.setPassThrough(passThrough);
        }
    }

    @Override
    public void setSize(int size) {
        if (size != this.size) {
            log.size(size);
            super.setSize(size);
        }
    }

    @Override
    public void setSlot(int index, Icon icon, String text, int ping) {
        if (!clientSkin[index].equals(icon) || !clientText[index].equals(text) || clientPing[index] != ping) {
            log.set(index, icon, text, ping);
            super.setSlot(index, icon, text, ping);
        }
    }
}
