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

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.protocol.AbstractPacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.util.Property119Handler;
import codecrafter47.bungeetablistplus.util.ProxyServer;
import com.google.common.base.MoreObjects;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItem;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfo;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfo;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

public class RewriteLogic extends AbstractPacketHandler {

    private final Map<UUID, UUID> rewriteMap = new HashMap<>();

    public RewriteLogic(PacketHandler parent) {
        super(parent);
    }

    @Override
    public PacketListenerResult onPlayerListPacket(LegacyPlayerListItem packet) {

        if (packet.getAction() == LegacyPlayerListItem.ADD_PLAYER) {
            for (LegacyPlayerListItem.Item item : packet.getItems()) {
                UUID uuid = item.getUuid();
                Player player = ProxyServer.getInstance().getPlayer(uuid).orElse(null);
                if (player != null) {
                    rewriteMap.put(uuid, player.getUniqueId());
                }
            }
        }

        boolean modified = false;

        if (packet.getAction() == LegacyPlayerListItem.REMOVE_PLAYER) {
            ListIterator<LegacyPlayerListItem.Item> it = packet.getItems().listIterator();
            while(it.hasNext()){
                LegacyPlayerListItem.Item item = it.next();
                UUID uuid = rewriteMap.remove(item.getUuid());
                modified |= uuid != null;
                it.set(copyToNewItem(MoreObjects.firstNonNull(uuid, item.getUuid()), item));
            }
        } else {
            ListIterator<LegacyPlayerListItem.Item> it = packet.getItems().listIterator();
            while(it.hasNext()){
                LegacyPlayerListItem.Item item = it.next();
                UUID uuid = rewriteMap.get(item.getUuid());
                if (uuid != null) {
                    modified = true;
                    if (packet.getAction() == LegacyPlayerListItem.ADD_PLAYER) {
                        Player player = ProxyServer.getInstance().getPlayer(item.getUuid()).orElse(null);
                        if (player != null) {
                            String[][] properties = Property119Handler.getProperties(player.getGameProfile());
                            Property119Handler.setProperties(item, properties);
                        }
                    }
                    it.set(copyToNewItem(uuid, item));
                }
            }
        }

        PacketListenerResult result = super.onPlayerListPacket(packet);
        return result == PacketListenerResult.PASS && modified ? PacketListenerResult.MODIFIED : result;
    }

    @Override
    public PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfo packet) {
        if (packet.getActions().contains(UpsertPlayerInfo.Action.ADD_PLAYER)) {
            for (UpsertPlayerInfo.Entry item : packet.getEntries()) {
                UUID uuid = item.getProfileId();
                Player player = ProxyServer.getInstance().getPlayer(uuid).orElse(null);
                if (player != null) {
                    rewriteMap.put(uuid, player.getUniqueId());
                    String[][] properties = Property119Handler.getProperties(player.getGameProfile());
                    Property119Handler.setProperties(item, properties);
                }
            }
        }
        boolean modified = false;
        ListIterator<UpsertPlayerInfo.Entry> it = packet.getEntries().listIterator();
        while(it.hasNext()){
            UpsertPlayerInfo.Entry item = it.next();
            UUID uuid = rewriteMap.get(item.getProfileId());
            if (uuid != null) {
                modified = true;
                it.set(copyToNewEntry(uuid, item));
            }
        }
        PacketListenerResult result = super.onPlayerListUpdatePacket(packet);
        return result == PacketListenerResult.PASS && modified ? PacketListenerResult.MODIFIED : result;
    }

    @Override
    public PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfo packet) {
        boolean modified = false;

        UUID[] uuids = packet.getProfilesToRemove().toArray(new UUID[0]);
        for (int i = 0; i < uuids.length; i++) {
            UUID uuid = rewriteMap.remove(uuids[i]);
            if (uuid != null) {
                modified = true;
                uuids[i] = uuid;
            }
        }

        PacketListenerResult result = super.onPlayerListRemovePacket(packet);
        return result == PacketListenerResult.PASS && modified ? PacketListenerResult.MODIFIED : result;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        rewriteMap.clear();

        super.onServerSwitch(is13OrLater);
    }

    private LegacyPlayerListItem.Item copyToNewItem(UUID uuid, LegacyPlayerListItem.Item item){
        LegacyPlayerListItem.Item newItem = new LegacyPlayerListItem.Item(uuid);
        newItem.setName(item.getName());
        newItem.setLatency(item.getLatency());
        newItem.setGameMode(item.getGameMode());
        newItem.setDisplayName(item.getDisplayName());
        newItem.setPlayerKey(item.getPlayerKey());
        newItem.setProperties(item.getProperties());
        return newItem;
    }

    private UpsertPlayerInfo.Entry copyToNewEntry(UUID uuid, UpsertPlayerInfo.Entry item){
        UpsertPlayerInfo.Entry newItem = new UpsertPlayerInfo.Entry(uuid);
        newItem.setProfile(item.getProfile());
        newItem.setLatency(item.getLatency());
        newItem.setGameMode(item.getGameMode());
        newItem.setDisplayName(item.getDisplayName());
        newItem.setListed(item.isListed());
        newItem.setChatSession(item.getChatSession());
        return newItem;
    }
}
