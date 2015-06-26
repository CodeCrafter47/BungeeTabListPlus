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

package codecrafter47.bungeetablistplus.packets;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.tablisthandler.TabList18v3;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.Team;

import java.lang.reflect.Field;

/**
 * Created by florian on 15.06.15.
 */
public class TeamPacket extends Team {
    private static Field playerField = null;

    public TeamPacket(String name) {
        super(name);
    }

    public TeamPacket() {
    }

    public TeamPacket(String name, byte mode, String displayName, String prefix, String suffix, String nameTagVisibility, byte color, byte friendlyFire, String[] players) {
        super(name, mode, displayName, prefix, suffix, nameTagVisibility, color, friendlyFire, players);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        if (handler instanceof DownstreamBridge) {
            getPlayerField(DownstreamBridge.class);
            if (playerField != null) {
                playerField.setAccessible(true);
                ProxiedPlayer player = (ProxiedPlayer) playerField.get(handler);
                if (BungeeTabListPlus.getTabList(player) instanceof TabList18v3) {
                    Server server = player.getServer();
                    if (server != null) {
                        BungeeTabListPlus.getInstance().getLogger().warning("Server " + server.getInfo().getName() + " uses Scoreboard teams. This feature is not compatible with BungeeTabListPlus.");
                    } else {
                        BungeeTabListPlus.getInstance().getLogger().warning("Player " + player.getName() + " received a Scoreboard team packet. This feature is not compatible with BungeeTabListPlus.");
                    }
                    throw CancelSendSignal.INSTANCE;
                }
            } else {
                BungeeTabListPlus.getInstance().getLogger().severe("Could not get player for " + handler);
            }
        }
        super.handle(handler);
    }

    private static Field getPlayerField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (ProxiedPlayer.class.isAssignableFrom(field.getType())) {
                playerField = field;
                return field;
            }
        }
        return null;
    }
}
