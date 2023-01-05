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

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.velocity.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.velocity.FakePlayerManager;
import codecrafter47.bungeetablistplus.api.velocity.ServerVariable;
import codecrafter47.bungeetablistplus.api.velocity.Variable;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.placeholder.PlayerPlaceholderResolver;
import codecrafter47.bungeetablistplus.placeholder.ServerPlaceholderResolver;
import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import codecrafter47.bungeetablistplus.player.FakePlayerManagerImpl;
import codecrafter47.bungeetablistplus.tablist.DefaultCustomTablist;
import codecrafter47.bungeetablistplus.util.IconUtil;
import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.Player;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.config.icon.IconManager;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
    protected TabView getTabViewForPlayer0(Player player) {
        TabView tabView = tabViewManager.getTabView(player);
        if (tabView == null) {
            throw new IllegalStateException("unknown player");
        }
        return tabView;
    }

    @Nonnull
    @Override
    protected de.codecrafter47.taboverlay.Icon getPlayerIcon0(Player player) {
        return IconUtil.getIconFromPlayer(player);
    }

    @Override
    protected CompletableFuture<de.codecrafter47.taboverlay.Icon> getIconFromImage0(BufferedImage image) {
        return iconManager.createIcon(image);
    }

    @Override
    protected void registerVariable0(Object plugin, Variable variable) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(variable, "variable");
        String id = variable.getName().toLowerCase();
        Preconditions.checkArgument(!variablesByName.containsKey(id), "Variable name already registered.");
        DataKey<String> dataKey = BTLPVelocityDataKeys.createBungeeThirdPartyVariableDataKey(id);
        playerPlaceholderResolver.addCustomPlaceholderDataKey(id, dataKey);
        btlp.scheduleSoftReload();
        variablesByName.put(id, variable);
    }

    String resolveCustomPlaceholder(String id, Player player) {
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
    protected void registerVariable0(Object plugin, ServerVariable variable) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(variable, "variable");
        String id = variable.getName().toLowerCase();
        Preconditions.checkArgument(!serverVariablesByName.containsKey(id), "Variable name already registered.");
        DataKey<String> dataKey = BTLPVelocityDataKeys.createBungeeThirdPartyServerVariableDataKey(id);
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
    protected FakePlayerManager getFakePlayerManager0() {
        FakePlayerManagerImpl fakePlayerManager = btlp.getFakePlayerManagerImpl();
        if (fakePlayerManager == null) {
            throw new IllegalStateException("Cannot call getFakePlayerManager() before onEnable()");
        }
        return fakePlayerManager;
    }

    @Override
    protected boolean isHidden0(Player player) {
        VelocityPlayer velocityPlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayerIfPresent(player);
        return velocityPlayer != null && velocityPlayer.get(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN);
    }
}
