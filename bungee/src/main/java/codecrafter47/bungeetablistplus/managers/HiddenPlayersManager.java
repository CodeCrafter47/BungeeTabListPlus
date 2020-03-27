package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.taboverlay.config.player.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiddenPlayersManager {

    private final List<VanishProvider> vanishProviders = new ArrayList<>();
    private final Map<BungeePlayer, PlayerDataListener> playerDataListenerMap = new HashMap<>();
    private boolean active = false;

    public void enable() {
        active = true;
    }

    public void disable() {
        active = false;
    }

    /**
     * Adds a vanish provider. To be called during setup.
     */
    public void addVanishProvider(String name, DataKey<Boolean> dataIsHidden) {
        if (active) {
            throw new IllegalStateException("Cannot call addVanishProvider() after enable()");
        }
        this.vanishProviders.add(new VanishProvider(name, dataIsHidden));
    }

    void onPlayerAdded(Player player) {
        if (player instanceof BungeePlayer) {
            PlayerDataListener playerDataListener = new PlayerDataListener((BungeePlayer) player);
            playerDataListenerMap.put((BungeePlayer) player, playerDataListener);
            for (VanishProvider vanishProvider : vanishProviders) {
                player.addDataChangeListener(vanishProvider.dataIsHidden, playerDataListener);
            }
            playerDataListener.run();
        }
    }

    void onPlayerRemoved(Player player) {
        if (player instanceof BungeePlayer) {
            PlayerDataListener playerDataListener = playerDataListenerMap.remove(player);
            if (playerDataListener != null) {
                for (VanishProvider vanishProvider : vanishProviders) {
                    player.removeDataChangeListener(vanishProvider.dataIsHidden, playerDataListener);
                }
            }
        }
    }

    public List<String> getActiveVanishProviders(BungeePlayer player) {
        List<String> activeVanishProviders = new ArrayList<>();
        for (VanishProvider vanishProvider : vanishProviders) {
            if (Boolean.TRUE.equals(player.get(vanishProvider.dataIsHidden))) {
                activeVanishProviders.add(vanishProvider.name);
            }
        }
        return activeVanishProviders;
    }

    private class PlayerDataListener implements Runnable {
        private final BungeePlayer player;

        private PlayerDataListener(BungeePlayer player) {
            this.player = player;
        }

        @Override
        public void run() {
            boolean hidden = false;
            for (VanishProvider vanishProvider : vanishProviders) {
                hidden = hidden || Boolean.TRUE.equals(player.get(vanishProvider.dataIsHidden));
            }
            player.getLocalDataCache().updateValue(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN, hidden);
        }
    }

    @Data
    private static class VanishProvider {
        private final String name;
        private final DataKey<Boolean> dataIsHidden;
    }
}
