package codecrafter47.bungeetablistplus.data;

import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.data.velocity.api.VelocityData;
import de.codecrafter47.taboverlay.config.player.Player;

public class PermissionDataProvider extends AbstractCompositeDataProvider<Boolean> {

    public PermissionDataProvider() {
        super(BTLPVelocityDataKeys.Permission);
    }

    @Override
    protected void registerListener(Player player, DataKey<Boolean> key, Runnable listener) {
        player.addDataChangeListener(MinecraftData.permission(key.getParameter()), listener);
        player.addDataChangeListener(VelocityData.permission(key.getParameter()), listener);
    }

    @Override
    protected void unregisterListener(Player player, DataKey<Boolean> key, Runnable listener) {
        player.removeDataChangeListener(MinecraftData.permission(key.getParameter()), listener);
        player.removeDataChangeListener(VelocityData.permission(key.getParameter()), listener);
    }

    @Override
    protected Boolean computeCompositeData(VelocityPlayer player, DataKey<Boolean> key) {
        return player.get(MinecraftData.permission(key.getParameter())) == Boolean.TRUE
                || player.get(VelocityData.permission(key.getParameter())) == Boolean.TRUE;
    }
}
