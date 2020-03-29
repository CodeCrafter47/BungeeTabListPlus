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

package codecrafter47.bungeetablistplus.protocol;

import lombok.NonNull;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

import javax.annotation.Nonnull;

/**
 * Base class for a custom packet handler. Passes all calls to the parent packet handler by default.
 */
public abstract class AbstractPacketHandler implements PacketHandler {

    private final PacketHandler parent;

    public AbstractPacketHandler(@Nonnull @NonNull PacketHandler parent) {
        this.parent = parent;
    }

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
        return parent.onPlayerListPacket(packet);
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        return parent.onTeamPacket(packet);
    }

    @Override
    public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
        return parent.onPlayerListHeaderFooterPacket(packet);
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        parent.onServerSwitch(is13OrLater);
    }
}
