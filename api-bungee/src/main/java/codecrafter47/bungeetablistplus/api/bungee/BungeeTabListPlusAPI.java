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

import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderManager;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListProvider;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class BungeeTabListPlusAPI {
    private static BungeeTabListPlusAPI instance = null;

    /**
     * Registers a PlaceholderProvider
     * <p>
     * A PlaceholderProvider can add multiple placeholders
     *
     * @param placeholderProvider the PlaceholderProvider
     */
    public static void registerPlaceholderProvider(PlaceholderProvider placeholderProvider) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.registerPlaceholderProvider0(placeholderProvider);
    }

    protected abstract void registerPlaceholderProvider0(PlaceholderProvider placeholderProvider);

    /**
     * Set a custom tab list for a player
     *
     * @param player          the player
     * @param tabListProvider the TabListProvider to use
     */
    public static void setCustomTabList(ProxiedPlayer player, TabListProvider tabListProvider) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.setCustomTabList0(player, tabListProvider);
    }

    protected abstract void setCustomTabList0(ProxiedPlayer player, TabListProvider tabListProvider);

    /**
     * Removes a custom tab list from a player.
     * If the player hasn't got a custom tab list associated with it this will do nothing.
     *
     * @param player the player
     */
    public static void removeCustomTabList(ProxiedPlayer player) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.removeCustomTabList0(player);
    }

    protected abstract void removeCustomTabList0(ProxiedPlayer player);

    /**
     * Get the PlaceholderManager instance
     *
     * @return the PlaceholderManager instance
     */
    public static PlaceholderManager getPlaceholderManager() {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getPlaceholderManager0();
    }

    protected abstract PlaceholderManager getPlaceholderManager0();

    /**
     * This method can be used to obtain a skin object. The method will always return immediately.
     * <p>
     * If the requested skin is in the cache the method will return the requested skin. If not it
     * will return the default skin (random Alex/ Steve skin) and start loading the requested skin in
     * the background so the requested skin will be available next time this method is invoked.
     * For this reason skin objects obtained by this method should not be cached.
     *
     * @param nameOrUUID either a valid player name or uuid or the name of an image file in the plugins/BungeeTabListPlus/heads directory
     * @return the skin associated with the player or the default skin
     * @throws IllegalArgumentException if the name or uuid is invalid
     */
    public static Skin getSkinForPlayer(String nameOrUUID) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getSkinForPlayer0(nameOrUUID);
    }

    protected abstract Skin getSkinForPlayer0(String nameOrUUID);

    /**
     * This method returns an instance of the default skin (random Alex/ Steve skin)
     *
     * @return default skin
     */
    public static Skin getDefaultSkin() {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getDefaultSkin0();
    }

    protected abstract Skin getDefaultSkin0();

    /**
     * Tell BungeeTabListPlus that all tab lists should be refreshed (at least) at the given interval
     *
     * @param interval interval in seconds
     */
    public static void requireTabListUpdateInterval(double interval) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.requireTabListUpdateInterval0(interval);
    }

    protected abstract void requireTabListUpdateInterval0(double interval);
}
