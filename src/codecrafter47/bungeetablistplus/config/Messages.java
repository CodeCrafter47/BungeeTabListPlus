package codecrafter47.bungeetablistplus.config;

import java.io.File;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;

public class Messages extends Config {

    public String errorNoPermission = "&cYou don't have permission to do that";
    public String errorNeedsPlayer = "&cThis command must be executed as Player";
    public String errorAlreadyHidden = "&cYou are already hidden";
    public String errorNotHidden = "&cCan't unhide, you're not hidden";
    public String successReloadComplete = "&aReloaded BungeeTabListPlus successfully";
    public String successPlayerHide = "&aYou have been hidden: Your name wont appear on the tablist";
    public String successPlayerUnhide = "&aYou're not hidden any longer";

    public Messages(Plugin plugin) throws InvalidConfigurationException {
        CONFIG_FILE = new File("plugins" + File.separator + plugin.
                getDescription().getName(), "messages.yml");
        CONFIG_HEADER = new String[]{"You can change messages here"};

        this.init();
    }
}
