/*
 *     Copyright (C) 2020 Florian Stober
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

package de.codecrafter47.bungeetablistplus.bungee.compat;

import net.md_5.bungee.protocol.packet.Team;

/**
 * Utility class for accessing methods in old BungeeCord builds, where team color is represented as a byte.
 */
public final class PacketUtil {

    /*
     * Utility class cannot be instantiated.
     */
    private PacketUtil() {
        throw new AssertionError();
    }

    public static void setTeamColorByte(final Team team, final byte c) {
        team.setColor(c);
    }

    public static byte getTeamColorByte(final Team team) {
        return (byte) team.getColor();
    }
}
