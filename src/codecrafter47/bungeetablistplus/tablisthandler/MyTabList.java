package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.config.TabListProvider;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabList;
import codecrafter47.bungeetablistplus.util.ColorParser;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MyTabList extends MyCustom implements IMyTabListHandler {

    public MyTabList(ProxiedPlayer player) {
        super(player);

    }

    @Override
    public void recreate() {
        if (getPlayer().getServer() != null) {
            if (BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().excludeServers.contains(getPlayer().getServer().getInfo().getName()) || isExcluded) {
                unload();
                return;
            }
        }

        TabListProvider tlp = BungeeTabListPlus.getInstance().getTabListManager().getTabListForPlayer(super.getPlayer());
        if (tlp == null) {
            exclude();
            unload();
            return;
        }
        super.clear();
        TabList tabList = tlp.getTabList(super.getPlayer());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().charLimit;

        for (int i = 0; i < tabList.getUsedSlots(); i++) {
            Slot line = tabList.getSlot(i);
            if (line == null) {
                line = new Slot("");
            }
            line.text = BungeeTabListPlus.getInstance().getVariablesManager().replacePlayerVariables(line.text, super.getPlayer());
            line.text = BungeeTabListPlus.getInstance().getVariablesManager().replaceVariables(line.text);  
            line.text = ChatColor.translateAlternateColorCodes('&', line.text);
            if (charLimit > 0) {
                line.text = ColorParser.substringIgnoreColors(line.text, charLimit);
            }
            super.setSlot(i, line, false);
        }
        super.update();
    }
}
