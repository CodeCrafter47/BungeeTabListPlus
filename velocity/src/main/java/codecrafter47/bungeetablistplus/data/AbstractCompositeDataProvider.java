package codecrafter47.bungeetablistplus.data;

import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.taboverlay.config.player.Player;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCompositeDataProvider<T> {

    @Getter
    private final DataKey<T> compositeDataKey;
    private final Map<CompositeKey, PlayerDataListener> playerDataListenerMap = new HashMap<>();

    protected AbstractCompositeDataProvider(DataKey<T> compositeDataKey) {
        this.compositeDataKey = compositeDataKey;
    }

    public final void onPlayerAdded(Player player, DataKey<T> key) {
        if (player instanceof VelocityPlayer) {
            PlayerDataListener playerDataListener = new PlayerDataListener((VelocityPlayer) player, key);
            playerDataListenerMap.put(new CompositeKey((VelocityPlayer) player, key), playerDataListener);
            registerListener(player, key, playerDataListener);
            playerDataListener.run();
        }
    }

    protected abstract void registerListener(Player player, DataKey<T> key, Runnable playerDataListener);

    public final void onPlayerRemoved(Player player, DataKey<T> key) {
        if (player instanceof VelocityPlayer) {
            PlayerDataListener playerDataListener = playerDataListenerMap.remove(new CompositeKey((VelocityPlayer) player, key));
            if (playerDataListener != null) {
                unregisterListener(player, key, playerDataListener);
            }
        }
    }

    protected abstract void unregisterListener(Player player, DataKey<T> key, Runnable listener);

    protected abstract T computeCompositeData(VelocityPlayer player, DataKey<T> key);

    protected class PlayerDataListener implements Runnable {
        private final VelocityPlayer player;
        private final DataKey<T> dataKey;

        private PlayerDataListener(VelocityPlayer player, DataKey<T> dataKey) {
            this.player = player;
            this.dataKey = dataKey;
        }

        @Override
        public void run() {
            player.getLocalDataCache().updateValue(dataKey, computeCompositeData(player, dataKey));
        }
    }

    @Value
    private static class CompositeKey {
        VelocityPlayer player;
        DataKey<?> dataKey;
    }
}
