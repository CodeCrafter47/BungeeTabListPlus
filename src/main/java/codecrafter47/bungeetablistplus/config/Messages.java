/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
package codecrafter47.bungeetablistplus.config;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public class Messages extends Config {

    public final String errorNoPermission = "&cYou don't have permission to do that";
    public final String errorNeedsPlayer = "&cThis command must be executed as Player";
    public final String errorAlreadyHidden = "&cYou are already hidden";
    public final String errorNotHidden = "&cCan't unhide, you're not hidden";
    public final String successReloadComplete = "&aReloaded BungeeTabListPlus successfully";
    public final String successPlayerHide = "&aYou have been hidden: Your name wont appear on the tablist";
    public final String successPlayerUnhide = "&aYou're not hidden any longer";

    public Messages(Plugin plugin) throws InvalidConfigurationException {
        CONFIG_FILE = new File("plugins" + File.separator + plugin.
                getDescription().getName(), "messages.yml");
        CONFIG_HEADER = new String[]{"You can change messages here"};

        this.init();
    }
}
