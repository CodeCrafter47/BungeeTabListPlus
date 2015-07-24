/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.skin.PlayerSkin;
import codecrafter47.bungeetablistplus.skin.Skin;

import java.util.Optional;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.connection.LoginResult;

import java.util.UUID;

public class BungeePlayer implements IPlayer {

    private final ProxiedPlayer player;
    private Skin skin = null;

    public BungeePlayer(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueID() {
        return player.getUniqueId();
    }

    @Override
    public Optional<ServerInfo> getServer() {
        Server server = player.getServer();
        if (server == null) return Optional.empty();
        return Optional.of(server.getInfo());
    }

    @Override
    public int getPing() {
        return player.getPing();
    }

    @Override
    public Skin getSkin() {
        if (!BungeeTabListPlus.isVersion18()) return SkinManager.defaultSkin;
        if (skin == null) {
            LoginResult loginResult = ((UserConnection) player).
                    getPendingConnection().getLoginProfile();
            if (loginResult != null) {
                for (LoginResult.Property s : loginResult.getProperties()) {
                    if (s.getName().equals("textures")) {
                        skin = new PlayerSkin(player.getUniqueId(), new String[]{s.getName(), s.getValue(), s.getSignature()});
                    }
                }
            }
            if (skin == null) {
                skin = new PlayerSkin(player.getUniqueId(), null);
            }
        }
        return skin;
    }

    @Override
    public int getGameMode() {
        return player.getPendingConnection().getVersion() < 47 ? 0 : ((UserConnection) player).getGamemode();
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }
}
