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
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.api.TabList;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Florian Stober
 */
public class ServerSection extends StaticSection {

    String server;

    public ServerSection(int vAlign, String server) {
        super(vAlign);
        this.server = server;
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
                         int size) {
        for (Slot s : text) {
            Slot s2 = new Slot(s);
            s2.text = BungeeTabListPlus.getInstance().
                    getVariablesManager().replaceServerVariables(player, s.text,
                    ProxyServer.getInstance().getServerInfo(server));
            tabList.setSlot(pos++, s2);
        }
        return pos;
    }

    public String getServer() {
        return server;
    }
}
