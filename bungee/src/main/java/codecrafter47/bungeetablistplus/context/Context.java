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

package codecrafter47.bungeetablistplus.context;

import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import codecrafter47.bungeetablistplus.config.CustomPlaceholder;
import codecrafter47.bungeetablistplus.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public class Context {

    private static int nextId = 0;

    public static final ImmutableContextKey<CustomTablist> KEY_TAB_LIST = new ImmutableContextKey<>();
    public static final ImmutableContextKey<Player> KEY_VIEWER = new ImmutableContextKey<>();
    public static final ImmutableContextKey<Player> KEY_PLAYER = new ImmutableContextKey<>();
    public static final ImmutableContextKey<String> KEY_SERVER = new ImmutableContextKey<>();
    public static final ImmutableContextKey<Integer> KEY_OTHER_PLAYERS_COUNT = new ImmutableContextKey<>();
    public static final ImmutableContextKey<Integer> KEY_SERVER_PLAYER_COUNT = new ImmutableContextKey<>();
    public static final ImmutableContextKey<Integer> KEY_COLUMNS = new ImmutableContextKey<>();
    public static final ImmutableContextKey<PlayerSets> KEY_PLAYER_SETS = new ImmutableContextKey<>();
    public static final ImmutableContextKey<Map<String, CustomPlaceholder>> KEY_CUSTOM_PLACEHOLDERS = new ImmutableContextKey<>();

    // Parent
    private final Object[] elements;

    // Constructor
    public Context() {
        elements = new Object[nextId];
    }

    private Context(@Nonnull Context parent) {
        elements = Arrays.copyOf(parent.elements, nextId);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(@Nonnull ImmutableContextKey<T> key) {
        return (T) elements[key.index];
    }

    public <T> Context put(@Nonnull ImmutableContextKey<T> key, @Nonnull T element) {
        if (elements[key.index] != null) {
            throw new IllegalStateException("Cannot change immutable value.");
        }
        elements[key.index] = element;
        return this;
    }

    public Context derived() {
        return new Context(this);
    }

    public static class ImmutableContextKey<T> {
        private final int index;

        private ImmutableContextKey() {
            this.index = nextId++;
        }
    }
}
