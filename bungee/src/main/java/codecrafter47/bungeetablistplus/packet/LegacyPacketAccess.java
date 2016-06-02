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

package codecrafter47.bungeetablistplus.packet;

import net.md_5.bungee.api.connection.Connection;

public interface LegacyPacketAccess {
    void createTeam(Connection.Unsafe connection, String player);

    void updateTeam(Connection.Unsafe connection, String player,
                    String prefix, String displayname, String suffix);

    void removeTeam(Connection.Unsafe connection, String player);

    void createOrUpdatePlayer(Connection.Unsafe connection, String player,
                              int ping);

    void removePlayer(Connection.Unsafe connection, String player);

    boolean isScoreboardSupported();

    boolean isTabModificationSupported();

    interface TeamPacketAccess {
        void createTeam(Connection.Unsafe connection, String player);

        void updateTeam(Connection.Unsafe connection, String player, String prefix,
                        String displayname, String suffix);

        void removeTeam(Connection.Unsafe connection, String player);
    }

    interface PlayerListPacketAccess {
        void createOrUpdatePlayer(Connection.Unsafe connection, String player, int ping);

        void removePlayer(Connection.Unsafe connection, String player);
    }
}
