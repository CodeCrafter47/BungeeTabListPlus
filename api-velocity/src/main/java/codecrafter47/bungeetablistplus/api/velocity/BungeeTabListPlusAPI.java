package codecrafter47.bungeetablistplus.api.velocity;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import de.codecrafter47.taboverlay.TabView;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public abstract class BungeeTabListPlusAPI {
    private static BungeeTabListPlusAPI instance = null;
    
    public static void registerVariable(Plugin plugin, Variable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.registerVariable0(plugin, variable);
    }
    
    protected abstract void registerVariable0(Plugin plugin, Variable variable);
    
    public static void registerVariable(Plugin plugin, ServerVariable variable) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        instance.registerVariable0(plugin, variable);
    }
    
    protected abstract void registerVariable0(Plugin plugin, ServerVariable variable);
    
    @Nonnull
    public static de.codecrafter47.taboverlay.Icon getPlayerIcon(Player player) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        return instance.getPlayerIcon0(player);
    }
    
    @Nonnull
    protected abstract de.codecrafter47.taboverlay.Icon getPlayerIcon0(Player player);
    
    public static CompletableFuture<de.codecrafter47.taboverlay.Icon> getIconFromImage(BufferedImage image) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        return instance.getIconFromImage0(image);
    }
    
    protected abstract CompletableFuture<de.codecrafter47.taboverlay.Icon> getIconFromImage0(BufferedImage image);
    
    public static TabView getTabViewForPlayer(Player player) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        return instance.getTabViewForPlayer0(player);
    }
    
    protected abstract TabView getTabViewForPlayer0(Player player);
    
    public static FakePlayerManager getFakePlayerManager() {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        return instance.getFakePlayerManager0();
    }
    
    protected abstract FakePlayerManager getFakePlayerManager0();
    
    public static boolean isHidden(Player player) {
        Preconditions.checkState(instance != null, "instance is null, is the plugin enabled?");
        return instance.isHidden0(player);
    }
    
    protected abstract boolean isHidden0(Player player);
}
