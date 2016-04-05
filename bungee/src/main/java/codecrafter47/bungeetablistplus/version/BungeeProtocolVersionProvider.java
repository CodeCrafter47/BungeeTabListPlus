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

public class BungeeProtocolVersionProvider implements ProtocolVersionProvider {

    @Override
    public boolean has18OrLater(ProxiedPlayer player) {
        return player.getPendingConnection().getVersion() >= 47;
    }

    @Override
    public String getVersionString(ProxiedPlayer player) {
        switch (player.getPendingConnection().getVersion()) {
            case 109:
                return "1.9.2";
            case 108:
                return "1.9.1";
            case 107:
                return "1.9";
            case 47:
                return "1.8";
            case 5:
                return "1.7.10";
            case 4:
                return "1.7.5";
            default:
                return null;
        }
    }
}
