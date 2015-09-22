/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
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
import codecrafter47.bungeetablistplus.section.*;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigParser {

    private final BungeeTabListPlus plugin;

    private static final Pattern PATTERN_ALIGN_BOTTOM = Pattern.compile("\\[ALIGN BOTTOM\\]");
    private static final Pattern PATTERN_ALIGN_LEFT = Pattern.compile("\\[ALIGN LEFT\\]");
    private static final Pattern PATTERN_ALIGN_RIGHT = Pattern.compile("\\[ALIGN RIGHT\\]");
    private static final Pattern PATTERN_COLUMN = Pattern.compile("\\[COLUMN=(\\d+)\\]");
    private static final Pattern PATTERN_ROW = Pattern.compile("\\[ROW=(\\d+)\\]");
    private static final Pattern PATTERN_MAXPLAYERS = Pattern.compile("\\[MAXPLAYERS=(\\d+)\\]");
    private static final Pattern PATTERN_SORT = Pattern.compile("\\[SORT=([^]]+)\\]");

    private static final Pattern PATTERN_FILLPLAYERS = Pattern.compile("^(?<prefix>.*)\\{fillplayers(?::(?<filter>.*))?\\}(?<suffix>.*)$");
    private static final Pattern PATTERN_FILLBUKKITPLAYERS = Pattern.compile("^(?<prefix>.*)\\{fillbukkitplayers\\}(?<suffix>.*)$");

    public ConfigParser(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public TabListProvider parse(TabListConfig config) {
        List<SlotTemplate> playerLines = config.playerLines.stream().map(this::parseSlot).collect(Collectors.toList());
        List<SlotTemplate> morePlayerLines = config.morePlayersLines.stream().map(this::parseSlot).collect(Collectors.toList());


        List<Section> topSections = new ArrayList<>();
        List<Section> botSections = new ArrayList<>();
        final boolean[] bottom = {false};
        for (String line : config.tabList) {
            // Its properties
            final int[] startColumn = {-1};
            final int[] column = {-1};
            final int[] maxplayers = {1000};
            final List<String> sortrules = new ArrayList<>();

            // Parsing tags
            line = findTag(line, PATTERN_ALIGN_BOTTOM, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ALIGN BOTTOM] in verticalMode");
                } else {
                    bottom[0] = true;
                }
            });

            line = findTag(line, PATTERN_ALIGN_LEFT, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ALIGN LEFT] in verticalMode");
                } else {
                    startColumn[0] = 0;
                }
            });

            line = findTag(line, PATTERN_ALIGN_RIGHT, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ALIGN RIGHT] in verticalMode");
                } else {
                    startColumn[0] = ConfigManager.getCols() - 1;
                }
            });

            line = findTag(line, PATTERN_COLUMN, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [COLUMN=?] in verticalMode");
                } else {
                    column[0] = Integer.parseInt(matcher.group(1));
                }
            });

            line = findTag(line, PATTERN_ROW, matcher -> {
                if (!config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ROW=?] in horizontalMode");
                } else {
                    column[0] = Integer.parseInt(matcher.group(1));
                }
            });

            line = findTag(line, PATTERN_SORT, matcher -> {
                sortrules.addAll(Arrays.asList(matcher.group(1).split(",")));
                validateSortrules(sortrules);
            });

            line = findTag(line, PATTERN_MAXPLAYERS, matcher -> {
                maxplayers[0] = Integer.parseInt(matcher.group(1));
            });

            if (startColumn[0] == -1 && column[0] != -1) {
                startColumn[0] = column[0];
            }

            // Get current section list
            List<Section> sections;
            if (!bottom[0]) {
                sections = topSections;
            } else {
                sections = botSections;
            }

            Matcher fillplayersMatcher = PATTERN_FILLPLAYERS.matcher(line);
            Matcher fillbukkitplayersMatcher = PATTERN_FILLBUKKITPLAYERS.matcher(line);
            if (fillplayersMatcher.matches()) {
                SlotTemplate prefix = parseSlot(fillplayersMatcher.group("prefix"));
                SlotTemplate suffix = parseSlot(fillplayersMatcher.group("suffix"));
                String args = fillplayersMatcher.group("filter");
                List<String> filter;
                if (args == null || args.isEmpty()) {
                    filter = new ArrayList<>();
                } else {
                    filter = Arrays.asList(args.split(","));
                }
                if (column[0] == -1) {
                    if (config.groupPlayers.equalsIgnoreCase("SERVER") && filter.isEmpty()) {
                        sections.add(new AutoFillPlayers(startColumn[0], prefix, suffix, sortrules, maxplayers[0], playerLines, morePlayerLines));
                    } else {
                        sections.add(new FillPlayersSection(startColumn[0], filter, prefix, suffix, sortrules, maxplayers[0], playerLines, morePlayerLines));
                    }
                } else {
                    ColumnSplitSection cs;
                    if (!sections.isEmpty() && sections.get(sections.size() - 1) instanceof ColumnSplitSection) {
                        cs = (ColumnSplitSection) sections.get(sections.size() - 1);
                    } else {
                        cs = new ColumnSplitSection();
                        sections.add(cs);
                    }
                    cs.addCollumn(column[0], new PlayerColumn(filter, prefix, suffix, sortrules, maxplayers[0], playerLines, morePlayerLines));
                }
            } else if (fillbukkitplayersMatcher.matches()) {
                SlotTemplate prefix = parseSlot(fillbukkitplayersMatcher.group("prefix"));
                SlotTemplate suffix = parseSlot(fillbukkitplayersMatcher.group("suffix"));
                sections.add(new FillBukkitPlayers(startColumn[0], prefix, suffix, sortrules, maxplayers[0], playerLines, morePlayerLines));
            } else {
                SlotTemplate template = parseSlot(line);
                StaticSection section;
                if (sections.size() > 0 && sections.get(sections.size() - 1) instanceof StaticSection && startColumn[0] == -1) {
                    section = (StaticSection) sections.get(sections.size() - 1);
                } else {
                    section = new StaticSection(startColumn[0]);
                    sections.add(section);
                }
                section.add(template);
            }
        }

        return new TabListProvider(plugin, topSections, botSections, config.showEmptyGroups, config, this, config.shownFooterHeader, parseSlot(config.header), parseSlot(config.footer));
    }

    public List<Section> parseServerSections(TabListConfig config, SlotTemplate g_prefix, SlotTemplate g_suffix, List<String> g_filter, String g_server, List<String> g_sort, int g_maxplayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        List<Section> sections = new ArrayList<>();
        for (String line : config.groupLines) {
            // Its properties
            final int[] startColumn = {-1};
            final List<String> sortrules = new ArrayList<>();
            final int[] maxplayers = {g_maxplayers};

            // Parsing tags
            line = findTag(line, PATTERN_ALIGN_LEFT, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ALIGN LEFT] in verticalMode");
                } else {
                    startColumn[0] = 0;
                }
            });

            line = findTag(line, PATTERN_ALIGN_RIGHT, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ALIGN RIGHT] in verticalMode");
                } else {
                    startColumn[0] = ConfigManager.getCols() - 1;
                }
            });

            line = findTag(line, PATTERN_SORT, matcher -> {
                sortrules.addAll(Arrays.asList(matcher.group(1).split(",")));
                validateSortrules(sortrules);
            });

            line = findTag(line, PATTERN_COLUMN, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [COLUMN=?] in verticalMode");
                } else {
                    startColumn[0] = Integer.parseInt(matcher.group(1));
                }
            });

            line = findTag(line, PATTERN_ROW, matcher -> {
                if (!config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ROW=?] in horizontalMode");
                } else {
                    startColumn[0] = Integer.parseInt(matcher.group(1));
                }
            });

            line = findTag(line, PATTERN_MAXPLAYERS, matcher -> {
                maxplayers[0] = Integer.parseInt(matcher.group(1));
            });

            sortrules.addAll(g_sort);

            Matcher fillplayersMatcher = PATTERN_FILLPLAYERS.matcher(line);
            if (fillplayersMatcher.matches()) {
                SlotTemplate prefix = SlotTemplate.of(g_prefix, parseSlot(fillplayersMatcher.group("prefix")));
                SlotTemplate suffix = SlotTemplate.of(parseSlot(fillplayersMatcher.group("suffix")), g_suffix);
                String args = fillplayersMatcher.group("filter");
                List<String> filter;
                if (args == null || args.isEmpty()) {
                    filter = new ArrayList<>();
                } else {
                    filter = Arrays.asList(args.split(","));
                }
                checkServer(filter);
                filter.addAll(g_filter);
                filter.addAll(Arrays.asList(g_server.split(",")));
                sections.add(new FillPlayersSection(startColumn[0], filter,
                        prefix, suffix, sortrules, maxplayers[0], playerLines, morePlayerLines));
            } else {
                ServerSection section;
                if (sections.size() > 0 && sections.get(sections.size() - 1) instanceof ServerSection && startColumn[0] == -1) {
                    section = (ServerSection) sections.get(sections.size() - 1);
                } else {
                    section = new ServerSection(startColumn[0], Arrays.asList(g_server.split(",")));
                    sections.add(section);
                }
                section.add(SlotTemplate.of(g_prefix, parseSlot(line), g_suffix));
            }
        }
        return sections;
    }

    private SlotTemplate parseSlot(String line) {
        return plugin.getPlaceholderManager().parseSlot(line);
    }

    private String findTag(String text, Pattern pattern, Consumer<Matcher> onFound) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            onFound.accept(matcher);
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return sb.toString();
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
            if (plugin.isServer(s)) {
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
