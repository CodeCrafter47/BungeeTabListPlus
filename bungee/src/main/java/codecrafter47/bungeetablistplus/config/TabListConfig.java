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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TabListConfig extends Config {

    @Comments({
            "Defines to which players this tabList applies",
            "No effect in the default tabList", "possible values:",
            "'Player:<Name>' for specific Player",
            "'Players:<player1>,<player2>' for multiple Players",
            "'Server:<Server>' for all Players on that Server",
            "'Servers:<server1>,<server2>' for all Players which are on These Servers",
            "'group:<group>' for all players within that permission group",
            "'groups:<group1>,<group2>' same with multiple groups",
            "'1.7' for all players with client version 1.7 or below",
            "'1.8' for all players with client version 1.8 or above",
            "'all' for all players"
    })
    public String showTo = "all";

    @Comments({
            "This text will be shown above the tablist"
    })
    public String header = "&bWelcome &6{player}";

    @Comments({
            "This text will be shown below the tablist"
    })
    public String footer = "&4play.minecraft.example.com";

    @Comments({
            "whether to shown header/footer or not. You should set this to false if you wan to use a bukkit/spigot side plugin for that."
    })
    public boolean shownFooterHeader = true;

    @Comments({
            "The skin shown for non-players",
            "leave empty for default skins"
    })
    public String defaultSkin = "MHF_Question";

    @Comments({
            "ping value tu use for non-player slots, ",
            "used if no other value is specified using [PING=?]"
    })
    public int defaultPing = 0;

    @Comments({
            "if true player skin is shown beneath name, otherwise default skin"
    })
    public boolean showCorrectPlayerSkins = true;

    @Comments({
            "1.8 ONLY",
            "When enabled the tablist will adjust it's size to the number of players online/ slots used, instead of using",
            "the static tab_size set in bungee's config.yml.",
            "WARNING: This is an experimental feature an will most likely cause bugs",
            "WARNING: [ALIGN=LEFT] and other formatting tags will not operate correctly",
            "WARNING: If this is enabled it is STRONGLY recommended to also enable verticalMode"
    })
    public boolean autoShrinkTabList = false;

    @Comments({
            "how Players should be grouped",
            "You can use 'SERVER' or 'NONE'"
    })
    public String groupPlayers = "SERVER";

    @Comments({
            "Whether to Show Groups with no Players",
            "not effective if groupPlayers=NONE"
    })
    public boolean showEmptyGroups = false;

    @Comments({
            "If enabled slots are filled top-to-bottom instead of left-to-right"
    })
    public boolean verticalMode = false;

    @Comments({
            "This is how a group looks in the tabList",
            "Use {fillplayers} at the point where you",
            "wish the players to be listet",
            "not effective if groupPlayers=NONE",
            "You can also use {fillplayers:<group>}"
    })
    public List<String> groupLines = new ArrayList<>();

    @Comments({
            "This allows you to change the way players are listet",
            "You can also use multiple slots to displaye additional information"
    })
    public List<String> playerLines = new ArrayList<>();

    @Comments({
            "These lines are shown if there's not enough space",
            "for all players upon the tabList"
    })
    public List<String> morePlayersLines = new ArrayList<>();

    @Comments({
            "And here finally is the tabList",
            "Use {fillplayers} at the point where",
            "you want the players to be shown.",
            "You can also use {fillplayers:<group>} or {fillplayers:<server>}",
            "Use [ALIGN BOTTOM] to state that the folowing",
            "lines should be shown at the bottom of the tabList",
            "You can also use [ALIGN LEFT]",
            "You can use Variables to display dynamic content",
            "more information at https://github.com/CodeCrafter47/BungeeTabListPlus/wiki"
    })
    public List<String> tabList = new ArrayList<>();

    {
        groupLines.add("[ALIGN LEFT][SKIN=MHF_ArrowRight]&9&l>&9 {server}({server_player_count}):");
        groupLines.add("{fillplayers}");
    }

    {
        playerLines.add("{tabName}");
    }

    {
        morePlayersLines.add("... and {other_count} others");
    }

    {
        tabList.add("&2>>>>>>>>>>>>");
        tabList.add("&aWelcome");
        tabList.add("&2<<<<<<<<<<<<");
        tabList.add("&2>>>>>>>>>>>>");
        tabList.add("&a{player}");
        tabList.add("&2<<<<<<<<<<<<");
        tabList.add(" ");
        tabList.add(" ");
        tabList.add(" ");
        tabList.add("{fillplayers}");
        tabList.add("[ALIGN BOTTOM]&2============");
        tabList.add("&2============");
        tabList.add("&2============");
        tabList.add("&2Time: &a{time}");
        tabList.add("&2Players: &a{players}");
        tabList.add("&2Balance: &a{balance}");
    }

    public TabListConfig(Plugin plugin, String filename) throws
            InvalidConfigurationException {
        CONFIG_FILE = new File(
                plugin.getDataFolder() + File.separator + "tabLists", filename);
        CONFIG_HEADER = new String[]{
                "", "This the default TabList",
                "It is shown to all users which haven't got another TabList", "",
                "Create a copy of this File, name it like you wish",
                "and change the showTo and some other options",
                "to create another TabList only shown to some users.",
                "By doing this you can for example create",
                "a special TabList for VIPs / Admins or",
                "create a tabList only shown o one server",
                "You can find more information on the wiki https://github.com/CodeCrafter47/BungeeTabListPlus/wiki",
                ""
        };
        init();
        save();
    }

    public boolean appliesTo(ProxiedPlayer player) {
        if (showTo.equalsIgnoreCase("ALL")) {
            return true;
        }

        if (showTo.equalsIgnoreCase("1.8")) {
            return BungeeTabListPlus.getInstance().getProtocolVersionProvider().getProtocolVersion(player) >= 47;
        }

        if (showTo.equalsIgnoreCase("1.7")) {
            return BungeeTabListPlus.getInstance().getProtocolVersionProvider().getProtocolVersion(player) < 47;
        }

        String s[] = showTo.split(":");

        if (s.length != 2) {
            return false;
        }

        if (s[0].equalsIgnoreCase("player")) {
            if (s[1].equalsIgnoreCase(player.getName())) {
                return true;
            }
        }

        if (s[0].equalsIgnoreCase("players")) {
            for (String p : s[1].split(",")) {
                if (p.equalsIgnoreCase(player.getName())) {
                    return true;
                }
            }
        }

        if (player.getServer() != null) {
            String server = player.getServer().getInfo().getName();

            if (s[0].equalsIgnoreCase("server")) {
                if (s[1].equalsIgnoreCase(server)) {
                    return true;
                }
            }

            if (s[0].equalsIgnoreCase("servers")) {
                for (String sv : s[1].split(",")) {
                    if (sv.equalsIgnoreCase(server)) {
                        return true;
                    }
                }
            }
        }

        String group = BungeeTabListPlus.getInstance().getPermissionManager().
                getMainGroup(BungeeTabListPlus.getInstance().getBungeePlayerProvider().wrapPlayer(player));

        if (group != null) {
            if (s[0].equalsIgnoreCase("group")) {
                if (s[1].equalsIgnoreCase(group)) {
                    return true;
                }
            }

            if (s[0].equals("groups")) {
                for (String sv : s[1].split(",")) {
                    if (sv.equalsIgnoreCase(group)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public String getFileName() {
        return this.CONFIG_FILE.getName();
    }
}
