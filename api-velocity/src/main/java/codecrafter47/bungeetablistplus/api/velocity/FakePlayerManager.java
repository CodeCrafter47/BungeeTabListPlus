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

package codecrafter47.bungeetablistplus.api.velocity;

import codecrafter47.bungeetablistplus.api.velocity.tablist.FakePlayer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.Collection;

/**
 * Controls fake players
 */
public interface FakePlayerManager {

    /**
     * Get all fake players which are currently displayed on the tab list
     *
     * @return collection of all fake players
     */
    Collection<FakePlayer> getOnlineFakePlayers();

    /**
     * @return whether the plugin will randomly add fake players it finds in the config, and randomly removes fake players
     */
    boolean isRandomJoinLeaveEnabled();

    /**
     * set whether the plugin should randomly add fake players it finds in the config, and randomly removes fake players
     *
     * @param value whether random join/leave events for fake players should be enabled
     */
    void setRandomJoinLeaveEnabled(boolean value);

    /**
     * Creates a fake player which is immediately visible on the tab list
     *
     * @param name   name of the fake player
     * @param server server of the fake player
     * @return the fake player
     */
    FakePlayer createFakePlayer(String name, ServerInfo server);

    /**
     * remove a fake player
     *
     * @param fakePlayer the fake player to be removed
     */
    void removeFakePlayer(FakePlayer fakePlayer);
}
