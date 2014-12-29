/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainConfig extends Config {

    @Comments({
            "time in seconds after which the tabList will be resend to all players",
            "set this to -1 to disable scheduled update of the tabList"
    })
    public final double tablistUpdateIntervall = 1;

    @Comments({
            "whether tabList should be resend if a player joins or leaves the server"
    })
    public final boolean updateOnPlayerJoinLeave = true;

    @Comments({
            "whether tablist should be resend if a player switches the server"
    })
    public final boolean updateOnServerChange = true;

    @Comments({
            "whether to use scoreboard functions to bypass the 16 character limit",
            "does NOT conflict if other scoreboard plugins"
    })
    public boolean useScoreboardToBypass16CharLimit = true;

    @Comments({
            "You can limit the number of characters per slot here",
            "Color codes do not count as a character; -1 means unlimited"
    })
    public final int charLimit = -1;

    @Comments({
            "Decide from where BungeeTabListPlus takes informations like permissions,",
            "prefix, suffix and group.",
            "Possible values:",
            "AUTO        - take best source",
            "BUKKIT      - take informations from bukkit/vault",
            "BUNGEEPERMS - take informations from bungeeperms",
            "BUNGEE      - take group from bungee, prefix from config.yml, permissions from bungee"
    })
    public final String permissionSource = "AUTO";

    @Comments({
            "whether ping is sent to clients",
            "setting this to false can help you reducing network traffic"
    })
    public final boolean sendPing = true;

    @Comments({
            "if enabled the plugin checks for new versions automatically.",
            "Use /BTLP to see whether a new version is available",
            "this does NOT automatically install an update"
    })
    public final boolean checkForUpdates = true;

    @Comments({
            "this notifies admins (everyone with the permission `bungeetablistplus.admin`) if an update is available"
    })
    public final boolean notifyAdminsIfUpdateAvailable = true;

    @Comments({
            "server Alias fo the {server} Variable"
    })
    public final HashMap<String, String> serverAlias = new HashMap<>();

    {
        serverAlias.put("server1", "Spawn");
        serverAlias.put("server2", "Creative");
        serverAlias.put("server3", "PvP");
    }

    @Comments({
            "Alias fo the {world} Variable. Match 'server:world' to an alias"
    })
    public final HashMap<String, String> worldAlias = new HashMap<>();

    {
        worldAlias.put("server1:world1", "Spawn");
        worldAlias.put("server2:world", "Creative");
        worldAlias.put("server3:world", "PvP");
    }

    @Comments({
            "list servers you wish to create custom prefixes for.",
            "the use of custom prefixes would use a new variable such as {serverPrefix}",
            "the name used below should be the alias name setup in the plugin's config file",
            "same used for {fillplayers:<server>} function"
    })
    public final HashMap<String, String> serverPrefixes = new HashMap<>();

    {
        serverPrefixes.put("Minigames", "&8(&bM&8)");
        serverPrefixes.put("SkyBlock", "&8(&dS&8) ");
    }

    @Comments({
            "the prefixes used for the {prefix} variable, based upon permission groups",
            "IMPORTANT: these prefixes won't be used by default. see the wiki for details"
    })
    public final HashMap<String, String> prefixes = new HashMap<>();

    {
        prefixes.put("default", "");
        prefixes.put("admin", "&c[A] ");
    }

    @Comments({
            "Interval (in seconds) at which all servers of your network get pinged to check whether they are online",
            "If you intend to use the {onlineState:SERVER} variable set this to 2 or any value you like",
            "setting this to -1 disables this feature"
    })
    public final int pingDelay = -1;

    @Comment(
            "replacement for the {onlineState} variable if the server is online")
    public final String online_text = "&2 ON";

    @Comment(
            "replacement for the {onlineState} variable if the server is offline")
    public final String offline_text = "&c OFF";

    @Comments({
            "those fakeplayers will randomly appear on the tablist. If you don't put any names there then no fakeplayers will appear"
    })
    public final List<String> fakePlayers = new ArrayList<>();

    @Comments({
            "servers which you wish to show their own tabList (The one provided by bukkit)"
    })
    public final List<String> excludeServers = new ArrayList<>();

    {
        excludeServers.add("server2");
        excludeServers.add("server7");
    }

    @Comments({
            "Detects which servers are using a bukkit-side tabList-plugin",
            "and lets them show it / doesn't show the tablist provided by this plugin on these servers",
            "This is disabled by default because it could be accidentially triggered by other plugins (Essentials nicknames etc.)",
            "Warning: This is an experimental feature, it may cause unintended behaviour"
    })
    public boolean autoExcludeServers = false;

    public MainConfig(Plugin plugin) throws InvalidConfigurationException {
        CONFIG_FILE = new File("plugins" + File.separator + plugin.
                getDescription().getName(), "config.yml");
        CONFIG_HEADER = new String[]{
                "This is the Config File of BungeeTabListPlus",
                "You can find more detailed information on the wiki: https://github.com/CodeCrafter47/BungeeTabListPlus/wiki"};

        this.init();
    }

    public String getServerAlias(String name) {
        if (serverAlias.get(name) != null) {
            return serverAlias.get(name);
        }
        return name;
    }
}
