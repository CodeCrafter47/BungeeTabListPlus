package codecrafter47.bungeetablistplus.util;

import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItem;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfo;

import java.util.Arrays;

public class Property119Handler {
    public static String[][] getProperties(LegacyPlayerListItem.Item item) {
        return Arrays.stream(item.getProperties().toArray(new GameProfile.Property[0])).map(prop -> new String[]{prop.getName(), prop.getValue(), prop.getSignature()}).toArray(String[][]::new);
    }

    public static String[][] getProperties(GameProfile profile) {
        return profile.getProperties().stream().map(prop -> new String[]{prop.getName(), prop.getValue(), prop.getSignature()}).toArray(String[][]::new);
    }

    public static void setProperties(LegacyPlayerListItem.Item item, String[][] properties) {
        item.setProperties(Arrays.asList(Arrays.stream(properties).map(array -> new GameProfile.Property(array[0], array[1], array.length >= 3 ? array[2] : null)).toArray(GameProfile.Property[]::new)));
    }

    public static void setProperties(UpsertPlayerInfo.Entry item, String[][] properties) {
        GameProfile profile = item.getProfile();
        profile.addProperties(Arrays.asList(Arrays.stream(properties).map(array -> new GameProfile.Property(array[0], array[1], array.length >= 3 ? array[2] : null)).toArray(GameProfile.Property[]::new)));
        item.setProfile(profile);
    }
}
