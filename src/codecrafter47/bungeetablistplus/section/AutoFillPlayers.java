package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.tablist.TabList;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Florian Stober
 */
public class AutoFillPlayers extends Section {

    public String prefix, suffix;
    public int startColumn, maxPlayers;
    public List<String> sortRules;

    public AutoFillPlayers(int startColumn, String prefix, String suffix,
            List<String> sortRules, int maxPlayers) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.startColumn = startColumn;
        this.sortRules = sortRules;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public int getMinSize(ProxiedPlayer player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
            int size) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getStartCollumn() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
