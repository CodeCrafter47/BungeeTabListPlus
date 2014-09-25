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
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ITabListProvider;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.api.TabList;
import codecrafter47.bungeetablistplus.config.TabListProvider;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.util.ColorParser;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MyTabList extends MyCustom implements IMyTabListHandler {

    public MyTabList(ProxiedPlayer player) {
        super(player);

    }

    @Override
    public void recreate() {
        if (getPlayer().getServer() != null) {
            if (BungeeTabListPlus.getInstance().getConfigManager().
                    getMainConfig().excludeServers.contains(getPlayer().
                            getServer().getInfo().getName()) || isExcluded) {
                unload();
                return;
            }
        }

        ITabListProvider tlp = BungeeTabListPlus.getInstance().
                getTabListManager().getTabListForPlayer(super.getPlayer());
        if (tlp == null) {
            exclude();
            unload();
            return;
        }
        super.clear();
        TabList tabList = tlp.getTabList(super.getPlayer());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getUsedSlots(); i++) {
            Slot line = tabList.getSlot(i);
            if (line == null) {
                line = new Slot("");
            }
            line.text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(line.text, super.getPlayer());
            line.text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replaceVariables(line.text);
            line.text = ChatColor.translateAlternateColorCodes('&', line.text);
            if (charLimit > 0) {
                line.text = ColorParser.substringIgnoreColors(line.text,
                        charLimit);
            }
            super.setSlot(i, line, false);
        }
        super.update();
    }
}
