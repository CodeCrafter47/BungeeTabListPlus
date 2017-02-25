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

package codecrafter47.bungeetablistplus.api.bungee;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;
import java.util.UUID;

/**
 * A player which is shown in the tablist.
 * <p>
 * This player may not be connected to this BungeeCord instance.
 * Examples for that could be if the players is connected through
 * another BungeeCord instance which is linked to the current BungeeCord
 * instance using RedisBungee. Or if the player is a fake player
 * created by BungeeTabListPlus.
 */
public interface IPlayer {
    /**
     * get the username of the player
     *
     * @return the username
     */
    String getName();

    /**
     * get the uuid of the player
     * @return the uuid of the player
     */
    UUID getUniqueID();

    /**
     * get the server the player is connected to
     * @return the server the player is connected to
     */
    Optional<ServerInfo> getServer();

    /**
     * get the ping of the player
     * @return the ping
     */
    int getPing();
}
