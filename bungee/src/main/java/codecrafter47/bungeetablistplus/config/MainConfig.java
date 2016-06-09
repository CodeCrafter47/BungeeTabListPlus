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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.ConfigSection;
import net.cubespace.Yamler.Config.Path;
import net.cubespace.Yamler.Config.YamlConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class MainConfig extends YamlConfig {

    public MainConfig() {
        CONFIG_HEADER = new String[]{
                "This is the Config File of BungeeTabListPlus",
                "You can find more detailed information on the wiki: https://github.com/CodeCrafter47/BungeeTabListPlus/wiki"
        };
    }

    @Comments({
            "time in seconds after which the tabList will be resend to all players",
            "set this to -1 to disable scheduled update of the tabList"
    })
    public double tablistUpdateInterval = 1;

    @Comments({
            "whether tabList should be resend if a player joins or leaves the server"
    })
    public boolean updateOnPlayerJoinLeave = true;

    @Comments({
            "whether tablist should be resend if a player switches the server"
    })
    public boolean updateOnServerChange = true;

    @Comments({
            "You can limit the number of characters per slot here",
            "Color codes do not count as a character; -1 means unlimited"
    })
    public int charLimit = -1;

    @Comments({
            "Decide from where BungeeTabListPlus takes information like permissions,",
            "prefix, suffix and group.",
            "Possible values:",
            "AUTO        - take best source",
            "BUKKIT      - take information from Bukkit/Vault",
            "BUNGEEPERMS - take information from BungeePerms",
            "BUNGEE      - take group from bungee, prefix from config.yml, permissions from bungee"
    })
    public String permissionSource = "AUTO";

    @Comments({
            "whether to show players in spectator mode"
    })
    public boolean showPlayersInGamemode3 = true;

    @Comments({
            "if enabled the plugin checks for new versions automatically.",
            "Use /BTLP to see whether a new version is available",
            "this does NOT automatically install an update"
    })
    public boolean checkForUpdates = true;

    @Comments({
            "this notifies admins (everyone with the permission `bungeetablistplus.admin`) if an update is available"
    })

    public boolean notifyAdminsIfUpdateAvailable = true;

    @Comments({
            "If this is set to true and the plugin encounters an issue a bug report is sent automatically",
            "Bug reports do not contain any sensitive or identifying information",
            "Bug reports contain the plugin name, plugin version and the error message that also appears in the server log"
    })
    public boolean automaticallySendBugReports = true;

    @Comments({
            "server Alias fo the {server} Variable"
    })
    public HashMap<String, String> serverAlias = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("factions", "Factions")
            .put("lobby0", "Lobby")
            .put("lobby1", "Lobby")
            .put("sg", "Survival Games")
            .build());

    @Comments({
            "Alias fo the {world} Variable. Match 'server:world' to an alias"
    })
    public HashMap<String, String> worldAlias = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("factions:world", "Overworld")
            .put("factions:world_nether", "Nether")
            .put("factions:world_end", "The End")
            .build());

    @Comments({
            "list servers you wish to create custom prefixes for.",
            "to use the custom prefixes use the {serverPrefix} variable"
    })
    public HashMap<String, String> serverPrefixes = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("Minigames", "&8(&bM&8)")
            .put("SkyBlock", "&8(&dS&8) ")
            .build());

    @Comments({
            "the prefixes used for the {prefix} variable, based upon permission groups",
            "IMPORTANT: these prefixes won't be used by default. see the wiki for details"
    })
    public HashMap<String, String> prefixes = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("default", "")
            .put("admin", "&c[A] ")
            .build());

    @Comments({
            "Interval (in seconds) at which all servers of your network get pinged to check whether they are online",
            "If you intend to use the {onlineState:SERVER} variable set this to 2 or any value you like",
            "setting this to -1 disables this feature"
    })
    public int pingDelay = -1;

    @Comments({
            "replacement for the {onlineState} variable if the server is online"
    })
    @Path("online-text")
    public String online_text = "&2 ON";

    @Comments({
            "replacement for the {onlineState} variable if the server is offline"
    })
    @Path("offline-text")
    public String offline_text = "&c OFF";

    @Comments({
            "those fakeplayers will randomly appear on the tablist. If you don't put any names there then no fakeplayers will appear"
    })
    public List<String> fakePlayers = new ArrayList<>();

    @Comments({
            "servers which you wish to show their own tabList (The one provided by bukkit)"
    })
    public List<String> excludeServers = new ArrayList<>();

    @Comments({
            "servers which you wish to hide from the global tabList",
            "Note that this is different from excludeServers above: this hides all players on the hidden servers from appearing",
            "on the tablist, whereas excluded servers' players are still on the BungeeTabListPlus tablist, but they do not see",
            "the global tab list"
    })
    public List<String> hiddenServers = new ArrayList<>();

    @Comments({
            "players which are permanently hidden from the tab list",
            "you can either put your username or your uuid (with dashes) here",
            "don't use this. you have absolutely no reason to hide from anyone. on your own server."
    })
    public List<String> hiddenPlayers = new ArrayList<>();

    // todo add more examples to comment, people keep asking for this
    @Comments({
            "Time zone to use for the {time} variable",
            "Can be full name like \"America/Los_Angeles\"",
            "or custom id like \"GMT+8\""
    })
    @Path("time-zone")
    public String timezone = TimeZone.getDefault().getID();

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(timezone);
    }

    public String getServerAlias(String name) {
        if (serverAlias.get(name) != null) {
            return serverAlias.get(name);
        }
        return name;
    }

    @Override
    public void update(ConfigSection section) {
        if (section.has("tablistUpdateIntervall") && !section.has("tablistUpdateInterval")) {
            section.set("tablistUpdateInterval", section.get("tablistUpdateIntervall"));
            section.remove("tablistUpdateIntervall");
        }

        if (section.has("online.text") && !section.has("online-text")) {
            section.set("online-text", section.get("online.text"));
            section.remove("online.text");
        }

        if (section.has("offline.text") && !section.has("offline-text")) {
            section.set("offline-text", section.get("offline.text"));
            section.remove("offline.text");
        }
    }
}
