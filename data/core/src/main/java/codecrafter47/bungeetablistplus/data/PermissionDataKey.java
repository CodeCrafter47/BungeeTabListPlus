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

package codecrafter47.bungeetablistplus.data;

public class PermissionDataKey extends DataKey<Boolean> {
    private final String permission;

    private static final long serialVersionUID = 1L;

    PermissionDataKey(String permission) {
        super("minecraft:permission", Scope.PLAYER);
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + permission.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof PermissionDataKey && getPermission().equals(((PermissionDataKey) obj).getPermission());
    }

    @Override
    public String toString() {
        return String.format("permission[%s]", permission);
    }
}
