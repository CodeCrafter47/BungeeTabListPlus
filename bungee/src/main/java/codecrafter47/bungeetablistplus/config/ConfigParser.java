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
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.PlayerManager;
import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;
import codecrafter47.bungeetablistplus.playersorting.SortingRule;
import codecrafter47.bungeetablistplus.playersorting.SortingRuleRegistry;
import codecrafter47.bungeetablistplus.section.AutoFillPlayers;
import codecrafter47.bungeetablistplus.section.ColumnSplitSection;
import codecrafter47.bungeetablistplus.section.FillBukkitPlayersSection;
import codecrafter47.bungeetablistplus.section.FillPlayersSection;
import codecrafter47.bungeetablistplus.section.PlayerColumn;
import codecrafter47.bungeetablistplus.section.Section;
import codecrafter47.bungeetablistplus.section.ServerSection;
import codecrafter47.bungeetablistplus.section.StaticSection;
import codecrafter47.bungeetablistplus.tablist.GenericServerGroup;
import codecrafter47.bungeetablistplus.tablistproviders.ConfigTabListProvider;
import codecrafter47.bungeetablistplus.tablistproviders.IConfigTabListProvider;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigParser {

    private final BungeeTabListPlus plugin;
    private final int tab_size;

    private static final Pattern PATTERN_ALIGN_BOTTOM = Pattern.compile("\\[ALIGN BOTTOM\\]");
    private static final Pattern PATTERN_ALIGN_LEFT = Pattern.compile("\\[ALIGN LEFT\\]");
    private static final Pattern PATTERN_ALIGN_RIGHT = Pattern.compile("\\[ALIGN RIGHT\\]");
    private static final Pattern PATTERN_COLUMN = Pattern.compile("\\[COLUMN=(\\d+)\\]");
    private static final Pattern PATTERN_ROW = Pattern.compile("\\[ROW=(\\d+)\\]");
    private static final Pattern PATTERN_MAXPLAYERS = Pattern.compile("\\[MAXPLAYERS=(\\d+)\\]");
    private static final Pattern PATTERN_SORT = Pattern.compile("\\[SORT=([^]]+)\\]");

    private static final Pattern PATTERN_FILLPLAYERS = Pattern.compile("^(?<prefix>.*)\\{fillplayers(?::(?<filter>(?:(?:[^{}]*)\\{(?:[^{}]*)\\})*(?:[^{}]*)))?\\}(?<suffix>.*)$");
    private static final Pattern PATTERN_FILLBUKKITPLAYERS = Pattern.compile("^(?<prefix>.*)\\{fillbukkitplayers\\}(?<suffix>.*)$");
    private final int columns;
    private final int rows;

    public ConfigParser(BungeeTabListPlus plugin, int tab_size) {
        this.plugin = plugin;
        this.tab_size = tab_size;
        columns = (tab_size + 19) / 20;
        rows = tab_size / columns;
    }

    public IConfigTabListProvider parse(TabListConfig config) {
        List<SlotTemplate> playerLines = config.playerLines.stream().filter(s -> s != null).map(this::parseSlot).collect(Collectors.toList());
        List<SlotTemplate> morePlayerLines = config.morePlayersLines.stream().filter(s -> s != null).map(this::parseSlot).collect(Collectors.toList());


        List<Function<TabListContext, List<Section>>> topSectionProviders = new ArrayList<>();
        List<Function<TabListContext, List<Section>>> botSectionProviders = new ArrayList<>();
        final boolean[] bottom = {false};
        for (String line : config.tabList) {
            if (line == null) continue;

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
                    startColumn[0] = columns - 1;
                }
            });

            line = findTag(line, PATTERN_COLUMN, matcher -> {
                if (config.verticalMode) {
                    plugin.getLogger().warning("You can not use [COLUMN=?] in verticalMode");
                } else {
                    column[0] = Integer.parseInt(matcher.group(1));
                    if (column[0] >= columns) {
                        plugin.getLogger().warning(String.format("You used [COLUMN=%d] but the tablist only has %d columns. Setting column to %d",
                                column[0], columns, columns - 1));
                        column[0] = columns - 1;
                    }
                }
            });

            line = findTag(line, PATTERN_ROW, matcher -> {
                if (!config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ROW=?] in horizontalMode");
                } else {
                    column[0] = Integer.parseInt(matcher.group(1));
                    if (column[0] >= rows) {
                        plugin.getLogger().warning(String.format("You used [ROW=%d] but the tablist only has %d rows. Setting row to %d",
                                column[0], rows, rows - 1));
                        column[0] = rows - 1;
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
                        boolean allowsExtension = maxplayers[0] == 1000 && startColumn[0] == -1;
                        if (allowsExtension && !sections.isEmpty() && sections.get(sections.size() - 1) instanceof FillPlayersSection
                                && ((FillPlayersSection) sections.get(sections.size() - 1)).allowsExtension()) {
                            ((FillPlayersSection) sections.get(sections.size() - 1)).addPlayers(parseFilter(filter), prefix, suffix, sorter);
                        } else {
                            sections.add(new FillPlayersSection(startColumn[0], parseFilter(filter), prefix, suffix, sorter, maxplayers[0], playerLines, morePlayerLines));
                        }
                    }
                } else {
                    ColumnSplitSection cs;
                    if (!sections.isEmpty() && sections.get(sections.size() - 1) instanceof ColumnSplitSection) {
                        cs = (ColumnSplitSection) sections.get(sections.size() - 1);
                    } else {
                        cs = new ColumnSplitSection();
                        sections.add(cs);
                    }
                    cs.addColumn(column[0], new PlayerColumn(parseFilter(filter), prefix, suffix, sorter, maxplayers[0], playerLines, morePlayerLines));
                }
            } else if (fillbukkitplayersMatcher.matches()) {
                SlotTemplate prefix = parseSlot(fillbukkitplayersMatcher.group("prefix"));
                SlotTemplate suffix = parseSlot(fillbukkitplayersMatcher.group("suffix"));
                sections.add(new FillBukkitPlayersSection(startColumn[0], prefix, suffix, sorter, maxplayers[0], playerLines, morePlayerLines));
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

        SlotTemplate header;
        if (!config.header.isEmpty()) {
            header = SlotTemplate.animate(config.header.stream().filter(s -> s != null).map(this::parseSlot).collect(Collectors.toList()), config.headerCycleInterval);
        } else {
            header = SlotTemplate.empty();
        }
        SlotTemplate footer;
        if (!config.footer.isEmpty()) {
            footer = SlotTemplate.animate(config.footer.stream().filter(s -> s != null).map(this::parseSlot).collect(Collectors.toList()), config.footerCycleInterval);
        } else {
            footer = SlotTemplate.empty();
        }

        return new ConfigTabListProvider(topSectionProviders, plugin, config, config.shownFooterHeader, botSectionProviders, header, footer, tab_size);
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
                list.add(GenericServerGroup.of(strings, alias));
            } else {
                serverSet.remove(server);
                list.add(GenericServerGroup.of(server));
            }
        }

        List<Function<ServerGroup, Section>> sections = new ArrayList<>();
        for (String line : config.groupLines) {
            if (line == null) continue;

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
                    startColumn[0] = columns - 1;
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
                    if (startColumn[0] >= columns) {
                        plugin.getLogger().warning(String.format("You used [COLUMN=%d] but the tablist only has %dcolumns. Setting columns to %d",
                                startColumn[0], columns, columns - 1));
                        startColumn[0] = columns - 1;
                    }
                }
            });

            line = findTag(line, PATTERN_ROW, matcher -> {
                if (!config.verticalMode) {
                    plugin.getLogger().warning("You can not use [ROW=?] in horizontalMode");
                } else {
                    startColumn[0] = Integer.parseInt(matcher.group(1));
                    if (startColumn[0] >= rows) {
                        plugin.getLogger().warning(String.format("You used [ROW=%d] but the tablist only has %d rows. Setting row to %d",
                                startColumn[0], rows, rows - 1));
                        startColumn[0] = rows - 1;
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
                    return new FillPlayersSection(startColumn[0], parseFilter(finalFilters), prefix, suffix, sorter, maxplayers[0], playerLines, morePlayerLines);
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
        return plugin.getPlaceholderManager0().parseSlot(line);
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

    public static PlayerManager.Filter parseFilter(Collection<String> filter) {
        List<PlayerManager.Filter> servers = new ArrayList<>();
        List<PlayerManager.Filter> groups = new ArrayList<>();
        for (String s : filter) {
            if (s.isEmpty()) {
                continue;
            }
            if (s.equalsIgnoreCase("currentserver")) {
                servers.add(new FilterCurrentServer());
            } else if (BungeeTabListPlus.getInstance().isServer(s)) {
                if (s.contains("#")) {
                    String[] split = s.split("#");
                    servers.add(new FilterServerAndWorld(split[0], split[1]));
                } else {
                    servers.add(new FilterServer(s));
                }
            } else {
                groups.add(new FilterGroup(s));
            }
        }
        if (servers.isEmpty() && groups.isEmpty()) {
            return FILTER_ALWAYS_TRUE;
        }
        if (servers.isEmpty()) {
            if (groups.size() == 1) {
                return groups.get(0);
            } else {
                return new OrFilter(groups);
            }
        }
        if (groups.isEmpty()) {
            if (servers.size() == 1) {
                return servers.get(0);
            } else {
                return new OrFilter(servers);
            }
        }
        return new AndFilter(ImmutableList.of(new OrFilter(servers), new OrFilter(groups)));
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

    private static PlayerManager.Filter FILTER_ALWAYS_TRUE = new PlayerManager.Filter() {
        @Override
        public boolean test(ProxiedPlayer viewer, IPlayer iPlayer) {
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == FILTER_ALWAYS_TRUE;
        }
    };

    private static class FilterCurrentServer implements PlayerManager.Filter {
        @Override
        public boolean test(ProxiedPlayer viewer, IPlayer iPlayer) {
            Optional<ServerInfo> server1 = iPlayer.getServer();
            Server server2 = viewer.getServer();
            if (!server1.isPresent() && server2 == null) {
                return true;
            }
            if (server1.isPresent() && server2 != null) {
                return server1.get().getName().endsWith(server2.getInfo().getName());
            }
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FilterCurrentServer;
        }
    }

    private static class FilterServer implements PlayerManager.Filter {
        private final String server;

        public FilterServer(String server) {
            this.server = server;
        }

        @Override
        public boolean test(ProxiedPlayer viewer, IPlayer iPlayer) {
            return iPlayer.getServer().map(serverInfo -> server.equalsIgnoreCase(serverInfo.getName())).orElse(false);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FilterServer && ((FilterServer) obj).server.equals(server);
        }
    }

    private static class FilterServerAndWorld implements PlayerManager.Filter {
        private final String server;
        private final String world;

        public FilterServerAndWorld(String server, String world) {
            this.server = server;
            this.world = world;
        }

        @Override
        public boolean test(ProxiedPlayer viewer, IPlayer iPlayer) {
            return iPlayer.getServer().map(serverInfo -> server.equalsIgnoreCase(serverInfo.getName()) && BungeeTabListPlus.getInstance().getBridge().get(iPlayer, DataKeys.World).map(w -> w.equalsIgnoreCase(world)).orElse(false)).orElse(false);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FilterServerAndWorld && ((FilterServerAndWorld) obj).server.equals(server) && ((FilterServerAndWorld) obj).world.equals(world);
        }
    }

    private static class FilterGroup implements PlayerManager.Filter {
        private final String group;

        public FilterGroup(String group) {
            this.group = group;
        }

        @Override
        public boolean test(ProxiedPlayer viewer, IPlayer iPlayer) {
            return BungeeTabListPlus.getInstance().getPermissionManager().getMainGroup(iPlayer).equalsIgnoreCase(group);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FilterGroup && ((FilterGroup) obj).group.equals(group);
        }
    }

    private static class OrFilter implements PlayerManager.Filter {
        private final List<? extends PlayerManager.Filter> components;

        private OrFilter(List<? extends PlayerManager.Filter> components) {
            this.components = components;
        }

        @Override
        public boolean test(ProxiedPlayer viewer, IPlayer player) {
            for (int i = 0; i < this.components.size(); ++i) {
                if (this.components.get(i).test(viewer, player)) {
                    return true;
                }
            }

            return false;
        }

        public boolean equals(Object obj) {
            if (obj instanceof OrFilter) {
                OrFilter that = (OrFilter) obj;
                return this.components.equals(that.components);
            } else {
                return false;
            }
        }
    }

    private static class AndFilter<T> implements PlayerManager.Filter {
        private final List<? extends PlayerManager.Filter> components;

        private AndFilter(List<? extends PlayerManager.Filter> components) {
            this.components = components;
        }

        @Override
        public boolean test(ProxiedPlayer viewer, IPlayer player) {
            for (int i = 0; i < this.components.size(); ++i) {
                if (!this.components.get(i).test(viewer, player)) {
                    return false;
                }
            }

            return true;
        }

        public boolean equals(Object obj) {
            if (obj instanceof AndFilter) {
                AndFilter that = (AndFilter) obj;
                return this.components.equals(that.components);
            } else {
                return false;
            }
        }
    }
}
