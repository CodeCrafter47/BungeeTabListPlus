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

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.Skin;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Represents a fake player on the tab list.
 */
public interface FakePlayer extends IPlayer {

    /**
     * Change the server
     *
     * @param newServer new server
     */
    void changeServer(ServerInfo newServer);

    /**
     * Set the skin of the fake player. Affects the head displayed in the tab list.
     *
     * @param skin the skin
     */
    void setSkin(Skin skin);

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
