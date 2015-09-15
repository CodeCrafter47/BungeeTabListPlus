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

package codecrafter47.bungeetablistplus.packet;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.tablisthandler.CustomTabList18;
import codecrafter47.bungeetablistplus.tablisthandler.TabList18v3;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.Team;

import java.lang.reflect.Field;
import java.util.logging.Level;

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
        boolean modified = false;
        ProxiedPlayer player = null;
        try {
            if (handler instanceof DownstreamBridge) {
                getPlayerField(DownstreamBridge.class);
                if (playerField != null) {
                    for (int i = 0; i < 5; i++) {
                        try {
                            playerField.setAccessible(true);
                            player = (ProxiedPlayer) playerField.get(handler);
                            break;
                        } catch (IllegalAccessException ex) {
                            if (i == 4) {
                                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to access player object in TeamPacketHandler for " + handler, ex);
                            }
                        }
                    }
                    if (player != null) {
                        Object tabList = BungeeTabListPlus.getTabList(player);
                        if (tabList instanceof CustomTabList18) {
                            modified = ((CustomTabList18) tabList).onTeamPacket(this);
                        }
                    }
                } else {
                    BungeeTabListPlus.getInstance().getLogger().severe("Could not get player for " + handler);
                }
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
        try {
            super.handle(handler);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
        try {
            if (modified) {
                if (player != null) {
                    player.unsafe().sendPacket(this);
                    throw CancelSendSignal.INSTANCE;
                } else {
                    BungeeTabListPlus.getInstance().getLogger().severe("Packet " + this + " has been modified but player is null");
                }
            }
        } catch (CancelSendSignal e) {
            throw e;
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
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
