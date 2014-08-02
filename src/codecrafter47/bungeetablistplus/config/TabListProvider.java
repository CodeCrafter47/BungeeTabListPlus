/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.section.AutoFillPlayers;
import codecrafter47.bungeetablistplus.section.Section;
import codecrafter47.bungeetablistplus.section.ServerSection;
import codecrafter47.bungeetablistplus.tablist.TabList;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class TabListProvider {

    List<Section> top;
    List<Section> bot;
    boolean showEmptyGroups;
    TabListConfig config;
    ConfigParser parser;

    public TabListProvider(List<Section> top, List<Section> bot,
            boolean showEmpty, TabListConfig config, ConfigParser parser) {
        this.top = top;
        this.bot = bot;
        showEmptyGroups = showEmpty;
        this.config = config;
        this.parser = parser;
    }

    public TabList getTabList(final ProxiedPlayer player) {

        List<Section> topSections = new ArrayList<>(top);
        List<Section> botSections = new ArrayList<>(bot);

        for (int n = 0; n < 2; n++) {
            List<Section> sectionList;
            if (n == 0) {
                sectionList = topSections;
            } else {
                sectionList = botSections;
            }

            for (int i = 0; i < sectionList.size(); i++) {
                Section section = sectionList.get(i);
                if (section instanceof AutoFillPlayers) {
                    sectionList.remove(i);
                    String prefix = ((AutoFillPlayers) section).prefix;
                    String suffix = ((AutoFillPlayers) section).suffix;
                    int startColumn = ((AutoFillPlayers) section).startColumn;
                    int maxPlayers = ((AutoFillPlayers) section).maxPlayers;
                    List<String> sortRules = ((AutoFillPlayers) section).sortRules;

                    Map<String, ServerInfo> servers = ProxyServer.getInstance().
                            getServers();

                    List<String> list = new LinkedList(servers.keySet());
                    Collections.sort(list, new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            int p1 = BungeeTabListPlus.getInstance().
                                    getPlayerManager().getServerPlayerCount(s1);
                            int p2 = BungeeTabListPlus.getInstance().
                                    getPlayerManager().getServerPlayerCount(s2);
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
                        if (config.showEmptyGroups || BungeeTabListPlus.
                                getInstance().getPlayerManager().
                                getServerPlayerCount(server) > 0) {
                            try {
                                List<Section> sections = parser.
                                        parseServerSections(
                                                prefix, suffix, new ArrayList(0),
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

        // precalculate all sections
        for (Section section : topSections) {
            section.precalculate(player);
        }
        for (Section section : botSections) {
            section.precalculate(player);
        }

        // Calculation maximum space the top sections need
        int topMax = 0;
        for (int i = 0; i < topSections.size(); i++) {
            Section s = topSections.get(i);
            topMax += s.getMaxSize(player);
            if (i + 1 < topSections.size()) {
                Section next = topSections.get(i + 1);
                int startColumn = next.getStartCollumn();
                if (startColumn != -1) {
                    topMax += (ConfigManager.getCols() + startColumn - (topMax % ConfigManager.
                            getCols())) % ConfigManager.getCols();
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
                int startColumn = next.getStartCollumn();
                if (startColumn != -1) {
                    tmin[i] += (ConfigManager.getCols() + startColumn - (topMin % ConfigManager.
                            getCols())) % ConfigManager.getCols();
                    topMin += (ConfigManager.getCols() + startColumn - (topMin % ConfigManager.
                            getCols())) % ConfigManager.getCols();
                }
            }
        }

        // calculating maximum space the bot sections need
        int botMax = 0;
        for (int i = botSections.size() - 1; i >= 0; i--) {
            Section s = botSections.get(i);
            botMax += s.getMaxSize(player);
            int startColumn = s.getStartCollumn();
            if (startColumn != -1) {
                botMax += (startColumn - botMax % ConfigManager.getCols() + ConfigManager.
                        getCols()) % ConfigManager.getCols();
            }
        }

        // calculating minimum space the bot sections need
        int[] bmin = new int[botSections.size()];
        int botMin = 0;
        for (int i = botSections.size() - 1; i >= 0; i--) {
            Section s = botSections.get(i);
            botMin += bmin[i] = s.getMinSize(player);
            int startColumn = s.getStartCollumn();
            if (startColumn != -1) {
                bmin[i] += (startColumn - botMin % ConfigManager.getCols() + ConfigManager.
                        getCols()) % ConfigManager.getCols();
                botMin += (startColumn - botMin % ConfigManager.getCols() + ConfigManager.
                        getCols()) % ConfigManager.getCols();
            }
        }

        // calculating bot align
        int botAlign = 0;
        if (!botSections.isEmpty()) {
            int i = 0;
            int s = 0;
            do {
                botAlign = botSections.get(i).getStartCollumn();
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
                    botAlign += ConfigManager.getCols();
                }
            }
        }

        // determine how much space top and bottom sections get
        int topsize = topMin;
        int botsize = botMin;
        {
            int left = ConfigManager.getTabSize() - topsize - botsize;

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

        if (topsize + botsize == ConfigManager.getTabSize()) {
            topsize -= topsize % ConfigManager.getCols();
            topsize += botAlign;
            botsize = ConfigManager.getTabSize() - topsize;
        }

        // calculating bot and top sections
        TabList tabList = new TabList(ConfigManager.getRows(), ConfigManager.
                getCols());
        // TOP
        // grob vorberechnen
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
            while (topSections.get(indexStart).getStartCollumn() == -1 && indexStart != 0) {
                indexStart--;
            }
            int startAlign;
            if (indexStart == 0) {
                startAlign = 0;
            } else {
                startAlign = topSections.get(indexStart).getStartCollumn();
            }

            int indexNext = index + 1;
            while (indexNext < topSections.size() && topSections.get(indexNext).
                    getStartCollumn() == -1) {
                indexNext++;
            }

            int nextAlign;
            if (topSections.size() > indexNext) {
                nextAlign = topSections.get(indexNext).getStartCollumn();
            } else {
                nextAlign = topsize % ConfigManager.getCols();
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
                int diff = (ConfigManager.getCols() + nextAlign - (s % ConfigManager.
                        getCols())) % ConfigManager.getCols();
                // diff -= startAlign;
                //diff = (diff + ConfigManager.getCols()) % ConfigManager.getCols();
                len[indexNext - 1] += diff;
                space -= diff;
            }

            topMin -= tmin[index];

            sections_left.remove(found);
        }

        // jetzt richtig berechnen
        int pos = 0;
        int i = 0;
        for (Section s : topSections) {
            int startColumn = s.getStartCollumn();
            if (startColumn != -1) {
                pos += (ConfigManager.getCols() + startColumn - (pos % ConfigManager.
                        getCols())) % ConfigManager.getCols();
            }
            pos = s.calculate(player, tabList, pos, len[i++]);
        }

        // BOT
        // grob vorberechnen
        len = new int[botSections.size()];
        sections_left = new ArrayList<>();
        space = botsize;//botSections.size();

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
            while (botSections.get(indexStart).getStartCollumn() == -1 && indexStart != 0) {
                indexStart--;
            }
            int startAlign;
            if (indexStart == 0) {
                startAlign = botAlign;
            } else {
                startAlign = botSections.get(indexStart).getStartCollumn();
            }

            int indexNext = index + 1;
            while (indexNext < botSections.size() && botSections.get(indexNext).
                    getStartCollumn() == -1) {
                indexNext++;
            }

            int nextAlign;
            if (botSections.size() > indexNext) {
                nextAlign = botSections.get(indexNext).getStartCollumn();
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
                int diff = (ConfigManager.getCols() + nextAlign - (s % ConfigManager.
                        getCols())) % ConfigManager.getCols();
                len[indexNext - 1] += diff;
                space -= diff;
            }

            botMin -= bmin[index];

            sections_left.remove(found);
        }

        // jetzt richtig berechnen
        pos = ConfigManager.getTabSize() - botsize;
        i = 0;
        for (Section s : botSections) {
            int startColumn = s.getStartCollumn();
            if (startColumn != -1) {
                pos += (ConfigManager.getCols() + startColumn - (pos % ConfigManager.
                        getCols())) % ConfigManager.getCols();
            }
            pos = s.calculate(player, tabList, pos, len[i++]);
        }

        return tabList;
    }

    public boolean appliesTo(ProxiedPlayer player) {
        return config.appliesTo(player);
    }
}
