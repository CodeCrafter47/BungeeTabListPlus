/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.tablist.TabList;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class CollumnSplitSection extends Section {

    PlayerColumn pc[] = new PlayerColumn[ConfigManager.getCols()];

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return 0;
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        int max = 0;
        for (int i = 0; i < pc.length; i++) {
            if (pc[i] != null) {
                int m = pc[i].getMaxSize(player);
                int span = 1;
                while (i + span != pc.length && pc[i + span] != null && (i + span < pc.length && pc[i + span - 1].filter.
                        equals(pc[i + span].filter))) {
                    span++;
                }
                m = (m + span - 1) / span;
                if (max < m) {
                    max = m;
                }
                i += span - 1;
            }
        }
        return max * ConfigManager.getCols();
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
            int size) {
        int sizePerCol = size / ConfigManager.getCols();
        for (int i = 0; i < pc.length; i++) {
            if (pc[i] != null) {
                int span = 1;
                while (i + span != pc.length && pc[i + span] != null && (i + span < pc.length && pc[i + span - 1].filter.
                        equals(pc[i + span].filter))) {
                    span++;
                }
                pc[i].calculate(player, tabList, i, pos / ConfigManager.
                        getCols(), sizePerCol * span, span);
                i += span - 1;
            }
        }
        return pos + sizePerCol * ConfigManager.getCols();
    }

    public void addCollumn(int i, PlayerColumn collumn) {
        pc[i] = collumn;
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
        for (int i = 0; i < pc.length; i++) {
            if (pc[i] != null) {
                pc[i].precalculate(player);
            }
        }
    }

    @Override
    public int getStartCollumn() {
        return 0;
    }

}
