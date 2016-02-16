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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.common.Configuration;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TabListConfig extends Configuration {

    public String showTo = "all";

    public int priority = 1;

    public List<String> header = Lists.newArrayList(
            "&cWelcome &f{player}",
            "&eW&celcome &f{player}",
            "&eWe&clcome &f{player}",
            "&eWel&ccome &f{player}",
            "&eWelc&come &f{player}",
            "&eWelco&cme &f{player}",
            "&eWelcom&ce &f{player}",
            "&eWelcome &f{player}",
            "&cW&eelcome &f{player}",
            "&cWe&elcome &f{player}",
            "&cWel&ecome &f{player}",
            "&cWelc&eome &f{player}",
            "&cWelco&eme &f{player}",
            "&cWelcom&ee &f{player}",
            "&cWelcome &f{player}"
    );

    public double headerCycleInterval = 0.20;

    public List<String> footer = Lists.newArrayList("&f&oPowered by BungeeTabListPlus");

    public double footerCycleInterval = 0.5;

    public boolean shownFooterHeader = true;

    public String defaultSkin = "colors/dark_gray.png";

    public int defaultPing = 1000;

    public boolean autoShrinkTabList = false;

    public String groupPlayers = "SERVER";

    public boolean showEmptyGroups = false;

    public boolean verticalMode = false;

    public List<String> groupLines = new ArrayList<>();

    public List<String> playerLines = new ArrayList<>();

    public List<String> morePlayersLines = new ArrayList<>();

    public List<String> tabList = new ArrayList<>();

    public int tab_size = 60;

    private final transient String name;

    {
        groupLines.add("[ALIGN LEFT][SKIN=colors/yellow.png]&e&n{server}&f&o ({server_player_count}):");
        groupLines.add("{fillplayers}");
        groupLines.add("[ALIGN LEFT][PING=1000]");
    }

    {
        playerLines.add("{tabName}");
    }

    {
        morePlayersLines.add("[SKIN=colors/gray.png][PING=0]&7... and &e{other_count} &7others");
    }

    {
        tabList.add("[SKIN=default/server.png][PING=0]&cServer: &6{server}");
        tabList.add("[SKIN=default/rank.png][PING=0]&cRank: &6{group}");
        tabList.add("[SKIN=default/ping.png][PING=0]&cPing: &6{ping}ms");
        tabList.add("[PING=1000]");
        tabList.add("[PING=1000]");
        tabList.add("[PING=1000]");
        tabList.add("{fillplayers}");
        tabList.add("[ALIGN BOTTOM][SKIN=colors/gold.png][PING=0]&6==============");
        tabList.add("[SKIN=colors/gold.png][PING=0]&6==============");
        tabList.add("[SKIN=colors/gold.png][PING=0]&6==============");
        tabList.add("[SKIN=default/clock.png][PING=0]&cTime: &6{time}");
        tabList.add("[SKIN=default/players.png][PING=0]&cPlayers: &6{players}");
        tabList.add("[SKIN=default/balance.png][PING=0]&cBalance: &6{balance}");
    }

    public TabListConfig(String name) {
        this.name = name;
        setHeader(
                "", "This the default TabList",
                "It is shown to all users which haven't got another TabList", "",
                "Create a copy of this File, name it like you wish",
                "and change the showTo and some other options",
                "to create another TabList only shown to some users.",
                "By doing this you can for example create",
                " - a special tab list for VIPs / Admins or",
                " - a tab list that is only shown on one server",
                "You can find more information on the wiki https://github.com/CodeCrafter47/BungeeTabListPlus/wiki",
                ""
        );
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

        String[] s = showTo.split(":");

        if (s.length != 2) {
            return false;
        }

        if (s[0].equalsIgnoreCase("player")) {
            if (s[1].equalsIgnoreCase(player.getName()) || s[1].equalsIgnoreCase(player.getUniqueId().toString())) {
                return true;
            }
        }

        if (s[0].equalsIgnoreCase("players")) {
            for (String p : s[1].split(",")) {
                if (p.equalsIgnoreCase(player.getName()) || p.equalsIgnoreCase(player.getUniqueId().toString())) {
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
                getMainGroup(BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayer(player));

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

    @Override
    @SuppressWarnings("unchecked")
    protected void read(Map<Object, Object> map) {
        if (map.containsKey("showTo")) {
            showTo = map.get("showTo").toString();
        }

        priority = parseInteger(map.getOrDefault("priority", priority));

        if (map.containsKey("header")) {
            Object header = map.get("header");
            if (header instanceof List) {
                this.header = (List<String>) header;
            } else {
                this.header = Lists.newArrayList(header.toString());
            }
        }

        headerCycleInterval = parseDouble(map.getOrDefault("headerAnimationUpdateInterval", headerCycleInterval));
        if (headerCycleInterval <= 0) {
            headerCycleInterval = 1;
        }

        if (map.containsKey("footer")) {
            Object footer = map.get("footer");
            if (footer instanceof List) {
                this.footer = (List<String>) footer;
            } else {
                this.footer = Lists.newArrayList(footer.toString());
            }
        }

        footerCycleInterval = parseDouble(map.getOrDefault("footerAnimationUpdateInterval", footerCycleInterval));
        if (footerCycleInterval <= 0) {
            footerCycleInterval = 1;
        }

        if (map.containsKey("shownFooterHeader")) {
            shownFooterHeader = parseBoolean(map.get("shownFooterHeader"));
        }

        if (map.containsKey("defaultSkin")) {
            defaultSkin = map.get("defaultSkin").toString();
        }

        if (map.containsKey("defaultPing")) {
            defaultPing = parseInteger(map.get("defaultPing"));
        }

        if (map.containsKey("autoShrinkTabList")) {
            autoShrinkTabList = parseBoolean(map.get("autoShrinkTabList"));
        }

        if (map.containsKey("groupPlayers")) {
            groupPlayers = map.get("groupPlayers").toString();
        }

        if (map.containsKey("showEmptyGroups")) {
            showEmptyGroups = parseBoolean(map.get("showEmptyGroups"));
        }

        if (map.containsKey("verticalMode")) {
            verticalMode = parseBoolean(map.get("verticalMode"));
        }

        if (map.containsKey("groupLines")) {
            groupLines = (List<String>) map.get("groupLines");
        }

        if (map.containsKey("playerLines")) {
            playerLines = (List<String>) map.get("playerLines");
        }

        if (map.containsKey("morePlayersLines")) {
            morePlayersLines = (List<String>) map.get("morePlayersLines");
        }

        if (map.containsKey("tabList")) {
            tabList = (List<String>) map.get("tabList");
        }

        tab_size = parseInteger(map.getOrDefault("tab_size", 80));
        if (tab_size <= 0) {
            tab_size = 1;
        }
        if (tab_size > 80) {
            tab_size = 80;
        }
    }

    @Override
    protected void write() {
        writeComments("Defines to which players this tabList applies",
                "No effect in the default tabList", "possible values:",
                "'Player:<Name>' for specific Player",
                "'Players:<player1>,<player2>' for multiple Players",
                "'Server:<Server>' for all Players on that Server",
                "'Servers:<server1>,<server2>' for all Players which are on These Servers",
                "'group:<group>' for all players within that permission group",
                "'groups:<group1>,<group2>' same with multiple groups",
                "'1.7' for all players with client version 1.7 or below",
                "'1.8' for all players with client version 1.8 or above",
                "'all' for all players");
        write("showTo", showTo);

        writeComments("If multiple tab list are available for a player the plugin",
                "chooses the tab list with the highest priority");
        write("priority", priority);

        writeComments("This text will be shown above the tablist",
                "Add multiple lines to create an animation");
        write("header", header);

        writeComments("Interval (in seconds) at which the header animation is updated",
                "Use this to configure the speed to the animation");
        write("headerAnimationUpdateInterval", headerCycleInterval);

        writeComments("This text will be shown below the tablist",
                "Add multiple lines to create an animation");
        write("footer", footer);

        writeComments("Interval (in seconds) at which the footer animation is updated",
                "Use this to configure the speed to the animation");
        write("footerAnimationUpdateInterval", footerCycleInterval);

        writeComment("whether to shown header/footer or not. You should set this to false if you wan to use a bukkit/spigot side plugin for that.");
        write("shownFooterHeader", shownFooterHeader);

        writeComments("The skin shown for non-players",
                "leave empty for default skins");
        write("defaultSkin", defaultSkin);

        writeComments("ping value tu use for non-player slots, ",
                "used if no other value is specified using [PING=?]");
        write("defaultPing", defaultPing);

        writeComments("1.8 ONLY",
                "When enabled the tablist will adjust it's size to the number of players online/ slots used, instead of using",
                "the static tab_size set in bungee's config.yml.",
                "WARNING: This is an experimental feature an will most likely cause bugs",
                "WARNING: [ALIGN=LEFT] and other formatting tags will not operate correctly",
                "WARNING: If this is enabled it is STRONGLY recommended to also enable verticalMode");
        write("autoShrinkTabList", autoShrinkTabList);

        writeComments("1.8 ONLY",
                "Number of slots in the tab list. Please not that this only affects 1.8 clients",
                "tab_size for 1.7 and older clients is configured in bungee's config.yml",
                "Can be from 1 to 80.");
        write("tab_size", tab_size);

        writeComments("how Players should be grouped",
                "You can use 'SERVER' or 'NONE'");
        write("groupPlayers", groupPlayers);

        writeComments("Whether to Show Groups with no Players",
                "not effective if groupPlayers=NONE");
        write("showEmptyGroups", showEmptyGroups);

        writeComment("If enabled slots are filled top-to-bottom instead of left-to-right");
        write("verticalMode", verticalMode);

        writeComments("This is how a group looks in the tabList",
                "Use {fillplayers} at the point where you",
                "wish the players to be listet",
                "not effective if groupPlayers=NONE",
                "You can also use {fillplayers:<group>}");
        write("groupLines", groupLines);

        writeComments("This allows you to change the way players are listed",
                "You can also use multiple slots to display additional information");
        write("playerLines", playerLines);

        writeComments("These lines are shown if there's not enough space",
                "for all players upon the tabList");
        write("morePlayersLines", morePlayersLines);

        writeComments("And here finally is the tabList",
                "Use {fillplayers} at the point where",
                "you want the players to be shown.",
                "You can also use {fillplayers:<group>} or {fillplayers:<server>}",
                "Use [ALIGN BOTTOM] to state that the following",
                "lines should be shown at the bottom of the tabList",
                "You can also use [ALIGN LEFT]",
                "You can use Variables to display dynamic content",
                "more information at https://github.com/CodeCrafter47/BungeeTabListPlus/wiki");
        write("tabList", tabList);
    }

    public String getName() {
        return name;
    }
}
