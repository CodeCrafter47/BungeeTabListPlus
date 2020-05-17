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

import com.google.common.collect.ImmutableList;
import de.codecrafter47.taboverlay.config.dsl.CustomPlaceholderConfiguration;
import de.codecrafter47.taboverlay.config.dsl.yaml.UpdateableConfig;
import de.codecrafter47.taboverlay.config.dsl.yaml.YamlUtil;
import lombok.val;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class MainConfig implements UpdateableConfig {

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
    public String time_zone = TimeZone.getDefault().getID();

    @Comment("Custom placeholders")
    public Map<String, CustomPlaceholderConfiguration> customPlaceholders = new HashMap<>();

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(time_zone);
    }

    @Comment({
            "Disables the custom tab list for players in spectators mode.",
            "As a result those players will see the vanilla tab list of the server.",
            "If you do not use this option players in spectator mode will see the ",
            "fake players created by BungeeTabListPlus in the teleport menu."
    })
    public boolean disableCustomTabListForSpectators = true;

    @Comment({
            "Removes the `~BTLP Slot ##` entries from tab completion if the.",
            "size of the tab list is 80 slots."
    })
    public boolean experimentalTabCompleteFixForTabSize80 = false;

    @Comment({
            "Replaces the `~BTLP Slot ##` entries in tab completion with smileys"
    })
    public boolean experimentalTabCompleteSmileys = false;

    public transient boolean needWrite = false;

    @Override
    public void update(MappingNode node) {
        val outdatedConfigOptions = ImmutableList.<String>of("tablistUpdateIntervall",
                "tablistUpdateInterval",
                "updateOnPlayerJoinLeave",
                "updateOnServerChange",
                "offline",
                "offline-text",
                "online",
                "online-text",
                "permissionSource",
                "useScoreboardToBypass16CharLimit",
                "autoExcludeServers",
                "showPlayersInGamemode3",
                "serverAlias",
                "worldAlias",
                "serverPrefixes",
                "prefixes",
                "charLimit",
                "automaticallySendBugReports");

        for (String option : outdatedConfigOptions) {
            needWrite |= YamlUtil.contains(node, option);
            YamlUtil.remove(node, option);
        }

        val newConfigOptions = ImmutableList.<String>of(
                "disableCustomTabListForSpectators",
                "experimentalTabCompleteFixForTabSize80",
                "experimentalTabCompleteSmileys"
        );

        for (String option : newConfigOptions) {
            needWrite |= !YamlUtil.contains(node, option);
        }
    }

    public void writeWithComments(Writer writer, Yaml yaml) throws IOException {
        writeCommentLine(writer, "This is the configuration file of BungeeTabListPlus");
        writeCommentLine(writer, "See https://github.com/CodeCrafter47/BungeeTabListPlus/wiki for additional information");

        String ser = yaml.dumpAs(this, Tag.MAP, null);

        Map<String, String[]> comments = new HashMap<>();
        for (Field field : MainConfig.class.getDeclaredFields()) {
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                    if (Modifier.isPublic(modifiers)) {
                        Path path = field.getAnnotation(Path.class);
                        comments.put(path != null ? path.value() : field.getName(), comment.value());
                    }
                }
            }
        }

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(ser.split("\n")));

        ListIterator<String> iterator = lines.listIterator();

        while (iterator.hasNext()) {
            String line = iterator.next();
            for (Map.Entry<String, String[]> entry : comments.entrySet()) {
                if (line.startsWith(entry.getKey())) {
                    String[] value = entry.getValue();
                    iterator.previous();
                    iterator.add("");
                    for (String comment : value) {
                        iterator.add("# " + comment);
                    }
                    iterator.next();
                }
            }
        }

        for (String line : lines) {
            writer.write(line);
            writer.write("\n");
        }

        writer.close();
    }

    private static void writeCommentLine(Writer writer, String comment) throws IOException {
        writer.write("# " + comment + "\n");
    }
}
