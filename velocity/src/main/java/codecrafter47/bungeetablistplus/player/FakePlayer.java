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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.velocity.Icon;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.util.IconUtil;
import codecrafter47.bungeetablistplus.util.ProxyServer;
import com.google.common.base.Charsets;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.data.velocity.api.VelocityData;
import io.netty.util.concurrent.EventExecutor;
import lombok.SneakyThrows;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class FakePlayer extends AbstractPlayer implements codecrafter47.bungeetablistplus.api.velocity.tablist.FakePlayer {

    private boolean randomServerSwitchEnabled;

    final DataCache data = new DataCache();

    private final EventExecutor mainThread;

    public FakePlayer(String name, ServerInfo server, boolean randomServerSwitchEnabled, EventExecutor mainThread) {
        super(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)), name);
        this.randomServerSwitchEnabled = randomServerSwitchEnabled;
        this.mainThread = mainThread;
        data.updateValue(VelocityData.Velocity_Server, server.getName());
    }

    @Override
    public Optional<RegisteredServer> getServer() {
        return Optional.ofNullable(get(VelocityData.Velocity_Server)).map(ProxyServer.getInstance()::getServer).orElse(null);
    }

    @Override
    public int getPing() {
        Integer ping = get(VelocityData.Velocity_Ping);
        return ping != null ? ping : 0;
    }

    @Override
    public Icon getIcon() {
        return IconUtil.convert(data.get(BTLPVelocityDataKeys.DATA_KEY_ICON));
    }

    @Override
    @SneakyThrows
    public void setPing(int ping) {
        if (!mainThread.inEventLoop()) {
            mainThread.submit(() -> setPing(ping)).sync();
            return;
        }
        data.updateValue(VelocityData.Velocity_Ping, ping);
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
        data.updateValue(VelocityData.Velocity_Server, newServer.getName());
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("deprecation")
    public void setIcon(Icon icon) {
        setIcon(IconUtil.convert(icon));
    }

    @Override
    @SneakyThrows
    public void setIcon(de.codecrafter47.taboverlay.Icon icon) {
        if (!mainThread.inEventLoop()) {
            mainThread.submit(() -> setIcon(icon)).sync();
            return;
        }
        data.updateValue(BTLPVelocityDataKeys.DATA_KEY_ICON, icon);
    }

    @Override
    protected DataHolder getResponsibleDataHolder(DataKey<?> key) {

        if (key.getScope().equals(VelocityData.SCOPE_VELOCITY_PLAYER) || key.getScope().equals(MinecraftData.SCOPE_PLAYER)) {
            return data;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_SERVER) || key.getScope().equals(VelocityData.SCOPE_VELOCITY_SERVER)) {
            return serverData;
        }

        BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Data key with unknown scope: " + key);
        return NullDataHolder.INSTANCE;
    }
}
