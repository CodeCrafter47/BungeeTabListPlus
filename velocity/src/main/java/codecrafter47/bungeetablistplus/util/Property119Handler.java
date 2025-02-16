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

package codecrafter47.bungeetablistplus.util;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfoPacket;

import java.util.Arrays;

public class Property119Handler {
    public static String[][] getProperties(LegacyPlayerListItemPacket.Item item) {
        return Arrays.stream(item.getProperties().toArray(new GameProfile.Property[0])).map(prop -> new String[]{prop.getName(), prop.getValue(), prop.getSignature()}).toArray(String[][]::new);
    }

    public static String[][] getProperties(GameProfile profile) {
        return profile.getProperties().stream().map(prop -> new String[]{prop.getName(), prop.getValue(), prop.getSignature()}).toArray(String[][]::new);
    }

    public static void setProperties(LegacyPlayerListItemPacket.Item item, String[][] properties) {
        item.setProperties(Arrays.asList(Arrays.stream(properties).map(array -> new GameProfile.Property(array[0], array[1], array.length >= 3 ? array[2] : null)).toArray(GameProfile.Property[]::new)));
    }

    public static void setProperties(UpsertPlayerInfoPacket.Entry item, String[][] properties) {
        GameProfile profile = item.getProfile();
        profile.addProperties(Arrays.asList(Arrays.stream(properties).map(array -> new GameProfile.Property(array[0], array[1], array.length >= 3 ? array[2] : null)).toArray(GameProfile.Property[]::new)));
        item.setProfile(profile);
    }
}
