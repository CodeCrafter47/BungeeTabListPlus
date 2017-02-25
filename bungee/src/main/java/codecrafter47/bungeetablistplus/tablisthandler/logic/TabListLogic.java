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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import io.netty.channel.Channel;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

import java.util.UUID;

public class TabListLogic extends AbstractTabListLogic {

    @Getter
    private final ProxiedPlayer player;
    private final Channel channel;
    private final boolean onlineMode;

    public TabListLogic(TabListHandler parent, ProxiedPlayer player) {
        super(parent);
        this.player = player;
        this.onlineMode = player.getPendingConnection().isOnlineMode();
        try {
            channel = ReflectionUtil.getChannelWrapper(player).getHandle();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    protected void sendPacket(DefinedPacket packet) {
        if (!onlineMode && packet instanceof PlayerListItem) {
            PlayerListItem pli = (PlayerListItem) packet;
            if (pli.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                for (PlayerListItem.Item item : pli.getItems()) {
                    if (fakePlayerUUIDSet.contains(item.getUuid())) {
                        item.setProperties(EMPTY_PROPRTIES);
                    }
                }
            }
        }
        player.unsafe().sendPacket(packet);
    }

    private void failIfNotInEventLoop() {
        if (!channel.eventLoop().inEventLoop()) {
            RuntimeException ex = new RuntimeException("Not in EventLoop");
            BungeeTabListPlus.getInstance().reportError(ex);
            throw ex;
        }
    }

    // Override all methods to add event loop check

    @Override
    public void setResizePolicy(PlayerTablistHandler.ResizePolicy resizePolicy) {
        failIfNotInEventLoop();
        super.setResizePolicy(resizePolicy);
    }

    @Override
    public void onConnected() {
        failIfNotInEventLoop();
        super.onConnected();
    }

    @Override
    public void onDisconnected() {
        failIfNotInEventLoop();
        super.onDisconnected();
    }

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
        failIfNotInEventLoop();
        return super.onPlayerListPacket(packet);
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        failIfNotInEventLoop();
        return super.onTeamPacket(packet);
    }

    @Override
    public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
        failIfNotInEventLoop();
        return super.onPlayerListHeaderFooterPacket(packet);
    }

    @Override
    public void onServerSwitch() {
        failIfNotInEventLoop();
        super.onServerSwitch();
    }

    @Override
    public void setPassThrough(boolean passTrough) {
        failIfNotInEventLoop();
        super.setPassThrough(passTrough);
    }

    @Override
    public void setSize(int size) {
        failIfNotInEventLoop();
        super.setSize(size);
    }

    @Override
    public void setSlot(int index, Icon skin0, String text, int ping) {
        failIfNotInEventLoop();
        super.setSlot(index, skin0, text, ping);
    }

    @Override
    public void updateText(int index, String text) {
        failIfNotInEventLoop();
        super.updateText(index, text);
    }

    @Override
    public void updatePing(int index, int ping) {
        failIfNotInEventLoop();
        super.updatePing(index, ping);
    }

    @Override
    public void setHeaderFooter(String header, String footer) {
        failIfNotInEventLoop();
        super.setHeaderFooter(header, footer);
    }
}
