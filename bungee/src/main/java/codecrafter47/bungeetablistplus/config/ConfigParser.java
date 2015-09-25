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
import codecrafter47.bungeetablistplus.api.ServerGroup;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.section.*;
import codecrafter47.bungeetablistplus.sorting.PlayerSorter;
import codecrafter47.bungeetablistplus.sorting.SortingRule;
import codecrafter47.bungeetablistplus.sorting.SortingRuleRegistry;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import com.google.common.collect.HashMultimap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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


        List<Function<TabListContext, List<Section>>> topSectionProviders = new ArrayList<>();
        List<Function<TabListContext, List<Section>>> botSectionProviders = new ArrayList<>();
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
                    if (column[0] > ConfigManager.getCols()) {
                        plugin.getLogger().warning(String.format("You used [COLUMN=%d] but the tablist only has %d columns. Setting column to %d",
                                column[0], ConfigManager.getCols(), ConfigManager.getCols() - 1));
                        column[0] = ConfigManager.getCols() - 1;
                    }
                }
            });

            line = findTag(line, PATTERN_ROW, matcher -> {
                if (!config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ROW=?] in horizontalMode");
                } else {
                    column[0] = Integer.parseInt(matcher.group(1));
                    if (column[0] > ConfigManager.getRows()) {
                        plugin.getLogger().warning(String.format("You used [ROW=%d] but the tablist only has %d rows. Setting row to %d",
                                column[0], ConfigManager.getRows(), ConfigManager.getRows() - 1));
                        column[0] = ConfigManager.getRows() - 1;
                    }
                }
            });

            line = findTag(line, PATTERN_SORT, matcher -> {
                sortrules.addAll(Arrays.asList(matcher.group(1).split(",")));
            });

            line = findTag(line, PATTERN_MAXPLAYERS, matcher -> {
                maxplayers[0] = Integer.parseInt(matcher.group(1));
            });

            if (startColumn[0] == -1 && column[0] != -1) {
                startColumn[0] = column[0];
            }

            PlayerSorter sorter = parseSortrules(sortrules);

            // Get current section list
            List<Function<TabListContext, List<Section>>> sections;
            if (!bottom[0]) {
                sections = topSectionProviders;
            } else {
                sections = botSectionProviders;
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
                        sections.add(parseServerSections(config, prefix, suffix, filter, sortrules, maxplayers[0], playerLines, morePlayerLines));
                    } else {
                        sections.add(new FillPlayersSection(startColumn[0], filter, prefix, suffix, sorter, maxplayers[0], playerLines, morePlayerLines));
                    }
                } else {
                    ColumnSplitSection cs;
                    if (!sections.isEmpty() && sections.get(sections.size() - 1) instanceof ColumnSplitSection) {
                        cs = (ColumnSplitSection) sections.get(sections.size() - 1);
                    } else {
                        cs = new ColumnSplitSection();
                        sections.add(cs);
                    }
                    cs.addCollumn(column[0], new PlayerColumn(filter, prefix, suffix, sorter, maxplayers[0], playerLines, morePlayerLines));
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

        if (config.header.size() > 1) {
            plugin.requireUpdateInterval(config.headerCycleInterval);
        }

        if (config.footer.size() > 1) {
            plugin.requireUpdateInterval(config.footerCycleInterval);
        }

        SlotTemplate header = SlotTemplate.animate(config.header.stream().map(this::parseSlot).collect(Collectors.toList()), config.headerCycleInterval);
        SlotTemplate footer = SlotTemplate.animate(config.footer.stream().map(this::parseSlot).collect(Collectors.toList()), config.footerCycleInterval);

        return new TabListProvider(plugin, topSectionProviders, botSectionProviders, config, config.shownFooterHeader, header, footer);
    }

    public AutoFillPlayers parseServerSections(TabListConfig config, SlotTemplate g_prefix, SlotTemplate g_suffix, List<String> g_filter, List<String> g_sort, int g_maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();

        Set<String> serverSet = new HashSet<>(servers.keySet());
        HashMultimap<String, String> aliasToServerMap = HashMultimap.create();
        for (Map.Entry<String, String> entry : BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().serverAlias.entrySet()) {
            if (ProxyServer.getInstance().getServerInfo(entry.getKey()) == null) {
                BungeeTabListPlus.getInstance().getLogger().warning("Configuration Error: Server \"" + entry.getKey() + "\" used in the alias map does not exist.");
                continue;
            }
            aliasToServerMap.put(entry.getValue(), entry.getKey());
        }

        List<ServerGroup> list = new ArrayList<>();
        while (!serverSet.isEmpty()) {
            String server = serverSet.iterator().next();
            String alias = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().serverAlias.get(server);
            if (alias != null) {
                Set<String> strings = aliasToServerMap.get(alias);
                serverSet.removeAll(strings);
                list.add(ServerGroup.of(serverSet, alias));
            } else {
                serverSet.remove(server);
                list.add(ServerGroup.of(server));
            }
        }

        List<Function<ServerGroup, Section>> sections = new ArrayList<>();
        for (String line : config.groupLines) {
            // Its properties
            final int[] startColumn = {-1};
            final List<String> sortrules = new ArrayList<>();
            final int[] maxplayers = {g_maxPlayers};

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
            });

            line = findTag(line, PATTERN_COLUMN, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [COLUMN=?] in verticalMode");
                } else {
                    startColumn[0] = Integer.parseInt(matcher.group(1));
                    if (startColumn[0] > ConfigManager.getCols()) {
                        plugin.getLogger().warning(String.format("You used [COLUMN=%d] but the tablist only has %dcolumns. Setting columns to %d",
                                startColumn[0], ConfigManager.getCols(), ConfigManager.getCols() - 1));
                        startColumn[0] = ConfigManager.getCols() - 1;
                    }
                }
            });

            line = findTag(line, PATTERN_ROW, matcher -> {
                if (!config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ROW=?] in horizontalMode");
                } else {
                    startColumn[0] = Integer.parseInt(matcher.group(1));
                    if (startColumn[0] > ConfigManager.getRows()) {
                        plugin.getLogger().warning(String.format("You used [ROW=%d] but the tablist only has %d rows. Setting row to %d",
                                startColumn[0], ConfigManager.getRows(), ConfigManager.getRows() - 1));
                        startColumn[0] = ConfigManager.getRows() - 1;
                    }
                }
            });

            line = findTag(line, PATTERN_MAXPLAYERS, matcher -> {
                maxplayers[0] = Integer.parseInt(matcher.group(1));
            });

            sortrules.addAll(g_sort);
            PlayerSorter sorter = parseSortrules(sortrules);

            Matcher fillplayersMatcher = PATTERN_FILLPLAYERS.matcher(line);
            if (fillplayersMatcher.matches()) {
                SlotTemplate prefix = SlotTemplate.of(g_prefix, parseSlot(fillplayersMatcher.group("prefix")));
                SlotTemplate suffix = SlotTemplate.of(parseSlot(fillplayersMatcher.group("suffix")), g_suffix);
                String args = fillplayersMatcher.group("filter");
                List<String> filter;
                if (args == null || args.isEmpty()) {
                    filter = new ArrayList<>();
                } else {
                    filter = new ArrayList<>(Arrays.asList(args.split(",")));
                }
                checkServer(filter);
                filter.addAll(g_filter);
                sections.add(group -> {
                    List<String> finalFilters = new ArrayList<>();
                    finalFilters.addAll(filter);
                    finalFilters.addAll(group.getServerNames());
                    return new FillPlayersSection(startColumn[0], finalFilters, prefix, suffix, sorter, maxplayers[0], playerLines, morePlayerLines);
                });
            } else {
                SlotTemplate slotTemplate = SlotTemplate.of(g_prefix, parseSlot(line), g_suffix);
                sections.add(group -> {
                    ServerSection serverSection = new ServerSection(startColumn[0], group);
                    serverSection.add(slotTemplate);
                    return serverSection;
                });
            }
        }

        return new AutoFillPlayers(list, sections, config.showEmptyGroups);
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

    private PlayerSorter parseSortrules(List<String> ruleNames) {
        List<SortingRule> rules = new ArrayList<>();
        for (String name : ruleNames) {
            Optional<SortingRule> rule = SortingRuleRegistry.getRule(name);
            if (rule.isPresent()) {
                rules.add(rule.get());
            } else {
                plugin.getLogger().warning("Can't sort players using rule '" + rule + "': Unknown rule");
            }
        }
        return new PlayerSorter(rules);
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
