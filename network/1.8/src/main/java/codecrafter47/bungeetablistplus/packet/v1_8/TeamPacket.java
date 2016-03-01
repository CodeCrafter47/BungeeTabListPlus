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

package codecrafter47.bungeetablistplus.packet.v1_8;

import codecrafter47.bungeetablistplus.packet.TeamPacketHandler;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.Team;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static Logger getLogger() {
        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugin("BungeeTabListPlus");
        return plugin != null ? plugin.getLogger() : ProxyServer.getInstance().getLogger();
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
                                getLogger().log(Level.SEVERE, "Failed to access player object in TeamPacketHandler", ex);
                            }
                        }
                    }
                    if (player != null) {
                        Field field = UserConnection.class.getDeclaredField("tabListHandler");
                        field.setAccessible(true);
                        Object tabList = field.get(player);
                        if (tabList instanceof TeamPacketHandler) {
                            modified = ((TeamPacketHandler) tabList).onTeamPacket(this);
                        }
                    }
                } else {
                    getLogger().severe("Could not get player for " + handler);
                }
            }
        } catch (Throwable th) {
            getLogger().log(Level.SEVERE, "Unexpected Exception", th);
        }
        try {
            super.handle(handler);
        } catch (Throwable th) {
            getLogger().log(Level.WARNING, "An error has occurred while handling a scoreboard packet. This is a serious issue and may lead to a client crash. The issue is caused by one of you bukkit plugins.", th);
        } finally {
            try {
                if (modified) {
                    if (player != null) {
                        player.unsafe().sendPacket(this);
                        throw CancelSendSignal.INSTANCE;
                    } else {
                        getLogger().severe("Packet " + this + " has been modified but player is null");
                    }
                }
            } catch (CancelSendSignal e) {
                throw e;
            } catch (Throwable th) {
                getLogger().log(Level.SEVERE, "Unexpected Exception", th);
            }
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
