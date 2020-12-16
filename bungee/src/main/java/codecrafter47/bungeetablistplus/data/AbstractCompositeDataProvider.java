package codecrafter47.bungeetablistplus.data;

import codecrafter47.bungeetablistplus.managers.HiddenPlayersManager;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.taboverlay.config.player.Player;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCompositeDataProvider<T> {

    @Getter
    private final DataKey<T> compositeDataKey;
    private final Map<BungeePlayer, HiddenPlayersManager.PlayerDataListener> playerDataListenerMap = new HashMap<>();

    protected AbstractCompositeDataProvider(DataKey<T> compositeDataKey) {
        this.compositeDataKey = compositeDataKey;
    }

    public final void onPlayerAdded(Player player) {
        if (player instanceof BungeePlayer) {
            HiddenPlayersManager.PlayerDataListener playerDataListener = new HiddenPlayersManager.PlayerDataListener((BungeePlayer) player);
            playerDataListenerMap.put((BungeePlayer) player, playerDataListener);
            registerListener(player, playerDataListener);
            playerDataListener.run();
        }
    }

    protected abstract void registerListener(Player player, HiddenPlayersManager.PlayerDataListener playerDataListener);

    public final void onPlayerRemoved(Player player) {
        if (player instanceof BungeePlayer) {
            HiddenPlayersManager.PlayerDataListener playerDataListener = playerDataListenerMap.remove(player);
            if (playerDataListener != null) {
                unregisterListener(player, playerDataListener);
            }
        }
    }

    protected abstract void unregisterListener(Player player, HiddenPlayersManager.PlayerDataListener playerDataListener);

    protected abstract T computeCompositeData(BungeePlayer player);

    protected class PlayerDataListener implements Runnable {
        private final BungeePlayer player;

        private PlayerDataListener(BungeePlayer player) {
            this.player = player;
        }

        @Override
        public void run() {
            player.getLocalDataCache().updateValue(getCompositeDataKey(), computeCompositeData(player));
        }
    }
}
