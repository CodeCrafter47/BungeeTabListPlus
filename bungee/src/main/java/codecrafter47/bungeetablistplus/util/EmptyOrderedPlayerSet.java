package codecrafter47.bungeetablistplus.util;

import de.codecrafter47.taboverlay.config.player.OrderedPlayerSet;
import de.codecrafter47.taboverlay.config.player.Player;

public class EmptyOrderedPlayerSet implements OrderedPlayerSet {

    public static final EmptyOrderedPlayerSet INSTANCE = new EmptyOrderedPlayerSet();

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void addListener(Listener listener) {

    }

    @Override
    public void removeListener(Listener listener) {

    }

    @Override
    public Player getPlayer(int index) {
        throw new IndexOutOfBoundsException();
    }
}
