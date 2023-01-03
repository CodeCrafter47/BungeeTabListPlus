/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import java.lang.reflect.Field;

public class ReflectionUtil {
    public static void setTablistHandler(Player player, TabList tablistHandler) throws NoSuchFieldException, IllegalAccessException {
        setField(ConnectedPlayer.class, player, "tabList", tablistHandler, 5);
    }

    public static TabList getTablistHandler(Player player) throws NoSuchFieldException, IllegalAccessException {
        return getField(ConnectedPlayer.class, player, "tabList", 5);
    }

    public static MinecraftConnection getChannelWrapper(Player player) throws NoSuchFieldException, IllegalAccessException {
        return getField(ConnectedPlayer.class, player, "connection", 50);
    }

    public static void setField(Class<?> clazz, Object instance, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        f.set(instance, value);
    }

    public static void setField(Class<?> clazz, Object instance, String field, Object value, int tries) throws NoSuchFieldException, IllegalAccessException {
        while (--tries > 0) {
            try {
                setField(clazz, instance, field, value);
                return;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        setField(clazz, instance, field, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Class<?> clazz, Object instance, String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return (T) f.get(instance);
    }

    public static <T> T getField(Class<?> clazz, Object instance, String field, int tries) throws NoSuchFieldException, IllegalAccessException {
        while (--tries > 0) {
            try {
                return getField(clazz, instance, field);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return getField(clazz, instance, field);
    }
}
