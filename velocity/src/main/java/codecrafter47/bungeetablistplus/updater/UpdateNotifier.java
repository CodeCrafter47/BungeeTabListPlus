/*
 *     Copyright (C) 2025 proferabg
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

package codecrafter47.bungeetablistplus.updater;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


/**
 * @author florian
 */
public class UpdateNotifier implements Runnable {

    private final BungeeTabListPlus plugin;

    public UpdateNotifier(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().notifyAdminsIfUpdateAvailable) {
            return;
        }
        if (!plugin.isUpdateAvailable() && !plugin.isNewDevBuildAvailable()) {
            return;
        }
        for (Player player : plugin.getProxy().getAllPlayers()) {
            if (player.hasPermission("bungeetablistplus.admin")) {
                if (plugin.isUpdateAvailable()) {
                    player.sendMessage(getPrefix()
                            .append(Component.text("A new version is available. Download ", NamedTextColor.GOLD))
                            .append(Component.text("here", NamedTextColor.LIGHT_PURPLE)
                                .decorate(TextDecoration.UNDERLINED).clickEvent(
                                ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "http://www.spigotmc.org/resources/bungeetablistplus.313/"))
                            ));
                } else {
                    player.sendMessage(getPrefix()
                            .append(Component.text("A new dev-build is available. Download ", NamedTextColor.GOLD))
                            .append(Component.text("here", NamedTextColor.LIGHT_PURPLE)
                                .decorate(TextDecoration.UNDERLINED).clickEvent(
                                ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL,"http://ci.codecrafter47.dyndns.eu/job/BungeeTabListPlus/"))
                            ));
                }
            }
        }
        if (plugin.isUpdateAvailable()) {
            plugin.getLogger().info("A new version of BungeeTabListPlus is available. Download from http://www.spigotmc.org/resources/bungeetablistplus.313/");
        } else {
            plugin.getLogger().info("A new dev-build is available at http://ci.codecrafter47.dyndns.eu/job/BungeeTabListPlus/");
        }
    }

    private Component getPrefix() {
        return Component.text("[", NamedTextColor.BLUE).append(Component.text("BungeeTabListPlus", NamedTextColor.YELLOW)).clickEvent(
                ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "http://www.spigotmc.org/resources/bungeetablistplus.313/")).
                append(Component.text("] ", NamedTextColor.BLUE));
    }

}
