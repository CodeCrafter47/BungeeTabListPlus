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

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.PlaceholderProvider;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;

public class ConditionalPlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bind("insertIfGamemode3").withTemplateArgs().to((context, args) -> context.getPlayer().getGameMode() == 3 ? args : SlotTemplate.empty());
        bind("insertIfHidden").withTemplateArgs().to((context, args) -> BungeeTabListPlus.isHidden(context.getPlayer(), context.getViewer()) ? args : SlotTemplate.empty());
        bind("insertIfServersSame").withTemplateArgs().to((context, args) -> context.getServer() != null && context.getServer().contains(context.getViewer().getServer().getInfo().getName()) ? args : SlotTemplate.empty());
        bind("insertIfServersDifferent").withTemplateArgs().to((context, args) -> context.getServer() == null || !context.getServer().contains(context.getViewer().getServer().getInfo().getName()) ? args : SlotTemplate.empty());
    }
}
