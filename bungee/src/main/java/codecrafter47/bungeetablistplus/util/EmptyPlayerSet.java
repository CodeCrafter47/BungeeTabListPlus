package codecrafter47.bungeetablistplus.util;

import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.expression.template.ExpressionTemplate;
import de.codecrafter47.taboverlay.config.player.OrderedPlayerSet;
import de.codecrafter47.taboverlay.config.player.Player;
import de.codecrafter47.taboverlay.config.player.PlayerSet;
import de.codecrafter47.taboverlay.config.player.PlayerSetPartition;
import de.codecrafter47.taboverlay.config.template.PlayerOrderTemplate;

import java.util.Collection;
import java.util.Collections;

public class EmptyPlayerSet implements PlayerSet {

    public static final EmptyPlayerSet INSTANCE = new EmptyPlayerSet();

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
    public Collection<? extends Player> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public OrderedPlayerSet getOrderedPlayerSet(Context context, PlayerOrderTemplate playerOrderTemplate) {
        return EmptyOrderedPlayerSet.INSTANCE;
    }

    @Override
    public PlayerSetPartition getPartition(ExpressionTemplate partitionFunction) {
        throw new UnsupportedOperationException();
    }
}
