/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
import lombok.SneakyThrows;

import java.util.HashSet;
import java.util.Set;

public class ExcludedServersTabOverlayProvider extends AbstractTabOverlayProvider<TabOverlayHandle, TabOverlayHandle> implements Runnable {

    private final Player player;
    private final BungeeTabListPlus btlp;
    private boolean shouldBeActive = false;
    private TabOverlayProviderSet tabOverlayProviderSet;
    private static Set<ExcludedServersTabOverlayProvider> attachedProviders = new HashSet<>();

    public ExcludedServersTabOverlayProvider(Player player, BungeeTabListPlus btlp) {
        super("excluded-servers", 10003, ContentOperationMode.PASS_TROUGH, HeaderAndFooterOperationMode.PASS_TROUGH);
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
            player.addDataChangeListener(BungeeData.BungeeCord_Server, this);
            String server = player.get(BungeeData.BungeeCord_Server);
            shouldBeActive = server == null || btlp.getExcludedServers().contains(server);
            attachedProviders.add(this);
        }).sync();
    }

    @Override
    @SneakyThrows
    protected void detach(TabView tabView) {
        btlp.getMainThreadExecutor().submit(() -> {
            attachedProviders.remove(this);
            player.removeDataChangeListener(BungeeData.BungeeCord_Server, this);
        }).sync();
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
        String server = player.get(BungeeData.BungeeCord_Server);
        shouldBeActive = server == null || btlp.getExcludedServers().contains(server);
        tabOverlayProviderSet.scheduleUpdate();
    }

    public static void onReload() {
        BungeeTabListPlus.getInstance().getMainThreadExecutor().execute(() -> {
            for (ExcludedServersTabOverlayProvider provider : attachedProviders) {
                provider.run();
            }
        });
    }
}
