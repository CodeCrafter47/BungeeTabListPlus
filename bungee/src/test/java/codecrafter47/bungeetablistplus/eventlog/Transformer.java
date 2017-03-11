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

package codecrafter47.bungeetablistplus.eventlog;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Transformer {

    private final Map<Properties, String> skinCache = new HashMap<>();
    private long nextSkinId = 0;

    public String wrapProperties(String[][] properties) {
        return skinCache.computeIfAbsent(new Properties(properties), p -> "Skin-" + (nextSkinId++));
    }

    public PlayerSkinWrapper wrapPlayerSkin(Icon skin) {
        return new PlayerSkinWrapper(skin.getPlayer() == null ? null : skin.getPlayer().toString(), wrapProperties(skin.getProperties()));
    }

    public TabListItemWrapper wrapTabListItem(PlayerListItem.Item item) {
        return new TabListItemWrapper(item.getUuid().toString(), item.getUsername(), item.getDisplayName(), item.getPing(), item.getGamemode(), wrapProperties(item.getProperties()));
    }

    public PlayerListPacketWrapper wrapPlayerListPacket(PlayerListItem packet) {
        return new PlayerListPacketWrapper(packet.getAction(), Arrays.stream(packet.getItems()).map(this::wrapTabListItem).collect(Collectors.toCollection(ArrayList::new)));
    }

    private static class Properties {
        private final String[][] properties;

        private Properties(String[][] properties) {
            this.properties = properties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Properties that = (Properties) o;

            return Arrays.deepEquals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(properties);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerSkinWrapper {
        public String owner;
        public String skin;

        public Icon unwrap() {
            return new Icon(owner == null || owner.equals("null") ? null : UUID.fromString(owner), new String[][]{{skin, skin, skin}});
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class TabListItemWrapper {
        public String uuid;
        public String username;
        public String displayName;
        public int ping;
        public int gamemode;
        public String properties;

        public PlayerListItem.Item unwrap() {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(UUID.fromString(uuid));
            item.setUsername(username);
            item.setDisplayName(displayName);
            item.setPing(ping);
            item.setGamemode(gamemode);
            item.setProperties(new String[][]{{properties}});
            return item;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerListPacketWrapper {
        public PlayerListItem.Action action;
        public List<TabListItemWrapper> items;

        public PlayerListItem unwrap() {
            PlayerListItem packet = new PlayerListItem();
            packet.setAction(action);
            packet.setItems(items.stream().map(TabListItemWrapper::unwrap).collect(Collectors.toList()).toArray(new PlayerListItem.Item[items.size()]));
            return packet;
        }
    }
}
