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
import net.md_5.bungee.api.plugin.Plugin;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public abstract class BungeeTabListPlusAPI {
    private static BungeeTabListPlusAPI instance = null;

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
    public static void registerVariable(Plugin plugin, Variable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.registerVariable0(plugin, variable);
    }

    protected abstract void registerVariable0(Plugin plugin, Variable variable);

    /**
     * Registers a PlaceholderProvider
     * <p>
     * A PlaceholderProvider can add multiple placeholders
     *
     * @param placeholderProvider the PlaceholderProvider
     * @deprecated Use {@link #registerVariable(Plugin, Variable)} instead.
     */
    @Deprecated
    public static void registerPlaceholderProvider(PlaceholderProvider placeholderProvider) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.registerPlaceholderProvider0(placeholderProvider);
    }

    protected abstract void registerPlaceholderProvider0(PlaceholderProvider placeholderProvider);

    /**
     * Create a new {@link CustomTablist}
     *
     * @return
     */
    public static CustomTablist createCustomTablist() {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.createCustomTablist0();
    }

    protected abstract CustomTablist createCustomTablist0();

    /**
     * Set a custom tab list for a player
     *
     * @param player          the player
     * @param tabListProvider the TabListProvider to use
     * @deprecated Use {@link #setCustomTabList(ProxiedPlayer, CustomTablist)} instead
     */
    @Deprecated
    public static void setCustomTabList(ProxiedPlayer player, TabListProvider tabListProvider) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.setCustomTabList0(player, tabListProvider);
    }

    protected abstract void setCustomTabList0(ProxiedPlayer player, TabListProvider tabListProvider);

    /**
     * Set a custom tab list for a player
     *
     * @param player        the player
     * @param customTablist the CustomTablist to use
     */
    public static void setCustomTabList(ProxiedPlayer player, CustomTablist customTablist) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.setCustomTabList0(player, customTablist);
    }

    protected abstract void setCustomTabList0(ProxiedPlayer player, CustomTablist customTablist);

    /**
     * Get the face part of the players skin as an icon for use in the tab list.
     *
     * @param player the player
     * @return the icon
     */
    @Nonnull
    public static Icon getIconFromPlayer(ProxiedPlayer player) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getIconFromPlayer0(player);
    }

    @Nonnull
    protected abstract Icon getIconFromPlayer0(ProxiedPlayer player);

    /**
     * Creates an icon from an 8x8 px image. The creation of the icon can take several
     * minutes. When the icon has been created the callback is invoked.
     *
     * @param image the image
     */
    public static void createIcon(BufferedImage image, Consumer<Icon> callback) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.createIcon0(image, callback);
    }

    protected abstract void createIcon0(BufferedImage image, Consumer<Icon> callback);

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
     * Get the FakePlayerManager instance
     *
     * @return the FakePlayerManager instance
     */
    public static FakePlayerManager getFakePlayerManager() {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getFakePlayerManager0();
    }

    protected abstract FakePlayerManager getFakePlayerManager0();

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
     * @deprecated Use {@link #getIconFromPlayer(ProxiedPlayer)} instead.
     */
    @Deprecated
    public static Skin getSkinForPlayer(String nameOrUUID) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getSkinForPlayer0(nameOrUUID);
    }

    protected abstract Skin getSkinForPlayer0(String nameOrUUID);

    /**
     * This method returns an instance of the default skin (random Alex/ Steve skin)
     *
     * @return default skin
     * @deprecated Use {@link Icon#DEFAULT} instead.
     */
    @Deprecated
    public static Skin getDefaultSkin() {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getDefaultSkin0();
    }

    protected abstract Skin getDefaultSkin0();

    /**
     * Tell BungeeTabListPlus that all tab lists should be refreshed (at least) at the given interval
     *
     * @param interval interval in seconds
     * @deprecated This is no longer required.
     */
    @Deprecated
    public static void requireTabListUpdateInterval(double interval) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        instance.requireTabListUpdateInterval0(interval);
    }

    protected abstract void requireTabListUpdateInterval0(double interval);
}
