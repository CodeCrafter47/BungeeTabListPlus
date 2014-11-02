/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.api.TabList;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import codecrafter47.bungeetablistplus.sorting.AdminFirst;
import codecrafter47.bungeetablistplus.sorting.Alphabet;
import codecrafter47.bungeetablistplus.sorting.ISortingRule;
import codecrafter47.bungeetablistplus.sorting.YouFirst;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

/**
 *
 * @author Florian Stober
 */
public class FillPlayersSection extends Section {

    int vAlign;
    Collection<String> filter;
    TabListConfig config;
    String prefix;
    String suffix;
    String skin;
    List<ProxiedPlayer> players;
    List<String> sort;
    int maxPlayers;

    public FillPlayersSection(int vAlign, Collection<String> filter,
            TabListConfig config, String prefix, String suffix, String skin,
            List<String> sortrules, int maxPlayers) {
        this.vAlign = vAlign;
        this.filter = filter;
        this.config = config;
        this.prefix = prefix;
        this.suffix = suffix;
        this.skin = skin;
        this.sort = sortrules;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
        players = BungeeTabListPlus.getInstance().getPlayerManager().getPlayers(
                filter, player);

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

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return 0;
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        int m = players.size();
        if (m > this.maxPlayers) {
            m = this.maxPlayers;
        }
        return m * config.playerLines.size();
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
            int size) {
        //System.out.println(size + " / " + getMaxSize(player));
        int playersToShow = players.size();
        if (playersToShow > this.maxPlayers) {
            playersToShow = this.maxPlayers;
        }
        if (playersToShow * config.playerLines.size() > size) {
            playersToShow = (size - config.morePlayersLines.size()) / config.playerLines.
                    size();
            if (playersToShow < 0) {
                playersToShow = 0;
            }
        }
        int other_count = players.size() - playersToShow;

        for (int i = 0; i < playersToShow; i++) {
            pos = drawPlayerLines(players.get(i), tabList, pos);
        }

        if (other_count > 0) {
            pos = drawMorePlayers(other_count, tabList, pos);
        }
        return pos;
    }

    private int drawPlayerLines(ProxiedPlayer player, TabList tabList, int pos) {
        int i = pos;
        for (; i < pos + config.playerLines.size(); i++) {
            String line = prefix + config.playerLines.get(i - pos) + suffix;
            line = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(line, player);
            tabList.setSlot(i, new Slot(line, BungeeTabListPlus.getInstance().
                    getConfigManager().getMainConfig().sendPing ? player.
                            getPing() : 0));
            if(skin != null && !skin.isEmpty())
                tabList.getSlot(i).setSkin(skin);
            else if(config.showCorrectPlayerSkins)
                tabList.getSlot(i).setTextures(BungeeTabListPlus.getPlayerTexture(
                        player));
        }
        return i;
    }

    private int drawMorePlayers(int other_count, TabList tabList, int pos) {
        int i = pos;
        for (; i < pos + config.morePlayersLines.size(); i++) {
            String line = prefix + config.morePlayersLines.get(i - pos) + suffix;
            line = line.replace("{other_count}", "" + other_count);
            tabList.setSlot(i, new Slot(line, 0));
            if(skin != null && !skin.isEmpty())
                tabList.getSlot(i).setSkin(skin);
        }
        return i;
    }

    @Override
    public int getStartCollumn() {
        return vAlign;
    }

}
