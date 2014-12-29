/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.api.TabList;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.player.IPlayer;
import codecrafter47.bungeetablistplus.skin.Skin;
import codecrafter47.bungeetablistplus.sorting.AdminFirst;
import codecrafter47.bungeetablistplus.sorting.Alphabet;
import codecrafter47.bungeetablistplus.sorting.ISortingRule;
import codecrafter47.bungeetablistplus.sorting.YouFirst;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

/**
 * @author Florian Stober
 */
public class PlayerColumn {

    final Collection<String> filter;
    private final TabListConfig config;
    private final String prefix;
    private final String suffix;
    private final Skin skin;
    private List<IPlayer> players;
    private final List<String> sort;
    private final int maxPlayers;

    public PlayerColumn(List<String> filter, TabListConfig config, String prefix,
                        String suffix, Skin skin, List<String> sortrules, int maxPlayers) {
        this.filter = filter;
        this.config = config;
        this.prefix = prefix;
        this.suffix = suffix;
        this.sort = sortrules;
        this.skin = skin;
        this.maxPlayers = maxPlayers;
    }

    public void precalculate(ProxiedPlayer player) {
        this.players = BungeeTabListPlus.getInstance().getPlayerManager().
                getPlayers(filter, player, BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3);

        final List<ISortingRule> srules = new ArrayList<>();
        for (String rule : sort) {
            if (rule.equalsIgnoreCase("you") || rule.
                    equalsIgnoreCase("youfirst")) {
                srules.add(new YouFirst(player));
            } else if (rule.equalsIgnoreCase("admin") || rule.equalsIgnoreCase(
                    "adminfirst")) {
                srules.add(new AdminFirst());
            } else if (rule.equalsIgnoreCase("alpha") || rule.equalsIgnoreCase(
                    "alphabet") || rule.equalsIgnoreCase("alphabetic") || rule.
                    equalsIgnoreCase("alphabetical") || rule.equalsIgnoreCase(
                    "alphabetically")) {
                srules.add(new Alphabet());
            }
        }

        Collections.sort(players, new Comparator<IPlayer>() {

            @Override
            public int compare(IPlayer p1, IPlayer p2) {
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

    public int getMaxSize(ProxiedPlayer player) {
        int m = players.size();
        if (m > maxPlayers) {
            m = maxPlayers;
        }
        return m * config.playerLines.size();
    }

    public void calculate(ProxiedPlayer player, TabList tabList, int collumn,
                          int row, int size, int span) {
        int playersToShow = players.size();
        if (playersToShow > maxPlayers) {
            playersToShow = maxPlayers;
        }
        if (playersToShow * config.playerLines.size() > size) {
            playersToShow = (size - config.morePlayersLines.size()) / config.playerLines.
                    size();
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
                line = BungeeTabListPlus.getInstance().getVariablesManager().
                        replacePlayerVariables(player, line, players.get(i));
                tabList.setSlot(p, collumn + c, new Slot(line,
                        BungeeTabListPlus.getInstance().getConfigManager().
                                getMainConfig().sendPing ? players.get(i).getPing() : 0));
                if (skin != SkinManager.defaultSkin)
                    tabList.getSlot(p, collumn + c).setSkin(skin);
                else if (config.showCorrectPlayerSkins)
                    tabList.getSlot(p, collumn + c).setSkin(players.get(i).getSkin());
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
                tabList.setSlot(p, collumn + c, new Slot(line, config.defaultPing));
                if (skin != SkinManager.defaultSkin)
                    tabList.getSlot(p, collumn + c).setSkin(skin);
                c++;
                if (c >= span) {
                    c = 0;
                    p++;
                }
            }
        }
    }
}
