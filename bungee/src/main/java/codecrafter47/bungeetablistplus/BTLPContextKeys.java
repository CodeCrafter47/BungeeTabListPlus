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

package codecrafter47.bungeetablistplus;

import de.codecrafter47.taboverlay.config.context.ContextKey;
import de.codecrafter47.taboverlay.config.player.PlayerSet;

public class BTLPContextKeys {

    public static final ContextKey<String> SERVER_ID = new ContextKey<>("SERVER_ID");
    public static final ContextKey<PlayerSet> SERVER_PLAYER_SET = new ContextKey<>("SERVER_PLAYER_SET");
}
