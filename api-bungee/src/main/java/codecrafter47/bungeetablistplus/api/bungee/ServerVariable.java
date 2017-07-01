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

/**
 * Base class for creating custom Variables bound to a server.
 * <p>
 * To create a custom (per server) Variable you need to create a subclass of this class
 * and register an instance of it with {@link BungeeTabListPlusAPI#registerVariable}
 * <p>
 * After registration the variable can be used in the config file in several ways:
 * Use {@code ${viewer server <name>}} to resolve the variable for the server of the
 * player looking at the tab list.
 * Use {@code ${player server <name>}} to resolve the variable for the server of a
 * player displayed on the tab list, this one can only be used inside the playerComponent.
 * Use {@code ${server <name>}} to resolve the variable for a particular server inside the
 * serverHeader option of the players by server component.
 * Use {@code ${server:<serverName> <name>}} to resolve the variable for a specific server.
 */
public abstract class ServerVariable {
    private final String name;

    /**
     * invoked by the subclass to set the name of the variable
     *
     * @param name name of the variable without { }
     */
    public ServerVariable(String name) {
        this.name = name;
    }

    /**
     * This method is periodically invoked by BungeeTabListPlus to check whether the replacement for the variable changed.
     * <p>
     * The implementation is expected to be thread safe.
     *
     * @param serverName name of the server for which the variable should be replaced
     * @return the replacement for the variable
     */
    public abstract String getReplacement(String serverName);

    /**
     * Getter for the variable name.
     *
     * @return the name of the variable
     */
    public final String getName() {
        return name;
    }
}
