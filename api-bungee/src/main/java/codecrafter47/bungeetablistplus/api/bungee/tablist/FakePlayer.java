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

package codecrafter47.bungeetablistplus.api.bungee.tablist;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a fake player on the tab list.
 */
public interface FakePlayer {
    /**
     * get the username of the player
     *
     * @return the username
     */
    String getName();

    /**
     * get the uuid of the player
     *
     * @return the uuid of the player
     */
    UUID getUniqueID();

    /**
     * get the server the player is connected to
     *
     * @return the server the player is connected to
     */
    Optional<ServerInfo> getServer();

    /**
     * get the ping of the player
     *
     * @return the ping
     */
    int getPing();

    /**
     * get the icon displayed on the tab list
     *
     * @return the icon
     */
    Icon getIcon();

    /**
     * Change the server
     *
     * @param newServer new server
     */
    void changeServer(ServerInfo newServer);

    /**
     * Set the icon displayed on the tab list.
     *
     * @param icon the icon
     */
    void setIcon(Icon icon);

    /**
     * Set the ping of the fake player.
     *
     * @param ping the ping
     */
    void setPing(int ping);

    /**
     * @return whether the fake player will randomly switch servers
     */
    boolean isRandomServerSwitchEnabled();

    /**
     * @param value whether the fake player will randomly switch servers
     */
    void setRandomServerSwitchEnabled(boolean value);
}
