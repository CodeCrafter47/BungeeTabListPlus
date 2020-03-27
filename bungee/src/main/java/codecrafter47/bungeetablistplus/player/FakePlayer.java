/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.util.IconUtil;
import com.google.common.base.Charsets;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import io.netty.util.concurrent.EventExecutor;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;
import java.util.UUID;

public class FakePlayer extends AbstractPlayer implements codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer {

    private boolean randomServerSwitchEnabled;

    final DataCache data = new DataCache();

    private final EventExecutor mainThread;

    public FakePlayer(String name, ServerInfo server, boolean randomServerSwitchEnabled, EventExecutor mainThread) {
        super(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name);
        this.randomServerSwitchEnabled = randomServerSwitchEnabled;
        this.mainThread = mainThread;
        data.updateValue(BungeeData.BungeeCord_Server, server.getName());
    }

    @Override
    public Optional<ServerInfo> getServer() {
        return Optional.ofNullable(get(BungeeData.BungeeCord_Server)).map(ProxyServer.getInstance()::getServerInfo);
    }

    @Override
    public int getPing() {
        Integer ping = get(BungeeData.BungeeCord_Ping);
        return ping != null ? ping : 0;
    }

    @Override
    public Icon getIcon() {
        return IconUtil.convert(data.get(BTLPBungeeDataKeys.DATA_KEY_ICON));
    }

    @Override
    @SneakyThrows
    public void setPing(int ping) {
        if (!mainThread.inEventLoop()) {
            mainThread.submit(() -> setPing(ping)).sync();
            return;
        }
        data.updateValue(BungeeData.BungeeCord_Ping, ping);
    }

    @Override
    public boolean isRandomServerSwitchEnabled() {
        return randomServerSwitchEnabled;
    }

    @Override
    public void setRandomServerSwitchEnabled(boolean value) {
        randomServerSwitchEnabled = value;
    }

    @Override
    @SneakyThrows
    public void changeServer(ServerInfo newServer) {
        if (!mainThread.inEventLoop()) {
            mainThread.submit(() -> changeServer(newServer)).sync();
            return;
        }
        data.updateValue(BungeeData.BungeeCord_Server, newServer.getName());
    }

    @Override
    @SneakyThrows
    public void setIcon(Icon icon) {
        if (!mainThread.inEventLoop()) {
            mainThread.submit(() -> setIcon(icon)).sync();
            return;
        }
        data.updateValue(BTLPBungeeDataKeys.DATA_KEY_ICON, IconUtil.convert(icon));
    }

    @Override
    protected DataHolder getResponsibleDataHolder(DataKey<?> key) {

        if (key.getScope().equals(BungeeData.SCOPE_BUNGEE_PLAYER) || key.getScope().equals(MinecraftData.SCOPE_PLAYER)) {
            return data;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_SERVER) || key.getScope().equals(BungeeData.SCOPE_BUNGEE_SERVER)) {
            return serverData;
        }

        BungeeTabListPlus.getInstance().getLogger().warning("Data key with unknown scope: " + key);
        return NullDataHolder.INSTANCE;
    }
}
