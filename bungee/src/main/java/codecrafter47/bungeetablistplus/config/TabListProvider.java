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
package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.ITabListProvider;
import codecrafter47.bungeetablistplus.section.AutoFillPlayers;
import codecrafter47.bungeetablistplus.section.Section;
import codecrafter47.bungeetablistplus.skin.Skin;
import codecrafter47.bungeetablistplus.tablist.FlippedTabList;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Stober
 */
public class TabListProvider implements ITabListProvider {

    private final BungeeTabListPlus plugin;
    private final List<Section> top;
    private final List<Section> bot;
    private final boolean showEmptyGroups;
    private final TabListConfig config;
    private final ConfigParser parser;

    public TabListProvider(BungeeTabListPlus plugin, List<Section> top, List<Section> bot,
                           boolean showEmpty, TabListConfig config, ConfigParser parser) {
        this.top = top;
        this.bot = bot;
        showEmptyGroups = showEmpty;
        this.config = config;
        this.parser = parser;
        this.plugin = plugin;
    }

    @Override
    public void fillTabList(final ProxiedPlayer player, ITabList tabList) {
        if (config.verticalMode) {
            tabList = new FlippedTabList(tabList);
        }

        List<Section> topSections = new ArrayList<>(top);
        List<Section> botSections = new ArrayList<>(bot);

        parseAutoFillplayers(player, topSections);
        parseAutoFillplayers(player, botSections);

        // precalculate all sections
        precalculateSections(player, topSections);
        precalculateSections(player, botSections);

        // Calculation maximum space the top sections need
        int topMax = 0;
        for (int i = 0; i < topSections.size(); i++) {
            Section s = topSections.get(i);
            topMax += s.getMaxSize(player);
            if (i + 1 < topSections.size()) {
                Section next = topSections.get(i + 1);
                int startColumn = next.getStartColumn();
                if (startColumn != -1) {
                    topMax += (tabList.getColumns() + startColumn - (topMax % tabList.getColumns())) % tabList.getColumns();
                }
            }
        }

        // Calculating minimum space the top sections need
        int[] tmin = new int[topSections.size()];
        int topMin = 0;
        for (int i = 0; i < topSections.size(); i++) {
            Section s = topSections.get(i);
            topMin += tmin[i] = s.getMinSize(player);
            if (i + 1 < topSections.size()) {
                Section next = topSections.get(i + 1);
                int startColumn = next.getStartColumn();
                if (startColumn != -1) {
                    tmin[i] += (tabList.getColumns() + startColumn - (topMin % tabList.getColumns())) % tabList.getColumns();
                    topMin += (tabList.getColumns() + startColumn - (topMin % tabList.getColumns())) % tabList.getColumns();
                }
            }
        }

        // calculating maximum space the bot sections need
        int botMax = 0;
        for (int i = botSections.size() - 1; i >= 0; i--) {
            Section s = botSections.get(i);
            botMax += s.getMaxSize(player);
            int startColumn = s.getStartColumn();
            if (startColumn != -1) {
                botMax += (startColumn - botMax % tabList.getColumns() + tabList.getColumns()) % tabList.getColumns();
            }
        }

        // calculating minimum space the bot sections need
        int[] bmin = new int[botSections.size()];
        int botMin = 0;
        for (int i = botSections.size() - 1; i >= 0; i--) {
            Section s = botSections.get(i);
            botMin += bmin[i] = s.getMinSize(player);
            int startColumn = s.getStartColumn();
            if (startColumn != -1) {
                bmin[i] += (startColumn - botMin % tabList.getColumns() + tabList.getColumns()) % tabList.getColumns();
                botMin += (startColumn - botMin % tabList.getColumns() + tabList.getColumns()) % tabList.getColumns();
            }
        }

        // calculating bot align
        int botAlign = 0;
        if (!botSections.isEmpty()) {
            int i = 0;
            int s = 0;
            do {
                botAlign = botSections.get(i).getStartColumn();
                if (botSections.get(i).getMaxSize(player) != botSections.get(i).
                        getMinSize(player) || botAlign != -1) {
                    break;
                }
                s += botSections.get(i).getMaxSize(player);
                i++;
                if (i == botSections.size() && botAlign == -1) {
                    botAlign = 0;
                }
            } while (botAlign == -1 && i < botSections.size());

            if (botAlign != -1) {
                botAlign -= s;
                while (botAlign < 0) {
                    botAlign += tabList.getColumns();
                }
            }
        }

        // determine how much space top and bottom sections get
        int topsize = topMin;
        int botsize = botMin;
        {
            int left = tabList.getSize() - topsize - botsize;

            if (left > 0) {
                int top_diff = topMax - topMin;
                int bot_diff = botMax - botMin;
                int avrg = left / 2;

                if (top_diff + bot_diff < left) {
                    topsize = topMax;
                    botsize = botMax;
                } else if (top_diff < avrg) {
                    topsize += top_diff;
                    botsize += left - top_diff;
                } else if (bot_diff < avrg) {
                    botsize += bot_diff;
                    topsize += left - bot_diff;
                } else {
                    botsize += avrg;
                    topsize += avrg;
                }
            }
        }

        if (topsize + botsize == tabList.getSize()) {
            topsize -= topsize % tabList.getColumns();
            topsize += botAlign;
            botsize = tabList.getSize() - topsize;
        }

        // calculating bot and top sections
        // TOP
        int len[] = new int[topSections.size()];
        List<Section> sections_left = new ArrayList<>(topSections);
        int space = topsize;//topSections.size();

        for (int i = 0; i < len.length; i++) {
            len[i] = -1;
        }

        Collections.sort(sections_left, new Comparator<Section>() {

            @Override
            public int compare(Section t, Section t1) {
                int minA = t.getMaxSize(player) - t.getMinSize(player);
                int minB = t1.getMaxSize(player) - t1.getMinSize(player);
                if (minA < minB) {
                    return -1;
                }
                if (minB < minA) {
                    return 1;
                }
                if (t.id < t1.id) {
                    return 1;
                }
                return -1;
            }
        });

        while (sections_left.size() > 0) {
            int average_diff = (space - topMin) / sections_left.size();

            Section found = sections_left.get(0);
            int min_diff = found.getMaxSize(player) - found.getMinSize(player);

            int size = found.getMinSize(player) + ((min_diff <= average_diff)
                    ? min_diff
                    : average_diff);

            if (size > found.getMaxSize(player)) {
                size = found.getMaxSize(player);
            }

            int index = topSections.indexOf(found);

            len[index] = size;
            space -= size;

            int indexStart = index;
            while (topSections.get(indexStart).getStartColumn() == -1 && indexStart != 0) {
                indexStart--;
            }
            int startAlign;
            if (indexStart == 0) {
                startAlign = 0;
            } else {
                startAlign = topSections.get(indexStart).getStartColumn();
            }

            int indexNext = index + 1;
            while (indexNext < topSections.size() && topSections.get(indexNext).
                    getStartColumn() == -1) {
                indexNext++;
            }

            int nextAlign;
            if (topSections.size() > indexNext) {
                nextAlign = topSections.get(indexNext).getStartColumn();
            } else {
                nextAlign = topsize % tabList.getColumns();
            }
            boolean areAv = true;
            for (int i = indexStart; i < indexNext; i++) {
                if (len[i] == -1) {
                    areAv = false;
                }
            }

            if (areAv) {
                int s = startAlign;
                for (int i = indexStart; i < indexNext; i++) {
                    s += len[i];
                }
                int diff = (tabList.getColumns() + nextAlign - (s % tabList.getColumns())) % tabList.getColumns();
                // diff -= startAlign;
                //diff = (diff + ConfigManager.getCols()) % ConfigManager.getCols();
                len[indexNext - 1] += diff;
                space -= diff;
            }

            topMin -= tmin[index];

            sections_left.remove(found);
        }

        int pos = 0;
        int i = 0;
        for (Section s : topSections) {
            int startColumn = s.getStartColumn();
            if (startColumn != -1) {
                pos += (tabList.getColumns() + startColumn - (pos % tabList.getColumns())) % tabList.getColumns();
            }
            pos = s.calculate(player, tabList, pos, len[i++]);
        }

        // BOT
        len = new int[botSections.size()];
        sections_left = new ArrayList<>();
        space = botsize;

        for (i = 0; i < len.length; i++) {
            len[i] = -1;
        }

        for (Section section : botSections) {
            sections_left.add(section);
        }

        Collections.sort(sections_left, new Comparator<Section>() {

            @Override
            public int compare(Section t, Section t1) {
                int minA = t.getMaxSize(player) - t.getMinSize(player);
                int minB = t1.getMaxSize(player) - t1.getMinSize(player);
                if (minA < minB) {
                    return -1;
                }
                if (minB < minA) {
                    return 1;
                }
                if (t.id < t1.id) {
                    return 1;
                }
                return -1;
            }
        });

        while (sections_left.size() > 0) {
            int average_diff = (space - botMin) / sections_left.size();
            Section found = sections_left.get(0);
            int min_diff = found.getMaxSize(player) - found.getMinSize(player);

            int size = found.getMinSize(player) + ((min_diff <= average_diff)
                    ? min_diff
                    : average_diff);

            if (size > found.getMaxSize(player)) {
                size = found.getMaxSize(player);
            }

            int index = botSections.indexOf(found);

            len[index] = size;
            space -= size;

            int indexStart = index;
            while (botSections.get(indexStart).getStartColumn() == -1 && indexStart != 0) {
                indexStart--;
            }
            int startAlign;
            if (indexStart == 0) {
                startAlign = botAlign;
            } else {
                startAlign = botSections.get(indexStart).getStartColumn();
            }

            int indexNext = index + 1;
            while (indexNext < botSections.size() && botSections.get(indexNext).
                    getStartColumn() == -1) {
                indexNext++;
            }

            int nextAlign;
            if (botSections.size() > indexNext) {
                nextAlign = botSections.get(indexNext).getStartColumn();
            } else {
                nextAlign = 0;
            }
            boolean areAv = true;
            for (i = indexStart; i < indexNext; i++) {
                if (len[i] == -1) {
                    areAv = false;
                }
            }

            if (areAv) {
                int s = startAlign;
                for (i = indexStart; i < indexNext; i++) {
                    s += len[i];
                }
                int diff = (tabList.getColumns() + nextAlign - (s % tabList.getColumns())) % tabList.getColumns();
                len[indexNext - 1] += diff;
                space -= diff;
            }

            botMin -= bmin[index];

            sections_left.remove(found);
        }

        pos = tabList.getSize() - botsize;
        i = 0;
        for (Section s : botSections) {
            int startColumn = s.getStartColumn();
            if (startColumn != -1) {
                pos += (tabList.getColumns() + startColumn - (pos % tabList.getColumns())) % tabList.getColumns();
            }
            pos = s.calculate(player, tabList, pos, len[i++]);
        }

        // header + footer
        if (this.config.shownFooterHeader) {
            String header = config.header;
            header = plugin.getVariablesManager().
                    replacePlayerVariables(player, header, plugin.getBungeePlayerProvider().wrapPlayer(player));
            header = plugin.getVariablesManager().
                    replaceVariables(player, header);
            header = ChatColor.translateAlternateColorCodes('&', header);
            header = header.replaceAll("\\{newline\\}", "\n");
            tabList.setHeader(header);
            String footer = config.footer;
            footer = plugin.getVariablesManager().
                    replacePlayerVariables(player, footer, plugin.getBungeePlayerProvider().wrapPlayer(player));
            footer = plugin.getVariablesManager().
                    replaceVariables(player, footer);
            footer = ChatColor.translateAlternateColorCodes('&', footer);
            footer = footer.replaceAll("\\{newline\\}", "\n");
            tabList.setFooter(footer);
        }

        if (BungeeTabListPlus.isVersion18()) {
            tabList.setDefaultSkin(plugin.
                    getSkinManager().getSkin(config.defaultSkin));
        }

        tabList.setDefaultPing(config.defaultPing);
    }

