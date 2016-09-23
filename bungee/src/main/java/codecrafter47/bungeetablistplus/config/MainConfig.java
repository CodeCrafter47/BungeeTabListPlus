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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.*;

import static codecrafter47.bungeetablistplus.yamlconfig.YamlUtil.*;

public class MainConfig implements UpdatableConfig {

    @Comment({
            "time in seconds after which the tabList will be resend to all players",
            "set this to -1 to disable scheduled update of the tabList",
            "This option will be removed soon. Don't use it anymore."
    })
    public double tablistUpdateInterval = 1;

    @Comment({
            "whether tabList should be resend if a player joins or leaves the server",
            "This option will be removed soon. Don't use it anymore."
    })
    public boolean updateOnPlayerJoinLeave = true;

    @Comment({
            "whether tablist should be resend if a player switches the server",
            "This option will be removed soon. Don't use it anymore."
    })
    public boolean updateOnServerChange = true;

    @Comment({
            "You can limit the number of characters per slot here",
            "Color codes do not count as a character; -1 means unlimited",
            "This option will be removed soon. Don't use it anymore."
    })
    public int charLimit = -1;

    @Comment({
            "Decide from where BungeeTabListPlus takes information like permissions,",
            "prefix, suffix and group.",
            "Possible values:",
            "AUTO        - take best source",
            "BUKKIT      - take information from Bukkit/Vault",
            "BUNGEEPERMS - take information from BungeePerms",
            "BUNGEE      - take group from bungee, prefix from config.yml, permissions from bungee",
            "This option will be removed soon. Don't use it anymore."
    })
    public String permissionSource = PermissionSource.AUTO.toString();

    public PermissionSource permissionSourceValue() {
        return PermissionSource.valueOf(permissionSource.toUpperCase(Locale.ROOT));
    }

    @Comment({
            "whether to show players in spectator mode",
            "This option will be removed soon. Don't use it anymore."
    })
    public boolean showPlayersInGamemode3 = true;

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
            "This option will be removed soon. Don't use it anymore.",
            "See https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating#server-alias"
    })
    public HashMap<String, String> serverAlias = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("factions", "Factions")
            .put("lobby0", "Lobby")
            .put("lobby1", "Lobby")
            .put("sg", "Survival Games")
            .build());

    @Comment({
            "This option will be removed soon. Don't use it anymore.",
            "See https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating#world-alias"
    })
    public HashMap<String, String> worldAlias = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("factions:world", "Overworld")
            .put("factions:world_nether", "Nether")
            .put("factions:world_end", "The End")
            .build());

    @Comment({
            "This option will be removed soon. Don't use it anymore.",
            "See https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating#server-prefix"
    })
    public HashMap<String, String> serverPrefixes = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("Minigames", "&8(&bM&8)")
            .put("SkyBlock", "&8(&dS&8) ")
            .build());

    @Comment({
            "This option will be removed soon. Don't use it anymore.",
            "See https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating#prefixes-in-configyml"
    })
    public HashMap<String, String> prefixes = Maps.newHashMap(ImmutableMap.<String, String>builder()
            .put("default", "")
            .put("admin", "&c[A] ")
            .build());

    @Comment({
            "Interval (in seconds) at which all servers of your network get pinged to check whether they are online",
            "If you intend to use the {onlineState:SERVER} variable set this to 2 or any value you like",
            "setting this to -1 disables this feature"
    })
    public int pingDelay = -1;

    @Comment({
            "This option will be removed soon. Don't use it anymore.",
            "See https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating#the-onlinestate-placeholder"
    })
    @Path("online-text")
    public String online_text = "&2 ON";

    @Comment({
            "This option will be removed soon. Don't use it anymore.",
            "See https://github.com/CodeCrafter47/BungeeTabListPlus/wiki/Updating#the-onlinestate-placeholder"
    })
    @Path("offline-text")
    public String offline_text = "&c OFF";

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

    // todo add more examples to comment, people keep asking for this
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

    public String getServerAlias(String name) {
        if (serverAlias.get(name) != null) {
            return serverAlias.get(name);
        }
        return name;
    }

    @Override
    public void update(MappingNode section) {
        if (contains(section, "tablistUpdateIntervall") && !contains(section, "tablistUpdateInterval")) {
            put(section, "tablistUpdateInterval", get(section, "tablistUpdateIntervall"));
            remove(section, "tablistUpdateIntervall");
        }

        if (!contains(section, "offline-text") && contains(section, "offline") && get(section, "offline") instanceof MappingNode) {
            MappingNode subsection = (MappingNode) get(section, "offline");
            if (contains(subsection, "text")) {
                put(section, "offline-text", get(subsection, "text"));
            }
            remove(section, "offline");
        }

        if (!contains(section, "online-text") && contains(section, "online") && get(section, "online") instanceof MappingNode) {
            MappingNode subsection = (MappingNode) get(section, "online");
            if (contains(subsection, "text")) {
                put(section, "online-text", get(subsection, "text"));
            }
            remove(section, "online");
        }

        if (contains(section, "permissionSource")) {
            Node permissionSource = get(section, "permissionSource");
            if (permissionSource instanceof ScalarNode) {
                String value = ((ScalarNode) permissionSource).getValue().toUpperCase(Locale.ROOT);
                if (value.equals("BUKKITPERMISSIONSEX")) {
                    value = "BUKKIT";
                }
                put(section, "permissionSource", new ScalarNode(permissionSource.getTag(), permissionSource.isResolved(), value, permissionSource.getStartMark(), permissionSource.getEndMark(), ((ScalarNode) permissionSource).getStyle()));
            }
        }

        remove(section, "useScoreboardToBypass16CharLimit");

        remove(section, "autoExcludeServers");
    }
}
