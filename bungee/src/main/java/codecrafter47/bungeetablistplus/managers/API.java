package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.*;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.placeholder.PlayerPlaceholderResolver;
import codecrafter47.bungeetablistplus.placeholder.ServerPlaceholderResolver;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.bungeetablistplus.player.FakePlayerManagerImpl;
import codecrafter47.bungeetablistplus.tablist.DefaultCustomTablist;
import codecrafter47.bungeetablistplus.util.IconUtil;
import com.google.common.base.Preconditions;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.config.icon.IconManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class API extends BungeeTabListPlusAPI {

    private final TabViewManager tabViewManager;
    private final IconManager iconManager;
    private final PlayerPlaceholderResolver playerPlaceholderResolver;
    private final ServerPlaceholderResolver serverPlaceholderResolver;
    private final Logger logger;
    private final BungeeTabListPlus btlp;

    private final Map<String, Variable> variablesByName = new HashMap<>();
    private final Map<String, ServerVariable> serverVariablesByName = new HashMap<>();

    public API(TabViewManager tabViewManager, IconManager iconManager, PlayerPlaceholderResolver playerPlaceholderResolver, ServerPlaceholderResolver serverPlaceholderResolver, Logger logger, BungeeTabListPlus btlp) {
        this.tabViewManager = tabViewManager;
        this.iconManager = iconManager;
        this.playerPlaceholderResolver = playerPlaceholderResolver;
        this.serverPlaceholderResolver = serverPlaceholderResolver;
        this.logger = logger;
        this.btlp = btlp;
    }

    @Override
    protected void setCustomTabList0(ProxiedPlayer player, CustomTablist customTablist) {
        TabView tabView = tabViewManager.getTabView(player);
        if (tabView == null) {
            throw new IllegalStateException("unknown player");
        }
        if (customTablist instanceof DefaultCustomTablist) {
            tabView.getTabOverlayProviders().removeProviders(DefaultCustomTablist.TabOverlayProviderImpl.class);
            ((DefaultCustomTablist) customTablist).addToPlayer(tabView);
        } else {
            throw new IllegalArgumentException("customTablist not created by createCustomTablist()");
        }
    }

    @Override
    protected void removeCustomTabList0(ProxiedPlayer player) {
        TabView tabView = tabViewManager.getTabView(player);
        if (tabView == null) {
            throw new IllegalStateException("unknown player");
        }
        tabView.getTabOverlayProviders().removeProviders(DefaultCustomTablist.TabOverlayProviderImpl.class);
    }

    @Nonnull
    @Override
    protected Icon getIconFromPlayer0(ProxiedPlayer player) {
        return IconUtil.convert(IconUtil.getIconFromPlayer(player));
    }

    @Override
    protected void createIcon0(BufferedImage image, Consumer<Icon> callback) {
        CompletableFuture<de.codecrafter47.taboverlay.Icon> future = iconManager.createIcon(image);
        future.thenAccept(icon -> callback.accept(IconUtil.convert(icon)));
    }

    @Override
    protected void registerVariable0(Plugin plugin, Variable variable) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(variable, "variable");
        String id = variable.getName().toLowerCase();
        Preconditions.checkArgument(!variablesByName.containsKey(id), "Variable name already registered.");
        DataKey<String> dataKey = BTLPBungeeDataKeys.createBungeeThirdPartyVariableDataKey(id);
        playerPlaceholderResolver.addCustomPlaceholderDataKey(id, dataKey);
        btlp.scheduleSoftReload();
        variablesByName.put(id, variable);
    }

    String resolveCustomPlaceholder(String id, ProxiedPlayer player) {
        Variable variable = variablesByName.get(id);
        if (variable != null) {
            try {
                return variable.getReplacement(player);
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Failed to query custom placeholder replacement " + id, th);
            }
        }
        return "";
    }

    @Override
    protected void registerVariable0(Plugin plugin, ServerVariable variable) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(variable, "variable");
        String id = variable.getName().toLowerCase();
        Preconditions.checkArgument(!serverVariablesByName.containsKey(id), "Variable name already registered.");
        DataKey<String> dataKey = BTLPBungeeDataKeys.createBungeeThirdPartyServerVariableDataKey(id);
        serverPlaceholderResolver.addCustomPlaceholderServerDataKey(id, dataKey);
        btlp.scheduleSoftReload();
        serverVariablesByName.put(id, variable);
    }

    String resolveCustomPlaceholderServer(String id, String serverName) {
        ServerVariable variable = serverVariablesByName.get(id);
        if (variable != null) {
            try {
                return variable.getReplacement(serverName);
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Failed to query custom placeholder replacement " + id, th);
            }
        }
        return "";
    }

    @Override
    protected CustomTablist createCustomTablist0() {
        return new DefaultCustomTablist();
    }

    @Override
    protected FakePlayerManager getFakePlayerManager0() {
        FakePlayerManagerImpl fakePlayerManager = btlp.getFakePlayerManagerImpl();
        if (fakePlayerManager == null) {
            throw new IllegalStateException("Cannot call getFakePlayerManager() before onEnable()");
        }
        return fakePlayerManager;
    }

    @Override
    protected boolean isHidden0(ProxiedPlayer player) {
        BungeePlayer bungeePlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayerIfPresent(player);
        return bungeePlayer != null && bungeePlayer.get(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN);
    }
}