    private void precalculateSections(ProxiedPlayer player, List<Section> topSections) {
        for (Section section : topSections) {
            section.precalculate(player);
        }
    }

    private void parseAutoFillplayers(final ProxiedPlayer player, List<Section> sectionList) {
        for (int i = 0; i < sectionList.size(); i++) {
            Section section = sectionList.get(i);
            if (section instanceof AutoFillPlayers) {
                sectionList.remove(i);
                String prefix = ((AutoFillPlayers) section).prefix;
                String suffix = ((AutoFillPlayers) section).suffix;
                int maxPlayers = ((AutoFillPlayers) section).maxPlayers;
                List<String> sortRules = ((AutoFillPlayers) section).sortRules;
                Skin skin = ((AutoFillPlayers) section).skin;

                Map<String, ServerInfo> servers = ProxyServer.getInstance().
                        getServers();

                Set<String> serverSet = new HashSet<>(servers.keySet());
                HashMultimap<String, String> aliasToServerMap = HashMultimap.create();
                for (Map.Entry<String, String> entry : BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().serverAlias.entrySet()) {
                    if (ProxyServer.getInstance().getServerInfo(entry.getKey()) == null) {
                        BungeeTabListPlus.getInstance().getLogger().warning("Configuration Error: Server \"" + entry.getKey() + "\" used in the alias map does not exist.");
                        continue;
                    }
                    aliasToServerMap.put(entry.getValue(), entry.getKey());
                }
                List<String> list = new ArrayList<>();
                while (!serverSet.isEmpty()) {
                    String server = serverSet.iterator().next();
                    String alias = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().serverAlias.get(server);
                    if (alias != null) {
                        Set<String> strings = aliasToServerMap.get(alias);
                        serverSet.removeAll(strings);
                        list.add(Joiner.on(',').join(strings));
                    } else {
                        serverSet.remove(server);
                        list.add(server);
                    }
                }

                Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        int p1 = plugin.
                                getPlayerManager().getServerPlayerCount(s1, player, plugin.getConfigManager().getMainConfig().showPlayersInGamemode3);
                        int p2 = plugin.
                                getPlayerManager().getServerPlayerCount(s2, player, plugin.getConfigManager().getMainConfig().showPlayersInGamemode3);
                        if (p1 < p2) {
                            return 1;
                        }
                        if (p1 > p2) {
                            return -1;
                        }
                        return s1.compareTo(s2);
                    }
                });

                int j = i;
                for (String server : list) {
                    if (showEmptyGroups || plugin.getPlayerManager().
                            getPlayerCount(server, player, plugin.getConfigManager().getMainConfig().showPlayersInGamemode3) > 0) {
                        try {
                            List<Section> sections = parser.
                                    parseServerSections(
                                            prefix, suffix, skin, new ArrayList<String>(0),
                                            server,
                                            sortRules, maxPlayers);
                            for (Section s : sections) {
                                sectionList.add(j++, s);
                            }
                        } catch (ParseException ex) {
                            Logger.
                                    getLogger(TabListProvider.class.
                                            getName()).
                                    log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    public boolean appliesTo(ProxiedPlayer player) {
        return config.appliesTo(player);
    }
}
