/*
 *     Copyright (C) 2025 proferabg
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
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfoPacket;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfoPacket;

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
    public PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet) {

        if (packet.getAction() == LegacyPlayerListItemPacket.ADD_PLAYER) {
            for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                UUID uuid = item.getUuid();
                Player player = ProxyServer.getInstance().getPlayer(uuid).orElse(null);
                if (player != null) {
                    rewriteMap.put(uuid, player.getUniqueId());
                }
            }
        }

        boolean modified = false;

        if (packet.getAction() == LegacyPlayerListItemPacket.REMOVE_PLAYER) {
            ListIterator<LegacyPlayerListItemPacket.Item> it = packet.getItems().listIterator();
            while(it.hasNext()){
                LegacyPlayerListItemPacket.Item item = it.next();
                UUID uuid = rewriteMap.remove(item.getUuid());
                modified |= uuid != null;
                it.set(copyToNewItem(MoreObjects.firstNonNull(uuid, item.getUuid()), item));
            }
        } else {
            ListIterator<LegacyPlayerListItemPacket.Item> it = packet.getItems().listIterator();
            while(it.hasNext()){
                LegacyPlayerListItemPacket.Item item = it.next();
                UUID uuid = rewriteMap.get(item.getUuid());
                if (uuid != null) {
                    modified = true;
                    if (packet.getAction() == LegacyPlayerListItemPacket.ADD_PLAYER) {
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
    public PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfoPacket packet) {
        if (packet.getActions().contains(UpsertPlayerInfoPacket.Action.ADD_PLAYER)) {
            for (UpsertPlayerInfoPacket.Entry item : packet.getEntries()) {
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
        ListIterator<UpsertPlayerInfoPacket.Entry> it = packet.getEntries().listIterator();
        while(it.hasNext()){
            UpsertPlayerInfoPacket.Entry item = it.next();
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
    public PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfoPacket packet) {
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

    private LegacyPlayerListItemPacket.Item copyToNewItem(UUID uuid, LegacyPlayerListItemPacket.Item item){
        LegacyPlayerListItemPacket.Item newItem = new LegacyPlayerListItemPacket.Item(uuid);
        newItem.setName(item.getName());
        newItem.setLatency(item.getLatency());
        newItem.setGameMode(item.getGameMode());
        newItem.setDisplayName(item.getDisplayName());
        newItem.setPlayerKey(item.getPlayerKey());
        newItem.setProperties(item.getProperties());
        return newItem;
    }

    private UpsertPlayerInfoPacket.Entry copyToNewEntry(UUID uuid, UpsertPlayerInfoPacket.Entry item){
        UpsertPlayerInfoPacket.Entry newItem = new UpsertPlayerInfoPacket.Entry(uuid);
        newItem.setProfile(item.getProfile());
        newItem.setLatency(item.getLatency());
        newItem.setGameMode(item.getGameMode());
        newItem.setDisplayName(item.getDisplayName());
        newItem.setListed(item.isListed());
        newItem.setChatSession(item.getChatSession());
        return newItem;
    }
}
