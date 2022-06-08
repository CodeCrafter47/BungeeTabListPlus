package de.codecrafter47.bungeetablistplus.bungee.compat;

import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.Arrays;

public class PropertyUtil {
    public static String[][] getProperties(PlayerListItem.Item item) {
        return Arrays.stream(item.getProperties()).map(prop -> new String[]{prop.getName(), prop.getValue(), prop.getSignature()}).toArray(String[][]::new);
    }

    public static String[][] getProperties(LoginResult loginResult) {
        return Arrays.stream(loginResult.getProperties()).map(prop -> new String[]{prop.getName(), prop.getValue(), prop.getSignature()}).toArray(String[][]::new);
    }

    public static void setProperties(PlayerListItem.Item item, String[][] properties) {
        item.setProperties(Arrays.stream(properties).map(array -> new Property(array[0], array[1], array.length >= 3 ? array[2] : null)).toArray(Property[]::new));
    }
}
