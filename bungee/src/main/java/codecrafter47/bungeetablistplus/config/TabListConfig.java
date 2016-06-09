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
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.ConfigSection;
import net.cubespace.Yamler.Config.Path;
import net.cubespace.Yamler.Config.YamlConfig;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.List;

public class TabListConfig extends YamlConfig {

    @Getter
    private final transient String name;

    public TabListConfig(String name) {
        this.name = name;
        CONFIG_HEADER = new String[]{
                "",
                "This the default TabList",
                "It is shown to all users which haven't got another TabList",
                "",
                "Create a copy of this File, name it like you wish",
                "and change the showTo and some other options",
                "to create another TabList only shown to some users.",
                "By doing this you can for example create",
                " - a special tab list for VIPs / Admins or",
                " - a tab list that is only shown on one server",
                "You can find more information on the wiki https://github.com/CodeCrafter47/BungeeTabListPlus/wiki",
                ""
        };
    }

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
            "If multiple tab list are available for a player the plugin",
            "chooses the tab list with the highest priority"
    })
    public int priority = 1;

    @Comments({
            "This text will be shown above the tablist",
            "Add multiple lines to create an animation"
    })
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

    @Comments({
            "Interval (in seconds) at which the header animation is updated",
            "Use this to configure the speed of the animation"
    })
    @Path("headerAnimationUpdateInterval")
    public double headerCycleInterval = 0.20;

    @Comments({
            "This text will be shown below the tablist",
            "Add multiple lines to create an animation"
    })
    public List<String> footer = Lists.newArrayList(
            "&f&oPowered by BungeeTabListPlus"
    );

    @Comments({
            "Interval (in seconds) at which the footer animation is updated",
            "Use this to configure the speed to the animation"
    })
    @Path("footerAnimationUpdateInterval")
    public double footerCycleInterval = 0.5;

    @Comments({
            "whether to shown header/footer or not. You should set this to false if you wan to use a bukkit/spigot side plugin for that."
    })
    public boolean shownFooterHeader = true;

    @Comments({
            "The skin shown for non-players",
            "leave empty for default skins"
    })
    public String defaultSkin = "colors/dark_gray.png";

    @Comments({
            "ping value tu use for non-player slots, ",
            "used if no other value is specified using [PING=?]"
    })
    public int defaultPing = 1000;

    @Comments({
            "1.8 and later versions ONLY",
            "When enabled the tablist will adjust it's size to the number of players online/ slots used, instead of using",
            "the static tab_size set in bungee's config.yml.",
            "WARNING: This is an experimental feature an will most likely cause bugs",
            "WARNING: [ALIGN=LEFT] and other formatting tags will not operate correctly",
            "WARNING: If this is enabled it is STRONGLY recommended to also enable verticalMode"
    })
    public boolean autoShrinkTabList = false;

    @Comments({
            "Number of slots in the tab list.",
            "If you have 1.7 or older clients this must match the value in bungee's config.yml",
            "Can be from 1 to 80."
    })
    public int tab_size = 60;

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
    public List<String> groupLines = Lists.newArrayList(
            "[ALIGN LEFT][SKIN=colors/yellow.png]&e&n{server}&f&o ({server_player_count}):",
            "{fillplayers}",
            "[ALIGN LEFT][PING=1000]"
    );

    @Comments({
            "This allows you to change the way players are listed",
            "You can also use multiple slots to display additional information"
    })
    public List<String> playerLines = Lists.newArrayList(
            "{tabName}"
    );

    @Comments({
            "These lines are shown if there's not enough space",
            "for all players upon the tabList"
    })
    public List<String> morePlayersLines = Lists.newArrayList(
            "[SKIN=colors/gray.png][PING=0]&7... and &e{other_count} &7others"
    );

    @Comments({
            "And here finally is the tabList",
            "Use {fillplayers} at the point where",
            "you want the players to be shown.",
            "You can also use {fillplayers:<group>} or {fillplayers:<server>}",
            "Use [ALIGN BOTTOM] to state that the following",
            "lines should be shown at the bottom of the tabList",
            "You can also use [ALIGN LEFT]",
            "You can use Variables to display dynamic content",
            "more information at https://github.com/CodeCrafter47/BungeeTabListPlus/wiki"
    })
    public List<String> tabList = Lists.newArrayList(
            "[SKIN=default/server.png][PING=0]&cServer: &6{server}",
            "[SKIN=default/rank.png][PING=0]&cRank: &6{group}",
            "[SKIN=default/ping.png][PING=0]&cPing: &6{ping}ms",
            "[PING=1000]",
            "[PING=1000]",
            "[PING=1000]",
            "{fillplayers}",
            "[ALIGN BOTTOM][SKIN=colors/gold.png][PING=0]&6==============",
            "[SKIN=colors/gold.png][PING=0]&6==============",
            "[SKIN=colors/gold.png][PING=0]&6==============",
            "[SKIN=default/clock.png][PING=0]&cTime: &6{time}",
            "[SKIN=default/players.png][PING=0]&cPlayers: &6{players}",
            "[SKIN=default/balance.png][PING=0]&cBalance: &6{balance}"
    );

    public boolean appliesTo(ProxiedPlayer player) {
        if (showTo.equalsIgnoreCase("ALL")) {
            return true;
        }

        if (showTo.equalsIgnoreCase("1.8")) {
            return BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player);
        }

        if (showTo.equalsIgnoreCase("1.7")) {
            return !BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player);
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

        Server playerServer = player.getServer();
        if (playerServer != null) {
            String server = playerServer.getInfo().getName();

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

        ConnectedPlayer connectedPlayer = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayerIfPresent(player);
        if (connectedPlayer != null) {
            String group = BungeeTabListPlus.getInstance().getPermissionManager().getMainGroup(connectedPlayer);

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
        }

        return false;
    }

    @Override
    public void update(ConfigSection section) {
        if (section.has("header") && section.get("header") instanceof String) {
            section.set("header", Lists.newArrayList(section.<String>get("header")));
        }

        if (section.has("footer") && section.get("footer") instanceof String) {
            section.set("footer", Lists.newArrayList(section.<String>get("footer")));
        }
    }
}
