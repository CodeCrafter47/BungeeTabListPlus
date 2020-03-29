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

import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import de.codecrafter47.bungeetablistplus.bungee.compat.WaterfallCompat;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Team;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Dirty hack to reduce memory usage of the plugin. Should be removed as soon as
 * the underlying problem is fixed in BungeeCord.
 */
public class LowMemoryTabOverlayHandlerImpl extends TabOverlayHandlerImpl {

    public LowMemoryTabOverlayHandlerImpl(Logger logger, Executor eventLoopExecutor, UUID viewerUuid, ProxiedPlayer player, boolean is18, boolean has113OrLater) {
        super(logger, eventLoopExecutor, viewerUuid, player, is18, has113OrLater);
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        if (super.onTeamPacket(packet) != PacketListenerResult.CANCEL) {
            sendPacket(packet);
        }
        return PacketListenerResult.CANCEL;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        if (!WaterfallCompat.isDisableEntityMetadataRewrite()) {
            for (String team : serverTeams.keySet()) {
                sendPacket(new Team(team));
            }
        }
        super.onServerSwitch(is13OrLater);
    }
}
