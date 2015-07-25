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

package codecrafter47.bungeetablistplus.common;

import codecrafter47.data.Value;
import codecrafter47.data.Values;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PermissionValues extends Values {

    private static final Map<String, Value<Boolean>> permissionValues = new HashMap<>();

    static {
        registerPermission("bungeetablistplus.admin");
        registerPermission("bungeetablistplus.help");
        registerPermission("bungeetablistplus.seevanished");
    }

    private static void registerPermission(String perm) {
        permissionValues.put(perm, registerValue("permission:" + perm));
    }

    public static Value<Boolean> getValueForPermission(String permission) {
        return permissionValues.get(permission);
    }

    public static Set<String> getRegisteredPermissions() {
        return permissionValues.keySet();
    }
}
