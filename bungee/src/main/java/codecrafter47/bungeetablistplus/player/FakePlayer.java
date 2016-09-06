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
import codecrafter47.bungeetablistplus.api.bungee.Skin;
import codecrafter47.bungeetablistplus.data.DataKey;
import com.google.common.base.Charsets;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;
import java.util.UUID;

public class FakePlayer implements Player, codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer {
    private final String name;
    ServerInfo server;
    private int ping;
    private int gamemode;
    private Skin skin;
    private final UUID uuid;
    private boolean randomServerSwitchEnabled;

    public FakePlayer(String name, ServerInfo server, boolean randomServerSwitchEnabled) {
        this.randomServerSwitchEnabled = randomServerSwitchEnabled;
        this.ping = 0;
        this.gamemode = 0;
        this.skin = null;
        this.name = name;
        this.server = server;
        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
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
    public Optional<ServerInfo> getServer() {
        return Optional.of(server);
    }

    @Override
    public int getPing() {
        return ping;
    }

    @Override
    public Skin getSkin() {
        return skin != null ? skin : BungeeTabListPlus.getInstance().getSkinManager().getSkin(name);
    }

    @Override
    public int getGameMode() {
        return gamemode;
    }

    @Override
    public void setPing(int ping) {
        this.ping = ping;
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
        this.gamemode = gamemode;
    }

    @Override
    public void changeServer(ServerInfo newServer) {
        server = newServer;
    }

    @Override
    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    @Override
    public <T> Optional<T> get(DataKey<T> key) {
        ConnectedPlayer player = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayerIfPresent(getName());
        if (player != null) {
            return player.get(key);
        }
        if (key.equals(BungeeTabListPlus.DATA_KEY_GAMEMODE)) {
            return (Optional<T>) Optional.of(gamemode);
        }
        if (key.equals(BungeeTabListPlus.DATA_KEY_SERVER)) {
            return (Optional<T>) Optional.of(server.getName());
        }
        if (key.equals(BungeeTabListPlus.DATA_KEY_ICON)) {
            Skin skin = getSkin();
            return (Optional<T>) Optional.of(new Icon(skin.getOwner(), skin.toProperty()));
        }
        return Optional.empty();
    }
}
