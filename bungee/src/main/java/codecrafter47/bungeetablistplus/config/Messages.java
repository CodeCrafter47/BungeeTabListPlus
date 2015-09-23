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
package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.common.Configuration;

import java.util.Map;

public class Messages extends Configuration {

    public String errorNoPermission = "&cYou don't have permission to do that";
    public String errorNeedsPlayer = "&cThis command must be executed as Player";
    public String errorAlreadyHidden = "&cYou are already hidden";
    public String errorNotHidden = "&cCan't unhide, you're not hidden";
    public String successReloadComplete = "&aReloaded BungeeTabListPlus successfully";
    public String successPlayerHide = "&aYou have been hidden: Your name wont appear on the tablist";
    public String successPlayerUnhide = "&aYou're not hidden any longer";

    public Messages() {
        setHeader("You can change messages here");
    }

    @Override
    protected void read(Map<Object, Object> map) {
        if (map.containsKey("errorNoPermission")) {
            errorNoPermission = map.get("errorNoPermission").toString();
        }

        if (map.containsKey("errorNeedsPlayer")) {
            errorNeedsPlayer = map.get("errorNeedsPlayer").toString();
        }

        if (map.containsKey("errorAlreadyHidden")) {
            errorAlreadyHidden = map.get("errorAlreadyHidden").toString();
        }

        if (map.containsKey("errorNotHidden")) {
            errorNotHidden = map.get("errorNotHidden").toString();
        }

        if (map.containsKey("successReloadComplete")) {
            successReloadComplete = map.get("successReloadComplete").toString();
        }

        if (map.containsKey("successPlayerHide")) {
            successPlayerHide = map.get("successPlayerHide").toString();
        }

        if (map.containsKey("successPlayerUnhide")) {
            successPlayerUnhide = map.get("successPlayerUnhide").toString();
        }
    }

    @Override
    protected void write() {
        write("errorNoPermission", errorNoPermission);
        write("errorNeedsPlayer", errorNeedsPlayer);
        write("errorAlreadyHidden", errorAlreadyHidden);
        write("errorNotHidden", errorNotHidden);
        write("successReloadComplete", successReloadComplete);
        write("successPlayerHide", successPlayerHide);
        write("successPlayerUnhide", successPlayerUnhide);
    }
}
