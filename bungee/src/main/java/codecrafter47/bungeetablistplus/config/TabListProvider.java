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
import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.ITabListProvider;
import codecrafter47.bungeetablistplus.layout.Layout;
import codecrafter47.bungeetablistplus.layout.LayoutException;
import codecrafter47.bungeetablistplus.layout.TablistLayoutManager;
import codecrafter47.bungeetablistplus.section.Section;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Florian Stober
 */
public class TabListProvider implements ITabListProvider {

    private final BungeeTabListPlus plugin;
    private final List<Function<TabListContext, List<Section>>> topSectionsProvider;
    private final List<Function<TabListContext, List<Section>>> botSectionsProvider;
    private final boolean showEmptyGroups;
    private final TabListConfig config;
    private final ConfigParser parser;
    private final TablistLayoutManager<Section> layoutManager = new TablistLayoutManager<>();
    private final boolean showHeaderFooter;
    private final SlotTemplate header;
    private final SlotTemplate footer;

    public TabListProvider(BungeeTabListPlus plugin, List<Function<TabListContext, List<Section>>> top, List<Function<TabListContext, List<Section>>> bot,
                           boolean showEmpty, TabListConfig config, ConfigParser parser, boolean showHeaderFooter, SlotTemplate header, SlotTemplate footer) {
        this.topSectionsProvider = top;
        this.botSectionsProvider = bot;
        showEmptyGroups = showEmpty;
        this.config = config;
        this.parser = parser;
        this.plugin = plugin;
        this.showHeaderFooter = showHeaderFooter;
        this.header = header;
        this.footer = footer;
    }

    @Override
    public void fillTabList(ProxiedPlayer player, ITabList tabList, TabListContext context) throws LayoutException {
        if (config.verticalMode) {
            tabList = tabList.flip();
        }

        if (config.autoShrinkTabList) {
            tabList.setShouldShrink(true);
        }

        List<Section> topSections = topSectionsProvider.stream().flatMap(f -> f.apply(context).stream()).collect(Collectors.toCollection(ArrayList::new));
        List<Section> botSections = botSectionsProvider.stream().flatMap(f -> f.apply(context).stream()).collect(Collectors.toCollection(ArrayList::new));

        // precalculate all sections
        precalculateSections(context, topSections);
        precalculateSections(context, botSections);

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

        if (BungeeTabListPlus.isVersion18()) {
            tabList.setDefaultSkin(plugin.getSkinManager().getSkin(config.defaultSkin));
        }

        tabList.setDefaultPing(config.defaultPing);
    }

    private void precalculateSections(TabListContext context, List<Section> topSections) {
        for (Section section : topSections) {
            section.precalculate(context);
        }
    }

    @Override
    public boolean appliesTo(ProxiedPlayer player) {
        return config.appliesTo(player);
    }
}
