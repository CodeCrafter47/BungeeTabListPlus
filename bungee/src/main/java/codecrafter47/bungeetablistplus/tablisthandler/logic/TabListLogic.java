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

package codecrafter47.bungeetablistplus.tablisthandler.logic;

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.player.FakePlayer;
import codecrafter47.bungeetablistplus.skin.PlayerSkin;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TabListLogic extends AbstractTabListLogic {

    @Getter
    private final ProxiedPlayer player;

    public TabListLogic(TabListHandler parent, ProxiedPlayer player) {
        super(parent);
        this.player = player;
    }

    protected UUID getUniqueId() {
        return player.getUniqueId();
    }

    protected void sendPacket(DefinedPacket packet) {
        player.unsafe().sendPacket(packet);
    }

    public List<IPlayer> getServerTabList() {
        List<IPlayer> list = new ArrayList<>(serverTabList.size());

        for (TabListItem item : serverTabList.values()) {
            FakePlayer fakePlayer = new FakePlayer(item.getUsername(), player.getServer().getInfo(), false);
            fakePlayer.setPing(item.getPing());
            fakePlayer.setGamemode(item.getGamemode());
            fakePlayer.setSkin(new PlayerSkin(item.getUuid(), item.getProperties()));
            list.add(fakePlayer);
        }

        return list;
    }
}
