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
import com.google.common.base.Charsets;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import lombok.SneakyThrows;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;
import java.util.concurrent.FutureTask;

public class FakePlayer extends AbstractPlayer implements codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer {
    private final String name;
    private final UUID uuid;
    private boolean randomServerSwitchEnabled;

    private final DataCache data = new DataCache();

    public FakePlayer(String name, ServerInfo server, boolean randomServerSwitchEnabled) {
        this.randomServerSwitchEnabled = randomServerSwitchEnabled;
        this.name = name;
        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));

        data.updateValue(BungeeData.BungeeCord_Server, server.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    @Override
    public Icon getIcon() {
        return data.get(BTLPBungeeDataKeys.DATA_KEY_ICON);
    }

    @SneakyThrows
    private void executeInMainThread(Runnable task) {
        BungeeTabListPlus btlp = BungeeTabListPlus.getInstance();
        if (btlp.getResendThread().isInMainThread()) {
            task.run();
        } else {
            FutureTask<Void> futureTask = new FutureTask<>(task, null);
            btlp.runInMainThread(futureTask);
            futureTask.get();
        }
    }

    @Override
    public void setPing(int ping) {
        executeInMainThread(() -> data.updateValue(BungeeData.BungeeCord_Ping, ping));
    }

    @Override
    public boolean isRandomServerSwitchEnabled() {
        return randomServerSwitchEnabled;
    }

    @Override
    public void setRandomServerSwitchEnabled(boolean value) {
        randomServerSwitchEnabled = value;
    }

    public void setGamemode(int gamemode) {
        executeInMainThread(() -> data.updateValue(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE, gamemode));
    }

    @Override
    public void changeServer(ServerInfo newServer) {
        executeInMainThread(() -> data.updateValue(BungeeData.BungeeCord_Server, newServer.getName()));
    }

    @Override
    public void setIcon(Icon icon) {
        executeInMainThread(() -> data.updateValue(BTLPBungeeDataKeys.DATA_KEY_ICON, icon));
    }

    @Override
    public <V> V get(DataKey<V> key) {
        return data.get(key);
    }

    @Override
    public <T> void addDataChangeListener(DataKey<T> key, Runnable listener) {
        data.addDataChangeListener(key, listener);
    }

    @Override
    public <T> void removeDataChangeListener(DataKey<T> key, Runnable listener) {
        data.removeDataChangeListener(key, listener);
    }
}
