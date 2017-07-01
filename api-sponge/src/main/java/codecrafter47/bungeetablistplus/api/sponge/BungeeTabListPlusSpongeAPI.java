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

package codecrafter47.bungeetablistplus.api.sponge;

import com.google.common.base.Preconditions;

public abstract class BungeeTabListPlusSpongeAPI {
    private static BungeeTabListPlusSpongeAPI instance;

    /**
     * Registers a custom variable
     * <p>
     * You cannot use this to replace existing variables. If registering a variable which already
     * exists there may be an exception thrown but there is no guarantee that an exception
     * is thrown in that case.
     *
     * @param plugin   your plugin
     * @param variable your variable
     */
    public static void registerVariable(Object plugin, Variable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.registerVariable0(plugin, variable);
    }

    protected abstract void registerVariable0(Object plugin, Variable variable);

    /**
     * Registers a custom per server variable.
     * <p>
     * You cannot use this to replace existing variables. If registering a variable which already
     * exists there may be an exception thrown but there is no guarantee that an exception
     * is thrown in that case.
     *
     * @param plugin   your plugin
     * @param variable your variable
     */
    public static void registerVariable(Object plugin, ServerVariable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.registerVariable0(plugin, variable);
    }

    protected abstract void registerVariable0(Object plugin, ServerVariable variable);

    /**
     * Unregisters a variable
     *
     * @param variable the variable
     */
    public static void unregisterVariable(Variable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.unregisterVariable0(variable);
    }

    protected abstract void unregisterVariable0(Variable variable);

    /**
     * Unregisters a per server variable
     *
     * @param variable the variable
     */
    public static void unregisterVariable(ServerVariable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.unregisterVariable0(variable);
    }

    protected abstract void unregisterVariable0(ServerVariable variable);

    /**
     * Unregisters all variables registered by the give plugin
     *
     * @param plugin the plugin
     */
    public static void unregisterVariables(Object plugin) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.unregisterVariables0(plugin);
    }

    protected abstract void unregisterVariables0(Object plugin);
}
