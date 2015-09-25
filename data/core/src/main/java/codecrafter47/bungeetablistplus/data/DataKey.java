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

import com.google.common.base.Preconditions;

import java.io.Serializable;

public class DataKey<T> implements Serializable {
    private final String id;
    private final Scope scope;

    private static final long serialVersionUID = 1L;

    public static DataKeyBuilder builder() {
        return new DataKeyBuilder();
    }

    protected DataKey(String id, Scope scope) {
        this.id = id;
        this.scope = scope;
    }

    public String getId() {
        return id;
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        return id.hashCode() + scope.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataKey && getId().equals(((DataKey<?>) obj).getId()) && getScope().equals(((DataKey<?>) obj).getScope());
    }

    @Override
    public String toString() {
        return getId();
    }

    public enum Scope {
        PLAYER, SERVER
    }

    public static class DataKeyBuilder {
        private String id = null;
        private Scope scope = null;

        private DataKeyBuilder() {
        }

        public DataKeyBuilder id(String id) {
            this.id = id;
            return this;
        }

        public DataKeyBuilder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public DataKeyBuilder server() {
            return scope(Scope.SERVER);
        }

        public DataKeyBuilder player() {
            return scope(Scope.PLAYER);
        }

        public <T> DataKey<T> build() {
            Preconditions.checkNotNull(id, "id");
            Preconditions.checkNotNull(scope, "scope");
            return new DataKey<>(id, scope);
        }
    }
}
