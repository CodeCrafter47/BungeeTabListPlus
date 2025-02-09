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

package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.util.VelocityPlugin;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "bungeetablistplus",
    name = "BungeeTabListPlus",
    authors = "CodeCrafter47 & proferabg",
    version = "@VERSION@",
    dependencies = {
        @Dependency(id = "redisbungee", optional = true),
        @Dependency(id = "luckperms", optional = true),
        @Dependency(id = "geyser", optional = true),
        @Dependency(id = "floodgate", optional = true),
        @Dependency(id = "viaversion", optional = true)
    }
)
public class BootstrapPlugin implements VelocityPlugin {

    private final Metrics.Factory metricsFactory;

    private static final String NO_RELOAD_PLAYERS = "Cannot reload BungeeTabListPlus while players are online.";

    @Getter
    private final Logger logger;

    @Getter
    private final ProxyServer proxy;

    @Getter
    private final Path dataDirectory;

    @Getter
    private final String version;


    @Inject
    public BootstrapPlugin(final ProxyServer proxy, final Logger logger, final @DataDirectory Path dataDirectory, final Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.version = BootstrapPlugin.class.getAnnotation(Plugin.class).version();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        if (Float.parseFloat(System.getProperty("java.class.version")) < 61.0) {
            getLogger().error("§cBungeeTabListPlus requires Java 17 or above. Please download and install it!");
            getLogger().error("Disabling plugin!");
            return;
        }
        if (!getProxy().getAllPlayers().isEmpty()) {
            for (Player player : getProxy().getAllPlayers()) {
                player.disconnect(Component.text(NO_RELOAD_PLAYERS));
            }
        }
        getProxy().getPluginManager().getPlugin("BungeeTabListPlus");
        BungeeTabListPlus.getInstance(this).onLoad();
        BungeeTabListPlus.getInstance(this).onEnable();
        // Metrics
        metricsFactory.make(this, 4332);
    }

    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        BungeeTabListPlus.getInstance().onDisable();
        if (!getProxy().isShuttingDown()) {
            getLogger().error("You cannot use ServerUtils to reload BungeeTabListPlus. Use /btlp reload instead.");
            if (!getProxy().getAllPlayers().isEmpty()) {
                for (Player proxiedPlayer : getProxy().getAllPlayers()) {
                    proxiedPlayer.disconnect(Component.text(NO_RELOAD_PLAYERS));
                }
            }
        }
    }
}
