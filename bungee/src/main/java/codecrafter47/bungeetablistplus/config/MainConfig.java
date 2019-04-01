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

import codecrafter47.bungeetablistplus.yamlconfig.Comment;
import codecrafter47.bungeetablistplus.yamlconfig.Path;
import codecrafter47.bungeetablistplus.yamlconfig.UpdatableConfig;
import codecrafter47.bungeetablistplus.yamlconfig.YamlNode;

import java.util.*;

public class MainConfig implements UpdatableConfig {

    @Comment({
            "if enabled the plugin checks for new versions automatically.",
            "Use /BTLP to see whether a new version is available",
            "this does NOT automatically install an update"
    })
    public boolean checkForUpdates = true;

    @Comment({
            "this notifies admins (everyone with the permission `bungeetablistplus.admin`) if an update is available"
    })

    public boolean notifyAdminsIfUpdateAvailable = true;

    @Comment({
            "Interval (in seconds) at which all servers of your network get pinged to check whether they are online",
            "If you intend to use the {onlineState:SERVER} variable set this to 2 or any value you like",
            "setting this to -1 disables this feature"
    })
    public int pingDelay = -1;

    @Comment({
            "those fakeplayers will randomly appear on the tablist. If you don't put any names there then no fakeplayers will appear"
    })
    public List<String> fakePlayers = new ArrayList<>();

    @Comment({
            "Servers which you wish to show their own tabList (The one provided by bukkit)",
            "Players on these servers don't see the custom tab list provided by BungeeTabListPlus"
    })
    public List<String> excludeServers = new ArrayList<>();

    @Comment({
            "Players on these servers are hidden from the tab list.",
            "Doesn't necessarily hide the server from the tab list."
    })
    public List<String> hiddenServers = new ArrayList<>();

    @Comment({
            "players which are permanently hidden from the tab list",
            "you can either put your username or your uuid (with dashes) here",
            "don't use this. you have absolutely no reason to hide from anyone. on your own server."
    })
    public List<String> hiddenPlayers = new ArrayList<>();

    @Comment({
            "Time zone to use for the {time} variable",
            "Can be full name like \"America/Los_Angeles\"",
            "or custom id like \"GMT+8\""
    })
    @Path("time-zone")
    public String timezone = TimeZone.getDefault().getID();

    @Comment("Custom placeholders")
    public Map<String, CustomPlaceholder> customPlaceholders = new HashMap<>();

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(timezone);
    }

    public transient boolean needWrite = false;

    @Override
    public void update(YamlNode section) {
        remove(section, "tablistUpdateIntervall");
        remove(section, "tablistUpdateInterval");

        remove(section, "updateOnPlayerJoinLeave");
        remove(section, "updateOnServerChange");

        remove(section, "offline");
        remove(section, "offline-text");

        remove(section, "online");
        remove(section, "online-text");

        remove(section, "permissionSource");

        remove(section, "useScoreboardToBypass16CharLimit");

        remove(section, "autoExcludeServers");

        remove(section, "showPlayersInGamemode3");

        remove(section, "serverAlias");
        remove(section, "worldAlias");
        remove(section, "serverPrefixes");
        remove(section, "prefixes");

        remove(section, "charLimit");

        remove(section, "automaticallySendBugReports");
    }

    private void remove(YamlNode section, String id) {
        if (section.contains(id)) {
            section.remove(id);
            needWrite = true;
        }
    }
}
