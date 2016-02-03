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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class MainConfig extends Configuration {


    public double tablistUpdateInterval = 1;

    public boolean updateOnPlayerJoinLeave = true;

    public boolean updateOnServerChange = true;

    public boolean useScoreboardToBypass16CharLimit = true;

    public int charLimit = -1;

    public String permissionSource = "AUTO";

    public boolean showPlayersInGamemode3 = true;

    public boolean checkForUpdates = true;

    public boolean notifyAdminsIfUpdateAvailable = true;

    public boolean automaticallySendBugReports = true;

    public HashMap<String, String> serverAlias = new HashMap<>();

    {
        serverAlias.put("server1", "Spawn");
        serverAlias.put("server2", "Creative");
        serverAlias.put("server3", "PvP");
    }

    public HashMap<String, String> worldAlias = new HashMap<>();

    {
        worldAlias.put("server1:world1", "Spawn");
        worldAlias.put("server2:world", "Creative");
        worldAlias.put("server3:world", "PvP");
    }

    public HashMap<String, String> serverPrefixes = new HashMap<>();

    {
        serverPrefixes.put("Minigames", "&8(&bM&8)");
        serverPrefixes.put("SkyBlock", "&8(&dS&8) ");
    }

    public HashMap<String, String> prefixes = new HashMap<>();

    {
        prefixes.put("default", "");
        prefixes.put("admin", "&c[A] ");
    }

    public int pingDelay = -1;

    public String online_text = "&2 ON";

    public String offline_text = "&c OFF";

    public List<String> fakePlayers = new ArrayList<>();

    public List<String> excludeServers = new ArrayList<>();

    {
        excludeServers.add("server2");
        excludeServers.add("server7");
    }

    public List<String> hiddenServers = new ArrayList<>();

    {
        hiddenServers.add("server3");
        hiddenServers.add("server9");
    }

    public List<String> hiddenPlayers = new ArrayList<>();

    public boolean autoExcludeServers = false;

    public String timezone = TimeZone.getDefault().getID();

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(timezone);
    }

    public MainConfig() {
        setHeader("This is the Config File of BungeeTabListPlus",
                "You can find more detailed information on the wiki: https://github.com/CodeCrafter47/BungeeTabListPlus/wiki");
    }

    public String getServerAlias(String name) {
        if (serverAlias.get(name) != null) {
            return serverAlias.get(name);
        }
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void read(Map<Object, Object> map) {
        if (map.containsKey("tablistUpdateInterval")) {
            tablistUpdateInterval = parseDouble(map.get("tablistUpdateInterval"));
        } else if (map.containsKey("tablistUpdateIntervall")) {
            tablistUpdateInterval = parseDouble(map.get("tablistUpdateIntervall"));
        }

        if (map.containsKey("updateOnPlayerJoinLeave")) {
            updateOnPlayerJoinLeave = parseBoolean(map.get("updateOnPlayerJoinLeave"));
        }

        if (map.containsKey("updateOnServerChange")) {
            updateOnServerChange = parseBoolean(map.get("updateOnServerChange"));
        }

        if (map.containsKey("useScoreboardToBypass16CharLimit")) {
            useScoreboardToBypass16CharLimit = parseBoolean(map.get("useScoreboardToBypass16CharLimit"));
        }

        if (map.containsKey("charLimit")) {
            charLimit = parseInteger(map.get("charLimit"));
        }

        if (map.containsKey("permissionSource")) {
            permissionSource = map.get("permissionSource").toString();
        }

        if (map.containsKey("showPlayersInGamemode3")) {
            showPlayersInGamemode3 = parseBoolean(map.get("showPlayersInGamemode3"));
        }

        if (map.containsKey("checkForUpdates")) {
            checkForUpdates = parseBoolean(map.get("checkForUpdates"));
        }

        if (map.containsKey("notifyAdminsIfUpdateAvailable")) {
            notifyAdminsIfUpdateAvailable = parseBoolean(map.get("notifyAdminsIfUpdateAvailable"));
        }

        if (map.containsKey("automaticallySendBugReports")) {
            automaticallySendBugReports = parseBoolean(map.get("automaticallySendBugReports"));
        }

        if (map.containsKey("serverAlias")) {
            serverAlias = (HashMap<String, String>) map.get("serverAlias");
        }

        if (map.containsKey("worldAlias")) {
            worldAlias = (HashMap<String, String>) map.get("worldAlias");
        }

        if (map.containsKey("serverPrefixes")) {
            serverPrefixes = (HashMap<String, String>) map.get("serverPrefixes");
        }

        if (map.containsKey("prefixes")) {
            prefixes = (HashMap<String, String>) map.get("prefixes");
        }

        if (map.containsKey("pingDelay")) {
            pingDelay = parseInteger(map.get("pingDelay"));
        }

        if (map.containsKey("online-text")) {
            online_text = map.get("online-text").toString();
        } else if (map.containsKey("online")) {
            online_text = ((Map<Object, Object>) map.get("online")).get("text").toString();
        }

        if (map.containsKey("offline-text")) {
            offline_text = map.get("offline-text").toString();
        } else if (map.containsKey("offline")) {
            offline_text = ((Map<Object, Object>) map.get("offline")).get("text").toString();
        }

        if (map.containsKey("fakePlayers")) {
            fakePlayers = ((List<?>) map.get("fakePlayers")).stream().map(Object::toString).collect(Collectors.toList());
        }

        if (map.containsKey("excludeServers")) {
            excludeServers = (List<String>) map.get("excludeServers");
        }

        if (map.containsKey("hiddenServers")) {
            hiddenServers = (List<String>) map.get("hiddenServers");
        }

        if (map.containsKey("autoExcludeServers")) {
            autoExcludeServers = parseBoolean(map.get("autoExcludeServers"));
        }

        if (map.containsKey("time-zone")) {
            timezone = map.get("time-zone").toString();
        }

        hiddenPlayers = (List<String>) map.getOrDefault("hiddenPlayers", new ArrayList<>());
    }

    @Override
    protected void write() {
        writeComments("time in seconds after which the tabList will be resend to all players",
                "set this to -1 to disable scheduled update of the tabList");
        write("tablistUpdateInterval", tablistUpdateInterval);

        writeComment("whether tabList should be resend if a player joins or leaves the server");
        write("updateOnPlayerJoinLeave", updateOnPlayerJoinLeave);

        writeComment("whether tablist should be resend if a player switches the server");
        write("updateOnServerChange", updateOnServerChange);

        writeComments("whether to use scoreboard functions to bypass the 16 character limit",
                "does NOT conflict if other scoreboard plugins");
        write("useScoreboardToBypass16CharLimit", useScoreboardToBypass16CharLimit);

        writeComments("You can limit the number of characters per slot here",
                "Color codes do not count as a character; -1 means unlimited");
        write("charLimit", charLimit);

        writeComments("Decide from where BungeeTabListPlus takes information like permissions,",
                "prefix, suffix and group.",
                "Possible values:",
                "AUTO        - take best source",
                "BUKKIT      - take information from Bukkit/Vault",
                "BUKKITPERMISSIONSEX      - take information from Bukkit/PermissionsEx",
                "BUNGEEPERMS - take information from BungeePerms",
                "BUNGEE      - take group from bungee, prefix from config.yml, permissions from bungee");
        write("permissionSource", permissionSource);

        writeComment("whether to show players in spectator mode");
        write("showPlayersInGamemode3", showPlayersInGamemode3);

        writeComments("if enabled the plugin checks for new versions automatically.",
                "Use /BTLP to see whether a new version is available",
                "this does NOT automatically install an update");
        write("checkForUpdates", checkForUpdates);

        writeComment("this notifies admins (everyone with the permission `bungeetablistplus.admin`) if an update is available");
        write("notifyAdminsIfUpdateAvailable", notifyAdminsIfUpdateAvailable);

        writeComments("If this is set to true and the plugin encounters an issue a bug report is sent automatically",
                "Bug reports do not contain any sensitive or identifying information",
                "Bug reports contain the plugin name, plugin version and the error message that also appears in the server log");
        write("automaticallySendBugReports", automaticallySendBugReports);

        writeComment("server Alias fo the {server} Variable");
        write("serverAlias", serverAlias);

        writeComment("Alias fo the {world} Variable. Match 'server:world' to an alias");
        write("worldAlias", worldAlias);

        writeComments("list servers you wish to create custom prefixes for.",
                "to use the custom prefixes use the {serverPrefix} variable");
        write("serverPrefixes", serverPrefixes);

        writeComments("the prefixes used for the {prefix} variable, based upon permission groups",
                "IMPORTANT: these prefixes won't be used by default. see the wiki for details");
        write("prefixes", prefixes);

        writeComments("Interval (in seconds) at which all servers of your network get pinged to check whether they are online",
                "If you intend to use the {onlineState:SERVER} variable set this to 2 or any value you like",
                "setting this to -1 disables this feature");
        write("pingDelay", pingDelay);

        writeComment("replacement for the {onlineState} variable if the server is online");
        write("online-text", online_text);

        writeComment("replacement for the {onlineState} variable if the server is offline");
        write("offline-text", offline_text);

        writeComment("those fakeplayers will randomly appear on the tablist. If you don't put any names there then no fakeplayers will appear");
        write("fakePlayers", fakePlayers);

        writeComment("servers which you wish to show their own tabList (The one provided by bukkit)");
        write("excludeServers", excludeServers);

        writeComments("servers which you wish to hide from the global tabList",
                "Note that this is different from excludeServers above: this hides all players on the hidden servers from appearing",
                "on the tablist, whereas excluded servers' players are still on the BungeeTabListPlus tablist, but they do not see",
                "the global tab list");
        write("hiddenServers", hiddenServers);

        writeComments("players which are permanently hidden from the tab list",
                "you can either put your username or your uuid (with dashes) here",
                "don't use this. you have absolutely no reason to hide from anyone. on your own server.");
        write("hiddenPlayers", hiddenPlayers);

        writeComments("Detects which servers are using a bukkit-side tabList-plugin",
                "and lets them show it / doesn't show the tablist provided by this plugin on these servers",
                "This is disabled by default because it could be accidentially triggered by other plugins (Essentials nicknames etc.)",
                "Warning: This is an experimental feature, it may cause unintended behaviour");
        write("autoExcludeServers", autoExcludeServers);

        writeComments("Time zone to use for the {time} variable",
                "Can be full name like \"America/Los_Angeles\"",
                "or custom id like \"GMT+8\"");
        write("time-zone", timezone);
    }
}
