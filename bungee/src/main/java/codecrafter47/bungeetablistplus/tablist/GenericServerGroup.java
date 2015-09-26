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

package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;

public class GenericServerGroup implements ServerGroup {
    private final ImmutableList<String> serverNames;
    private final String name;

    public GenericServerGroup(Collection<String> servers, String name) {
        this.serverNames = ImmutableList.copyOf(servers);
        this.name = name;
    }

    public static ServerGroup of(String serverName) {
        return new GenericServerGroup(Collections.singleton(serverName), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().getServerAlias(serverName));
    }


    public static ServerGroup of(Collection<String> server, String name) {
        return new GenericServerGroup(server, name);
    }

    @Override
    public Collection<String> getServerNames() {
        return serverNames;
    }

    @Override
    public String getName() {
        return name;
    }
}
