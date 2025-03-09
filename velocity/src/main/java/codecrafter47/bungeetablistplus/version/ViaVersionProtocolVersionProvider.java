/*
 *     Copyright (C) 2025 proferabg
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

package codecrafter47.bungeetablistplus.version;

import com.velocitypowered.api.proxy.Player;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;


public class ViaVersionProtocolVersionProvider implements ProtocolVersionProvider {

    @Override
    public boolean has18OrLater(Player player) {
        int version = Via.getAPI().getPlayerVersion(player);
        return ProtocolVersion.getIndex(ProtocolVersion.getProtocol(version)) >= ProtocolVersion.getIndex(ProtocolVersion.v1_8);
    }

    @Override
    public boolean has113OrLater(Player player) {
        // Note this doesn't care about the client version, but about the server version
        return player.getProtocolVersion().getProtocol() >= 393;
    }

    @Override
    public boolean has119OrLater(Player player) {
        return Via.getAPI().getPlayerVersion(player) >= 759;
    }

    @Override
    public boolean has1193OrLater(Player player) {
        return Via.getAPI().getPlayerVersion(player) >= 761;
    }

    @Override
    public boolean is18(Player player) {
        return Via.getAPI().getPlayerVersion(player) == 47;
    }

    @Override
    public boolean has1203OrLater(Player player) {
        return Via.getAPI().getPlayerVersion(player) >= 765;
    }

    @Override
    public boolean has1214OrLater(Player player) {
        return Via.getAPI().getPlayerVersion(player) >= 769;
    }

    @Override
    public String getVersion(Player player) {
        return ProtocolVersion.getProtocol(Via.getAPI().getPlayerVersion(player)).getName();
    }

}
