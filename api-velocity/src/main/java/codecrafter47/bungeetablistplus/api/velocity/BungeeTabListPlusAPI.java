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

package codecrafter47.bungeetablistplus.api.velocity;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.Player;
import de.codecrafter47.taboverlay.TabView;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

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
    public static void registerVariable(Object plugin, Variable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.registerVariable0(plugin, variable);
    }

    protected abstract void registerVariable0(Object plugin, Variable variable);

    /**
     * Registers a custom variable bound to a specific server
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
     * Get the face part of the players skin as an icon for use in the tab list.
     *
     * @param player the player
     * @return the icon
     */
    @Nonnull
    public static de.codecrafter47.taboverlay.Icon getPlayerIcon(Player player) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getPlayerIcon0(player);
    }

    @Nonnull
    protected abstract de.codecrafter47.taboverlay.Icon getPlayerIcon0(Player player);


    /**
     * Creates an icon from an 8x8 px image. The creation of the icon can take several
     * minutes. When the icon has been created the callback is invoked.
     *
     * @param image the image
     * @return a completable future providing the icon is ready
     */
    public static CompletableFuture<de.codecrafter47.taboverlay.Icon> getIconFromImage(BufferedImage image) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getIconFromImage0(image);
    }

    protected abstract CompletableFuture<de.codecrafter47.taboverlay.Icon> getIconFromImage0(BufferedImage image);

    /**
     * Get the tab view of a player. The tab view object allows registering and unregistering custom tab overlay
     * handlers.
     *
     * @param player the player
     * @return tab view of that player
     * @throws IllegalStateException is the player is not found
     * @see TabView
     * @see de.codecrafter47.taboverlay.TabOverlayProviderSet
     * @see de.codecrafter47.taboverlay.TabOverlayProvider
     * @see de.codecrafter47.taboverlay.AbstractPlayerTabOverlayProvider
     */
    public static TabView getTabViewForPlayer(Player player) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.getTabViewForPlayer0(player);
    }

    protected abstract TabView getTabViewForPlayer0(Player player);

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
     * Check if a player is hidden from the tab list.
     * <p>
     * A player is regarded as hidden if one of the following conditions is true:
     * - The player is hidden using a vanish plugin(e.g. SuperVanish, Essentials, ...)
     * - The player has been hidden using the /btlp hide command
     * - The player is in the list of hidden players in the configuration
     * - The player is on one of the hidden servers(configuration)
     *
     * @param player the player
     * @return true if hidden, false otherwise
     */
    public static boolean isHidden(Player player) {
        Preconditions.checkState(instance != null, "BungeeTabListPlus not initialized");
        return instance.isHidden0(player);
    }

    protected abstract boolean isHidden0(Player player);
}
