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
import org.yaml.snakeyaml.nodes.MappingNode;

import java.util.*;

import static codecrafter47.bungeetablistplus.yamlconfig.YamlUtil.remove;

public class MainConfig implements UpdatableConfig {

    @Comment({
            "You can limit the number of characters per slot here",
            "Color codes do not count as a character; -1 means unlimited",
            "This option will be removed soon. Don't use it anymore."
    })
    public int charLimit = -1;

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
            "If this is set to true and the plugin encounters an issue a bug report is sent automatically",
            "Bug reports do not contain any sensitive or identifying information",
            "Bug reports contain the plugin name, plugin version and the error message that also appears in the server log"
    })
    public boolean automaticallySendBugReports = true;

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
            "servers which you wish to show their own tabList (The one provided by bukkit)"
    })
    public List<String> excludeServers = new ArrayList<>();

    @Comment({
            "servers which you wish to hide from the global tabList",
            "Note that this is different from excludeServers above: this hides all players on the hidden servers from appearing",
            "on the tablist, whereas excluded servers' players are still on the BungeeTabListPlus tablist, but they do not see",
            "the global tab list"
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

    @Override
    public void update(MappingNode section) {
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
    }
}
