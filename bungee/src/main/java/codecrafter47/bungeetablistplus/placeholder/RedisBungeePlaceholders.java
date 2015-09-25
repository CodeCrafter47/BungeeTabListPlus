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

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.api.PlaceholderProvider;
import codecrafter47.bungeetablistplus.api.ServerGroup;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;

import java.util.Optional;

public class RedisBungeePlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bind("server_rplayer_count").to(context -> {
            int sum = 0;
            Optional<ServerGroup> serverGroup = context.getServerGroup();
            if (serverGroup.isPresent()) {
                for (String server : serverGroup.get().getServerNames()) {
                    sum += RedisBungee.getApi().getPlayersOnServer(server).size();
                }
            }
            return Integer.toString(sum);
        });
    }
}
