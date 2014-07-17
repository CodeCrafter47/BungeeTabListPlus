package codecrafter47.bungeetablistplus.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;

public class MainConfig extends Config {

    @Comments({
        "time in seconds after which the tabList will be resend to all players",
        "set this to -1 to disable scheduled update of the tabList"
    })
    public double tablistUpdateIntervall = 5;

    @Comments({
        "whether tabList should be resend if a player joins or leaves the server"
    })
    public boolean updateOnPlayerJoinLeave = true;

    @Comments({
        "whether tablist should be resend if a player switches the server"
    })
    public boolean updateOnServerChange = true;

    @Comments({
        "whether to use scoreboard functions to bypass the 16 character limit",
        "does NOT conflict if other scoreboard plugins"
    })
    public boolean useScoreboardToBypass16CharLimit = true;

    @Comments({
        "You can limit the number of characters per slot here",
        "Color codes do not count as a character; -1 means unlimited"
    })
    public int charLimit = -1;

    @Comments({
        "Decide from where BungeeTabListPlus takes informations like permissions,",
        "prefix, suffix and group.",
        "Possible values:",
        "AUTO        - take best source",
        "BUKKIT      - take informations from bukkit/vault",
        "BUNGEEPERMS - take informations from bungeeperms",
        "BUNGEE      - take group from bungee, prefix from config.yml, permissions from bungee"
    })
    public String permissionSource = "AUTO";

    @Comments({
        "whether ping is sent to clients",
        "setting this to false can help you reducing network traffic"
    })
    public boolean sendPing = true;

    @Comments({
        "if enabled the plugin checks for new versions automatically.",
        "Use /BTLP to see whether a new version is available",
        "this does NOT automatically install an update"
    })
    public boolean checkForUpdates = true;

    @Comments({
        "server Alias fo the {server} Variable"
    })
    public HashMap<String, String> serverAlias = new HashMap<>();
    {
        serverAlias.put("server1", "Spawn");
        serverAlias.put("server2", "Creative");
        serverAlias.put("server3", "PvP");
    }

    @Comments({
        "the prefixes used for the {prefix} variable, based upon bungeecord permission groups",
        "this will only be used if bungeeperms is not installed"
    })
    public HashMap<String, String> prefixes = new HashMap<>();
    {
        prefixes.put("default", "");
        prefixes.put("admin", "&c[A] ");
    }

    @Comments({
        "servers which you wish to show their own tabList (The one provided by bukkit)"
    })
    public List<String> excludeServers = new ArrayList<>();

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
        CONFIG_FILE = new File("plugins" + File.separator + plugin.getDescription().getName(), "config.yml");
        CONFIG_HEADER = new String[]{"This is the Config File of BungeeTabListPlus"};

        this.init();
    }

    public String getServerAlias(String name) {
        if (serverAlias.get(name) != null) {
            return serverAlias.get(name);
        }
        return name;
    }
}
