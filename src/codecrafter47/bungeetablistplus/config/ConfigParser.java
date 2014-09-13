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
package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.section.AutoFillPlayers;
import codecrafter47.bungeetablistplus.section.CollumnSplitSection;
import codecrafter47.bungeetablistplus.section.FillBukkitPlayers;
import codecrafter47.bungeetablistplus.section.FillPlayersSection;
import codecrafter47.bungeetablistplus.section.PlayerColumn;
import codecrafter47.bungeetablistplus.section.Section;
import codecrafter47.bungeetablistplus.section.ServerSection;
import codecrafter47.bungeetablistplus.section.StaticSection;
import codecrafter47.bungeetablistplus.tablist.Slot;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 *
 * @author Florian Stober
 */
public class ConfigParser {

    //private static final Pattern formatThings = Pattern.compile("\\[ALIGN .*\\]");
    private final BungeeTabListPlus plugin;

    public TabListConfig config;
    int level = 1;

    public ConfigParser(TabListConfig config, BungeeTabListPlus plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    public TabListProvider parse() throws ParseException {
        List<Section> topSections = new ArrayList<>();
        List<Section> botSections = new ArrayList<>();
        boolean bottom = false;
        for (String line : config.tabList) {
            // Its properties
            int ping = 0;
            int startColumn = -1;
            int collumn = -1;
            int maxplayers = 1000;
            String skin = null;
            List<String> sortrules = new ArrayList<>();

            // Parsing tags
            List<String> tags = parseTags(line);
            for (String tag : tags) {
                if (tag.equals("ALIGN BOTTOM")) {
                    bottom = true;
                } else if (tag.equals("ALIGN LEFT")) {
                    startColumn = 0;
                } else if (tag.equals("ALIGN RIGHT")) { // Check at least two cols
                    startColumn = ConfigManager.getCols() - 1;
                } else if (tag.startsWith("PING=")) {
                    ping = Integer.parseInt(tag.substring(5, tag.length()));
                } else if (tag.startsWith("COLUMN=")) {
                    collumn = Integer.parseInt(tag.substring(7, tag.length()));
                } else if (tag.startsWith("SORT=")) {
                    sortrules = Arrays.asList(tag.substring(5, tag.length()).
                            split(","));
                    validateSortrules(sortrules);
                } else if (tag.startsWith("MAXPLAYERS=")) {
                    maxplayers = Integer.parseInt(tag.
                            substring(11, tag.length()));
                } else if (tag.startsWith("SKIN=") && BungeeTabListPlus.
                        isVersion18()) {
                    skin = tag.substring(5, tag.length());
                } else {
                    plugin.getLogger().log(Level.WARNING,
                            "Unknown Tag \"[{0}]\" in {1}", new Object[]{tag,
                                config.getFileName()});
                }
            }

            if (startColumn == -1 && collumn != -1) {
                startColumn = collumn;
            }

            // Get current section list
            List<Section> sections;
            if (!bottom) {
                sections = topSections;
            } else {
                sections = botSections;
            }

            // Strip Tags
            String text = stripTags(line);

            // Parsing FillPlayers
            if (isFillPlayers(line)) {
                String prefix = text.substring(0, text.indexOf("{fillplayers"));
                String suffix = text.substring(text.
                        indexOf('}', prefix.length()), text.length() - 1);
                String args = text.charAt(prefix.length() + 12) == ':' ? text.
                        substring(prefix.length() + 13, text.length() - suffix.
                                length() - 1) : "";
                List<String> filter;
                if (args.length() > 0) {
                    filter = Arrays.asList(args.split(","));
                } else {
                    filter = new ArrayList<>();
                }
                if (collumn == -1) {
                    if (config.groupPlayers.equalsIgnoreCase("SERVER") && filter.
                            isEmpty()) {
                        sections.add(new AutoFillPlayers(startColumn, prefix,
                                suffix, sortrules, maxplayers));
                    } else {
                        sections.add(new FillPlayersSection(startColumn, filter,
                                config, prefix, suffix, sortrules, maxplayers));
                    }
                } else {
                    CollumnSplitSection cs;
                    if (sections.get(sections.size() - 1) instanceof CollumnSplitSection) {
                        cs = (CollumnSplitSection) sections.get(
                                sections.size() - 1);
                    } else {
                        cs = new CollumnSplitSection();
                        sections.add(cs);
                    }
                    cs.addCollumn(collumn, new PlayerColumn(filter, config,
                            prefix, suffix, sortrules, maxplayers));
                }
            } else if (isFillBukkitPlayers(line)) {
                sections.add(new FillBukkitPlayers(startColumn));
            } // Parsing Normal text
            else {
                StaticSection section;
                if (sections.size() > 0 && sections.get(sections.size() - 1) instanceof StaticSection && startColumn == -1) {
                    section = (StaticSection) sections.get(sections.size() - 1);
                } else {
                    section = new StaticSection(startColumn);
                    sections.add(section);
                }
                Slot slot = new Slot(text, ping);
                slot.setSkin(skin);
                section.add(slot);
            }
        }

        return new TabListProvider(topSections, botSections,
                config.showEmptyGroups, config, this);
    }

    public List<Section> parseServerSections(String g_prefix, String g_suffix,
            List<String> g_filter, String g_server, List<String> g_sort,
            int maxplayers) throws ParseException {
        List<Section> sections = new ArrayList<>();
        for (String line : config.groupLines) {
            // Its properties
            int ping = 0;
            int startColumn = -1;
            String skin = null;
            List<String> sortrules = new ArrayList<>();

            // Parsing tags
            List<String> tags = parseTags(line);
            for (String tag : tags) {
                if (tag.equals("ALIGN LEFT")) {
                    startColumn = 0;
                } else if (tag.equals("ALIGN RIGHT")) {
                    startColumn = ConfigManager.getCols() - 1;
                } else if (tag.startsWith("PING=")) {
                    ping = Integer.parseInt(tag.substring(5, tag.length()));
                } else if (tag.startsWith("SORT=")) {
                    sortrules = new ArrayList<>(Arrays.asList(tag.substring(5,
                            tag.length()).split(",")));
                } else if (tag.startsWith("COLUMN=")) {
                    startColumn = Integer.parseInt(tag.
                            substring(7, tag.length()));
                } else if (tag.startsWith("MAXPLAYERS=")) {
                    maxplayers = Integer.parseInt(tag.
                            substring(11, tag.length()));
                } else if (tag.startsWith("SKIN=") && BungeeTabListPlus.
                        isVersion18()) {
                    skin = tag.substring(5, tag.length());
                } else {
                    plugin.getLogger().log(Level.WARNING,
                            "Unknown Tag \"[{0}]\" in {1}", new Object[]{tag,
                                config.getFileName()});
                }
            }

            sortrules.addAll(g_sort);

            // Strip Tags
            String text = stripTags(line);
            // Parsing FillPlayers
            if (isFillPlayers(line)) {
                // TODO autogroup
                String prefix = g_prefix + text.substring(0, text.indexOf(
                        "{fillplayers"));
                String suffix = text.substring(text.
                        indexOf('}', prefix.length()), text.length() - 1) + g_suffix;
                String args = text.charAt(prefix.length() + 12) == ':' ? text.
                        substring(prefix.length() + 13, text.length() - suffix.
                                length() - 1) : "";
                List<String> filter = new ArrayList<>(Arrays.asList(args.split(
                        ",")));
                checkServer(filter);
                filter.addAll(g_filter);
                filter.add(g_server);
                sections.add(new FillPlayersSection(startColumn, filter, config,
                        prefix, suffix, sortrules, maxplayers));
            } // Parsing Normal text
            else {
                ServerSection section;
                if (sections.size() > 0 && sections.get(sections.size() - 1) instanceof ServerSection && startColumn == -1) {
                    section = (ServerSection) sections.get(sections.size() - 1);
                } else {
                    section = new ServerSection(startColumn, g_server);
                    sections.add(section);
                }
                Slot slot = new Slot(g_prefix + text + g_suffix, ping);
                slot.setSkin(skin);
                section.add(slot);
            }
        }
        return sections;
    }

    private List<String> parseTags(String line) throws ParseException {
        // TODO this can be optimized
        int i = 0;
        List<String> tags = new ArrayList<>();
        while (line.charAt(i) == '[') {
            int end = line.indexOf(']', i);
            if (end == -1) {
                throw new ParseException("Missing ']'", i);
            }
            tags.add(line.substring(i + 1, end));
            i = end + 1;
        }
        return tags;
    }

    private String stripTags(String line) throws ParseException {
        // TODO this can be optimized
        int i = 0;
        int end = -1;
        while (line.charAt(i) == '[') {
            end = line.indexOf(']', i);
            if (end == -1) {
                throw new ParseException("Missing ']'", i);
            }
            i = end + 1;
        }
        if (end + 1 >= line.length() - 1) {
            return "";
        }
        return line.substring(end + 1, line.length());
    }

    private boolean isFillPlayers(String s) {
        return s.matches("^.*\\{fillplayers(:.*)?\\}.*$");
    }

    private boolean isFillBukkitPlayers(String s) {
        return s.matches("^.*\\{fillbukkitplayers\\}.*$");
    }

    private void validateSortrules(List<String> sortrules) {
        for (String rule : sortrules) {
            if (!(rule.equalsIgnoreCase("you") || rule.equalsIgnoreCase(
                    "youfirst") || rule.equalsIgnoreCase("admin") || rule.
                    equalsIgnoreCase("adminfirst") || rule.equalsIgnoreCase(
                            "alpha") || rule.equalsIgnoreCase("alphabet") || rule.
                    equalsIgnoreCase("alphabetic") || rule.equalsIgnoreCase(
                            "alphabetical") || rule.equalsIgnoreCase(
                            "alphabetically"))) {
                plugin.getLogger().warning(
                        ChatColor.RED + "Can't sort players using rule '" + rule + "': Unknown rule");
            }
        }
    }

    private void checkServer(List<String> filter) {
        for (String s : filter) {
            if (plugin.getPlayerManager().isServer(s)) {
                plugin.getLogger().warning(
                        ChatColor.RED + "You shouldn't use {fillplayers:<server>} in groupLines");
            }
            if (s.equalsIgnoreCase("currentserver")) {
                plugin.getLogger().warning(
                        ChatColor.RED + "You shouldn't use {fillplayers:currentserver} in groupLines");
            }
        }
    }
}
