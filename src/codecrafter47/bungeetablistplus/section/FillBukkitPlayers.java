/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabList;
import codecrafter47.bungeetablistplus.tablisthandler.CustomTabListHandler;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class FillBukkitPlayers extends Section {

    int startColumn;

    public FillBukkitPlayers(int startColumn) {
        this.startColumn = startColumn;
    }

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return 0;
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        return ((CustomTabListHandler) player.getTabList()).bukkitplayers.size();
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
            int size) {
        List<String> players = ((CustomTabListHandler) player.getTabList()).bukkitplayers;
        int p = pos;
        for (; p < pos + size; p++) {
            if (players.size() > p - pos) {
                tabList.setSlot(p, new Slot(players.get(p - pos)));
            }
        }
        return p;
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
        // Do nothing
    }

    @Override
    public int getStartCollumn() {
        return this.startColumn;
    }

}
