package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class TabListConfig extends Config {

    @Comments({
        "Defines to which players this tabList applies",
        "No effect in the default tabList", "possible values:",
        "'Player:<Name>' for spezific Player",
        "'Players:<player1>,<player2>' for multiple Players",
        "'Server:<Server>' for all Players on that Server",
        "'Servers:<server1>,<server2>' for all Players which are on These Servers",
        "'group:<group>' for all players within that permission group",
        "groups:<group1>,<group2> same with multiple groups",
        "'all' for all players"
    })
    public String showTo = "all";
    
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
        "more information at http://www.spigotmc.org/resources/bungeetablistplus.313/"
    })
    public List<String> tabList = new ArrayList<>();

    {
        groupLines.add("[ALIGN LEFT]&c>>> {server}({server_player_count}):");
        groupLines.add(" ");
        groupLines.add(" ");
        groupLines.add("{fillplayers}");
    }

    {
        playerLines.add("{permprefix}{player}{permsuffix}");
    }

    {
        morePlayersLines.add("... and {other_count} others");
    }

    {
        tabList.add("&a>>>>>>>>>>>>");
        tabList.add("&aWelcome");
        tabList.add("&a<<<<<<<<<<<<");
        tabList.add("&a>>>>>>>>>>>>");
        tabList.add("&a{player}");
        tabList.add("&a<<<<<<<<<<<<");
        tabList.add(" ");
        tabList.add(" ");
        tabList.add(" ");
        tabList.add("{fillplayers}");
        tabList.add("[ALIGN BOTTOM]&3------------");
        tabList.add("&3------------");
        tabList.add("&3------------");
        tabList.add("&3and some");
        tabList.add("&3other text");
        tabList.add("&3here");
    }

    public TabListConfig(Plugin plugin, String filename) throws InvalidConfigurationException {
        CONFIG_FILE = new File(plugin.getDataFolder() + File.separator + "tabLists", filename);
        CONFIG_HEADER = new String[]{
            "", "This the default TabList", "It is shown to all users which haven't got another TabList", "",
            "Create a copy of this File, name it like you wish", "and change the showTo and some other options",
            "to create another TabList only shown to some users.", "By doing this you can for example create",
            "a special TabList for VIPs / Admins or", "create a tabList only shown o one server", ""
        };

        this.init();
    }

    public boolean appliesTo(ProxiedPlayer player) {
        if (showTo.equalsIgnoreCase("ALL")) {
            return true;
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

        String group = BungeeTabListPlus.getInstance().getPermissionManager().getMainGroup(player);

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
