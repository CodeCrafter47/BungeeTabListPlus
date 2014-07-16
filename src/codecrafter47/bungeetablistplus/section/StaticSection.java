
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.section;

//~--- non-JDK imports --------------------------------------------------------

import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabList;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class StaticSection extends Section {
    List<Slot> text;
    int vAlign;

    public StaticSection(int vAlign, List<Slot> text) {
        this.vAlign = vAlign;
        this.text = text;
    }

    public StaticSection(int vAlign) {
        this.vAlign = vAlign;
        this.text = new ArrayList<>();
    }

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return text.size();
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        return text.size();
    }

    public void add(Slot slot) {
        text.add(slot);
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos, int size) {
        for(Slot s : text){
            tabList.setSlot(pos++, new Slot(s.text, s.ping));
        }
        return pos;
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
    }

    @Override
    public int getStartCollumn() {
        return vAlign;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
