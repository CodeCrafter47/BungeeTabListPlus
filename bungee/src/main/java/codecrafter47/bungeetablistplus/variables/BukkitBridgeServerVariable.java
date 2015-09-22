/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ServerVariable;
import codecrafter47.bungeetablistplus.layout.TabListContext;
import codecrafter47.data.Value;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Florian Stober
 */
public class BukkitBridgeServerVariable<T> implements ServerVariable {
    private final Value<T> value;
    private final Function<Optional<T>, String> toString;

    public BukkitBridgeServerVariable(Value<T> value) {
        this(value, t -> t.map(Object::toString).orElse(""));
    }

    public BukkitBridgeServerVariable(Value<T> value, Function<Optional<T>, String> toString) {
        this.value = value;
        this.toString = toString;
    }

    @Override
    public String getReplacement(ProxiedPlayer viewer, List<ServerInfo> servers, String args, TabListContext context) {
        return toString.apply(BungeeTabListPlus.getInstance().getBridge().getServerInformation(servers.get(0), value));
    }
}
