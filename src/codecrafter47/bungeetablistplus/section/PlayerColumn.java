/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import codecrafter47.bungeetablistplus.sorting.AdminFirst;
import codecrafter47.bungeetablistplus.sorting.Alphabet;
import codecrafter47.bungeetablistplus.sorting.ISortingRule;
import codecrafter47.bungeetablistplus.sorting.YouFirst;
import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class PlayerColumn {

    Collection<String> filter;
    TabListConfig config;
    String prefix;
    String suffix;
    List<ProxiedPlayer> players;
    List<String> sort;
    int maxPlayers;

    public PlayerColumn(List<String> filter, TabListConfig config, String prefix, String suffix, List<String> sortrules, int maxPlayers) {
        this.filter = filter;
        this.config = config;
        this.prefix = prefix;
        this.suffix = suffix;
        this.sort = sortrules;
        this.maxPlayers = maxPlayers;
    }

    public void precalculate(ProxiedPlayer player) {
        this.players = BungeeTabListPlus.getInstance().getPlayerManager().getPlayers(filter, player);

        final List<ISortingRule> srules = new ArrayList<>();
        for (String rule : sort) {
            if (rule.equalsIgnoreCase("you") || rule.equalsIgnoreCase("youfirst")) {
                srules.add(new YouFirst(player));
            } else if (rule.equalsIgnoreCase("admin") || rule.equalsIgnoreCase("adminfirst")) {
                srules.add(new AdminFirst());
            } else if (rule.equalsIgnoreCase("alpha") || rule.equalsIgnoreCase("alphabet") || rule.equalsIgnoreCase("alphabetic") || rule.equalsIgnoreCase("alphabetical") || rule.equalsIgnoreCase("alphabetically")) {
                srules.add(new Alphabet());
            }
        }

        Collections.sort(players, new Comparator<ProxiedPlayer>() {

            @Override
            public int compare(ProxiedPlayer p1, ProxiedPlayer p2) {
                for (ISortingRule rule : srules) {
                    int i = rule.compare(p1, p2);
                    if (i != 0) {
                        return -i;
                    }
                }
                if (players.indexOf(p2) > players.indexOf(p1)) {
                    return -1;
                }
                return 1;
            }
        });
    }

    public int getMinSize(ProxiedPlayer player) {
        return 0;
    }

    public int getMaxSize(ProxiedPlayer player) {
        int m = players.size();
        if (m > maxPlayers) {
            m = maxPlayers;
        }
        return m * config.playerLines.size();
    }

    public void calculate(ProxiedPlayer player, TabList tabList, int collumn, int row, int size, int span) {
        int playersToShow = players.size();
        if(playersToShow > maxPlayers)playersToShow = maxPlayers;
        if (playersToShow * config.playerLines.size() > size) {
            playersToShow = (size - config.morePlayersLines.size()) / config.playerLines.size();
            if (playersToShow < 0) {
                playersToShow = 0;
            }
        }
        int other_count = players.size() - playersToShow;

        int p = row;
        int c = 0;
        for (int i = 0; i < playersToShow; i++) {
            for (String line : config.playerLines) {
                line = prefix + line + suffix;
                line = BungeeTabListPlus.getInstance().getVariablesManager().replacePlayerVariables(line, players.get(i));
                tabList.setSlot(p, collumn + c, new Slot(line, BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().sendPing ? players.get(i).getPing() : 0));
                c++;
                if (c >= span) {
                    c = 0;
                    p++;
                }
            }
        }

        if (other_count > 0) {
            for (String line : config.morePlayersLines) {
                line = prefix + line + suffix;
                line = line.replace("{other_count}", "" + other_count);
                tabList.setSlot(p, collumn + c, new Slot(line));
                c++;
                if (c >= span) {
                    c = 0;
                    p++;
                }
            }
        }
    }
}
