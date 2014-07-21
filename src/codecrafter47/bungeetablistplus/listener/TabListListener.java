package codecrafter47.bungeetablistplus.listener;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.tablisthandler.CustomTabListHandler;
import codecrafter47.bungeetablistplus.tablisthandler.MyTabList;
import codecrafter47.bungeetablistplus.tablisthandler.ScoreboardTabList;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.tab.TabListHandler;
import net.md_5.bungee.event.EventHandler;

public class TabListListener implements Listener {

    private final BungeeTabListPlus plugin;

    public TabListListener(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) {
        TabListHandler tab;
        if (!plugin.getConfigManager().getMainConfig().useScoreboardToBypass16CharLimit) {
            tab = new MyTabList(e.getPlayer());
            if (plugin.getConfigManager().getMainConfig().updateOnPlayerJoinLeave) {
                plugin.resendTabLists();
            }
            plugin.sendImmediate(e.getPlayer());
        } else {
            tab = new ScoreboardTabList(e.getPlayer());
        }
        e.getPlayer().setTabList(tab);
    }

    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent e) {
        /////////////////////////////////
        plugin.sendImmediate(e.getPlayer());
        /////////////////////////////////
        if (plugin.getConfigManager().getMainConfig().updateOnServerChange) {
            plugin.resendTabLists();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent e) {
        if (plugin.getConfigManager().getMainConfig().updateOnPlayerJoinLeave) {
            plugin.resendTabLists();
        }
    }
}
