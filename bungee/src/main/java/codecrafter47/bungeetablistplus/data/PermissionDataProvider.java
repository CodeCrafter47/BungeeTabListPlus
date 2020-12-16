package codecrafter47.bungeetablistplus.data;

import codecrafter47.bungeetablistplus.player.BungeePlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.taboverlay.config.player.Player;

public class PermissionDataProvider extends AbstractCompositeDataProvider<Boolean> {

    public PermissionDataProvider() {
        super(BTLPBungeeDataKeys.Permission);
    }

    @Override
    protected void registerListener(Player player, DataKey<Boolean> key, Runnable listener) {
        player.addDataChangeListener(MinecraftData.permission(key.getParameter()), listener);
        player.addDataChangeListener(BungeeData.permission(key.getParameter()), listener);
    }

    @Override
    protected void unregisterListener(Player player, DataKey<Boolean> key, Runnable listener) {
        player.removeDataChangeListener(MinecraftData.permission(key.getParameter()), listener);
        player.removeDataChangeListener(BungeeData.permission(key.getParameter()), listener);
    }

    @Override
    protected Boolean computeCompositeData(BungeePlayer player, DataKey<Boolean> key) {
        return player.get(MinecraftData.permission(key.getParameter())) == Boolean.TRUE
                || player.get(BungeeData.permission(key.getParameter())) == Boolean.TRUE;
    }
}
