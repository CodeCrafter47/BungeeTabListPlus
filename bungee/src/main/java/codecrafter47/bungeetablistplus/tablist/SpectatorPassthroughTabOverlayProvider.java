package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import de.codecrafter47.taboverlay.AbstractTabOverlayProvider;
import de.codecrafter47.taboverlay.TabOverlayProviderSet;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.config.player.Player;
import de.codecrafter47.taboverlay.handler.ContentOperationMode;
import de.codecrafter47.taboverlay.handler.HeaderAndFooterOperationMode;
import de.codecrafter47.taboverlay.handler.TabOverlayHandle;
import lombok.SneakyThrows;

public class SpectatorPassthroughTabOverlayProvider extends AbstractTabOverlayProvider<TabOverlayHandle, TabOverlayHandle> implements Runnable {

    private final Player player;
    private final BungeeTabListPlus btlp;
    private boolean shouldBeActive = false;
    private TabOverlayProviderSet tabOverlayProviderSet;

    public SpectatorPassthroughTabOverlayProvider(Player player, BungeeTabListPlus btlp) {
        super("spectator-passthough", 10002, ContentOperationMode.PASS_TROUGH, HeaderAndFooterOperationMode.PASS_TROUGH);
        this.player = player;
        this.btlp = btlp;
    }

    @Override
    protected void activate(TabView tabView, TabOverlayHandle contentHandle, TabOverlayHandle headerAndFooterHandle) {

    }

    @Override
    @SneakyThrows
    protected void attach(TabView tabView) {
        btlp.getMainThreadExecutor().submit(() -> {
            tabOverlayProviderSet = tabView.getTabOverlayProviders();
            player.addDataChangeListener(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE, this);
            Integer gamemode = player.get(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE);
            shouldBeActive = gamemode != null && gamemode == 3;
        }).get();
    }

    @Override
    @SneakyThrows
    protected void detach(TabView tabView) {
        btlp.getMainThreadExecutor().submit(() -> {
            player.removeDataChangeListener(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE, this);
        }).get();
    }

    @Override
    protected void deactivate(TabView tabView) {

    }

    @Override
    protected boolean shouldActivate(TabView tabView) {
        return shouldBeActive;
    }

    @Override
    public void run() {
        Integer gamemode = player.get(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE);
        if (shouldBeActive != (shouldBeActive = gamemode != null && gamemode == 3)) {
            tabOverlayProviderSet.scheduleUpdate();
        }
    }
}
