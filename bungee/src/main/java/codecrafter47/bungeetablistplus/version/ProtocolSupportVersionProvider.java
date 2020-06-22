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

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

public class ProtocolSupportVersionProvider implements ProtocolVersionProvider {

    private static final boolean psb12 = ProxyServer.getInstance().getPluginManager().getPlugin("ProtocolSupportBungee").getDescription().getVersion().equals("1.2");

    @Override
    public boolean has18OrLater(ProxiedPlayer player) {
        ProtocolVersion protocolVersion = ProtocolSupportAPI.getProtocolVersion(player);
        if (psb12) {
            switch (protocolVersion) {
                case MINECRAFT_1_8:
                    return true;
                default:
                    return false;
            }
        } else {
            return protocolVersion.isAfterOrEq(ProtocolVersion.MINECRAFT_1_8);
        }
    }

    @Override
    public boolean has113OrLater(ProxiedPlayer player) {
        ProtocolVersion protocolVersion = ProtocolSupportAPI.getProtocolVersion(player);
        if (psb12) {
            return false;
        } else {
            return protocolVersion.getId() >= 393;
        }
    }

    @Override
    public boolean is18(ProxiedPlayer player) {
        ProtocolVersion protocolVersion = ProtocolSupportAPI.getProtocolVersion(player);
        return protocolVersion == ProtocolVersion.MINECRAFT_1_8;
    }

    @Override
    public String getVersion(ProxiedPlayer player) {
        return ProtocolSupportAPI.getProtocolVersion(player).getName();
    }

}
