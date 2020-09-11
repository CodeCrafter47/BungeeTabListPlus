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

package codecrafter47.bungeetablistplus.util;

import de.codecrafter47.taboverlay.config.player.OrderedPlayerSet;
import de.codecrafter47.taboverlay.config.player.Player;

public class EmptyOrderedPlayerSet implements OrderedPlayerSet {

    public static final EmptyOrderedPlayerSet INSTANCE = new EmptyOrderedPlayerSet();

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void addListener(Listener listener) {

    }

    @Override
    public void removeListener(Listener listener) {

    }

    @Override
    public Player getPlayer(int index) {
        throw new IndexOutOfBoundsException();
    }
}
