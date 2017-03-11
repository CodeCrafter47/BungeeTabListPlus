package codecrafter47.bungeetablistplus.data;

import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;

public class ServerDataHolder implements DataHolder {

    private final DataHolder local;
    private final DataHolder bridge;

    public ServerDataHolder(DataHolder local, DataHolder bridge) {
        this.local = local;
        this.bridge = bridge;
    }

    @Override
    public <V> V get(DataKey<V> key) {
        if (key.getScope() == MinecraftData.SCOPE_SERVER) {
            return bridge.get(key);
        } else if (key.getScope() == BungeeData.SCOPE_BUNGEE_SERVER) {
            return local.get(key);
        } else {
            throw new IllegalArgumentException("Unexpected scope " + key.getScope());
        }
    }

    @Override
    public <T> void addDataChangeListener(DataKey<T> key, Runnable listener) {
        if (key.getScope() == MinecraftData.SCOPE_SERVER) {
            bridge.addDataChangeListener(key, listener);
        } else if (key.getScope() == BungeeData.SCOPE_BUNGEE_SERVER) {
            local.addDataChangeListener(key, listener);
        } else {
            throw new IllegalArgumentException("Unexpected scope " + key.getScope());
        }
    }

    @Override
    public <T> void removeDataChangeListener(DataKey<T> key, Runnable listener) {
        if (key.getScope() == MinecraftData.SCOPE_SERVER) {
            bridge.removeDataChangeListener(key, listener);
        } else if (key.getScope() == BungeeData.SCOPE_BUNGEE_SERVER) {
            local.removeDataChangeListener(key, listener);
        } else {
            throw new IllegalArgumentException("Unexpected scope " + key.getScope());
        }
    }
}
