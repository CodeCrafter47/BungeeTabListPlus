
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.section;

//~--- non-JDK imports --------------------------------------------------------

import codecrafter47.bungeetablistplus.tablist.TabList;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public abstract class Section {
    public double id = Math.random();
    
    public abstract int getMinSize(ProxiedPlayer player);

    public abstract int getMaxSize(ProxiedPlayer player);

    public abstract int calculate(ProxiedPlayer player, TabList tabList, int pos, int size);
    
    public abstract void precalculate(ProxiedPlayer player);
    
    // can be -1 for N/A
    public abstract int getStartCollumn();
}


//~ Formatted by Jindent --- http://www.jindent.com
