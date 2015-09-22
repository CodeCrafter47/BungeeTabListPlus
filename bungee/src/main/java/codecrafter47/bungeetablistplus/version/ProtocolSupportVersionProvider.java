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
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

public class ProtocolSupportVersionProvider implements ProtocolVersionProvider {
    @Override
    public int getProtocolVersion(ProxiedPlayer player) {
        ProtocolVersion protocolVersion = ProtocolSupportAPI.getProtocolVersion(player);
        switch (protocolVersion) {
            case MINECRAFT_1_8:
                return 47;
            case MINECRAFT_1_7_10:
                return 5;
            case MINECRAFT_1_7_5:
                return 4;
            case MINECRAFT_1_6_4:
            case MINECRAFT_1_6_2:
            case MINECRAFT_1_5_2:
                return -1;
            case UNKNOWN:
            default:
                throw new IllegalStateException("Protocol version of player " + player.getName() + " is " + protocolVersion.name() + ". Don't know what to do.");
        }
    }
}
