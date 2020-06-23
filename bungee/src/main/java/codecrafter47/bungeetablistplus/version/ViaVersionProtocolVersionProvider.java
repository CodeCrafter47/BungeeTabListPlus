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

package codecrafter47.bungeetablistplus.version;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

public class ViaVersionProtocolVersionProvider implements ProtocolVersionProvider {

    @Override
    public boolean has18OrLater(ProxiedPlayer player) {
        int version = Via.getAPI().getPlayerVersion(player);
        return ProtocolVersion.getIndex(ProtocolVersion.getProtocol(version)) >= ProtocolVersion.getIndex(ProtocolVersion.v1_8);
    }

    @Override
    public boolean has113OrLater(ProxiedPlayer player) {
        // Note this doesn't care about the client version, but about the server version
        return player.getPendingConnection().getVersion() >= 393;
    }

    @Override
    public boolean is18(ProxiedPlayer player) {
        return Via.getAPI().getPlayerVersion(player) == 47;
    }

    @Override
    public String getVersion(ProxiedPlayer player) {
        return ProtocolVersion.getProtocol(Via.getAPI().getPlayerVersion(player)).getName();
    }

}
