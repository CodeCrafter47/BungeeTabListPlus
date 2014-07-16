
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.section;

//~--- non-JDK imports --------------------------------------------------------

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabList;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class ServerSection extends StaticSection {
    String server;

    public ServerSection(int vAlign, String server) {
        super(vAlign);
        this.server = server;
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos, int size) {
        for(Slot s : text){
            Slot s2 = new Slot(BungeeTabListPlus.getInstance().getVariablesManager().replaceServerVariables(s.text, ProxyServer.getInstance().getServerInfo(server)), s.ping);
            tabList.setSlot(pos++, s2);
        }
        return pos;
    }

    public String getServer() {
        return server;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
