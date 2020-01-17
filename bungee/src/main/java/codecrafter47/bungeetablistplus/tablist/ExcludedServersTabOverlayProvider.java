package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.taboverlay.AbstractTabOverlayProvider;
import de.codecrafter47.taboverlay.TabOverlayProviderSet;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.config.player.Player;
import de.codecrafter47.taboverlay.handler.ContentOperationMode;
import de.codecrafter47.taboverlay.handler.HeaderAndFooterOperationMode;
import de.codecrafter47.taboverlay.handler.TabOverlayHandle;

public class ExcludedServersTabOverlayProvider extends AbstractTabOverlayProvider<TabOverlayHandle, TabOverlayHandle> implements Runnable {

    private final Player player;
    private final BungeeTabListPlus btlp;
    private boolean shouldBeActive = false;
    private TabOverlayProviderSet tabOverlayProviderSet;

    public ExcludedServersTabOverlayProvider(Player player, BungeeTabListPlus btlp) {
        super("excluded-servers", 10003, ContentOperationMode.PASS_TROUGH, HeaderAndFooterOperationMode.PASS_TROUGH);
        this.player = player;
        this.btlp = btlp;
    }


    @Override
    protected void activate(TabView tabView, TabOverlayHandle contentHandle, TabOverlayHandle headerAndFooterHandle) {

    }

    @Override
    protected void attach(TabView tabView) {
        tabOverlayProviderSet = tabView.getTabOverlayProviders();
        player.addDataChangeListener(BungeeData.BungeeCord_Server, this);
        shouldBeActive = btlp.getExcludedServers().contains(player.get(BungeeData.BungeeCord_Server));
    }

    @Override
    protected void detach(TabView tabView) {
        player.removeDataChangeListener(BungeeData.BungeeCord_Server, this);
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
        shouldBeActive = btlp.getExcludedServers().contains(player.get(BungeeData.BungeeCord_Server));
        tabOverlayProviderSet.scheduleUpdate();
    }
}
