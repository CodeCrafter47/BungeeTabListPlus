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

package codecrafter47.bungeetablistplus.tablistproviders.legacy;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.config.old.TabListConfig;
import codecrafter47.bungeetablistplus.layout.Layout;
import codecrafter47.bungeetablistplus.layout.TablistLayoutManager;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.section.Section;
import codecrafter47.bungeetablistplus.tablist.GenericTabListContext;
import lombok.SneakyThrows;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ConfigTabListProvider implements IConfigTabListProvider {

    protected final BungeeTabListPlus plugin;
    protected final List<Function<TabListContext, List<Section>>> topSectionsProvider;
    protected final List<Function<TabListContext, List<Section>>> botSectionsProvider;
    protected final TabListConfig config;
    protected final boolean showHeaderFooter;
    protected final SlotTemplate header;
    protected final SlotTemplate footer;
    private final TablistLayoutManager<Section> layoutManager = new TablistLayoutManager<>();
    private final int tab_size;

    public ConfigTabListProvider(List<Function<TabListContext, List<Section>>> top, BungeeTabListPlus plugin, TabListConfig config, boolean showHeaderFooter, List<Function<TabListContext, List<Section>>> bot, SlotTemplate header, SlotTemplate footer, int tab_size) {
        this.topSectionsProvider = top;
        this.plugin = plugin;
        this.config = config;
        this.showHeaderFooter = showHeaderFooter;
        this.botSectionsProvider = bot;
        this.header = header;
        this.footer = footer;
        this.tab_size = tab_size;
    }

    @Override
    @SneakyThrows
    public void fillTabList(ProxiedPlayer player, TabList tabList, TabListContext context) {
        if (config.verticalMode) {
            tabList = tabList.flip();
            context = new GenericTabListContext(tabList.getRows(), tabList.getColumns(), player, context.getPlayerManager()).setPlayer(context.getPlayer());
        }

        if (config.autoShrinkTabList) {
            tabList.setShouldShrink(true);
        }

        final TabListContext finalContext = context;
        List<Section> topSections = topSectionsProvider.stream().flatMap(f -> f.apply(finalContext).stream()).collect(Collectors.toCollection(ArrayList::new));
        List<Section> botSections = botSectionsProvider.stream().flatMap(f -> f.apply(finalContext).stream()).collect(Collectors.toCollection(ArrayList::new));

        // precalculate all sections
        preCalculateSections(context, topSections);
        preCalculateSections(context, botSections);

        // remove empty sections
        for (Iterator<Section> iterator = topSections.iterator(); iterator.hasNext(); ) {
            Section topSection = iterator.next();
            if (topSection.getMaxSize() == 0) {
                iterator.remove();
            }
        }
        for (Iterator<Section> iterator = botSections.iterator(); iterator.hasNext(); ) {
            Section botSection = iterator.next();
            if (botSection.getMaxSize() == 0) {
                iterator.remove();
            }
        }


        // calc tablist
        Layout<Section> layout = layoutManager.calculateLayout(topSections, botSections, context);
        for (int i = 0; i < tabList.getSize(); i++) {
            Optional<Layout<Section>.SlotData> slotData = layout.getSlotData(i);
            if (slotData.isPresent()) {
                Layout<Section>.SlotData data = slotData.get();
                tabList.setSlot(i, data.getSection().getSlotAt(context, data.getSlotIndex(), data.getSectionSize()));
            }
        }

        // header + footer
        if (showHeaderFooter) {
            tabList.setHeader(header.buildSlot(context).getText());
            tabList.setFooter(footer.buildSlot(context).getText());
        }

        tabList.setDefaultSkin(plugin.getSkinManager().getSkin(config.defaultSkin));

        tabList.setDefaultPing(config.defaultPing);
    }

    @Override
    public int getWishedTabListSize() {
        return tab_size;
    }

    private void preCalculateSections(TabListContext context, List<Section> topSections) {
        for (Section section : topSections) {
            section.preCalculate(context);
        }
    }

    @Override
    public boolean appliesTo(ProxiedPlayer player) {
        if (config.showTo.equalsIgnoreCase("ALL")) {
            return true;
        }

        if (config.showTo.equalsIgnoreCase("1.8")) {
            return BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player);
        }

        if (config.showTo.equalsIgnoreCase("1.7")) {
            return !BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player);
        }

        String[] s = config.showTo.split(":");

        if (s.length != 2) {
            return false;
        }

        if (s[0].equalsIgnoreCase("player")) {
            if (s[1].equalsIgnoreCase(player.getName()) || s[1].equalsIgnoreCase(player.getUniqueId().toString())) {
                return true;
            }
        }

        if (s[0].equalsIgnoreCase("players")) {
            for (String p : s[1].split(",")) {
                if (p.equalsIgnoreCase(player.getName()) || p.equalsIgnoreCase(player.getUniqueId().toString())) {
                    return true;
                }
            }
        }

        Server playerServer = player.getServer();
        if (playerServer != null) {
            String server = playerServer.getInfo().getName();

            if (s[0].equalsIgnoreCase("server")) {
                if (s[1].equalsIgnoreCase(server)) {
                    return true;
                }
            }

            if (s[0].equalsIgnoreCase("servers")) {
                for (String sv : s[1].split(",")) {
                    if (sv.equalsIgnoreCase(server)) {
                        return true;
                    }
                }
            }
        }

        ConnectedPlayer connectedPlayer = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayerIfPresent(player);
        if (connectedPlayer != null) {
            String group = BungeeTabListPlus.getInstance().getPermissionManager().getMainGroup(connectedPlayer);

            if (group != null) {
                if (s[0].equalsIgnoreCase("group")) {
                    if (s[1].equalsIgnoreCase(group)) {
                        return true;
                    }
                }

                if (s[0].equals("groups")) {
                    for (String sv : s[1].split(",")) {
                        if (sv.equalsIgnoreCase(group)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int getPriority() {
        return config.priority;
    }
}