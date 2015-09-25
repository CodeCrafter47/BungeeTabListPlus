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

package codecrafter47.bungeetablistplus.api;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;

public class ServerGroup {
    private final ImmutableList<String> serverNames;
    private final String name;

    public ServerGroup(Collection<String> servers, String name) {
        this.serverNames = ImmutableList.copyOf(servers);
        this.name = name;
    }

    public static ServerGroup of(String serverName) {
        return new ServerGroup(Collections.singleton(serverName), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().getServerAlias(serverName));
    }


    public static ServerGroup of(Collection<String> server, String name) {
        return new ServerGroup(server, name);
    }

    public Collection<String> getServerNames() {
        return serverNames;
    }

    public String getName() {
        return name;
    }
}
