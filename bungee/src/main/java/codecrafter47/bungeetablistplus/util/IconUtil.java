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

package codecrafter47.bungeetablistplus.util;

import de.codecrafter47.bungeetablistplus.bungee.compat.PropertyUtil;
import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.ProfileProperty;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;

import javax.annotation.Nonnull;
import java.util.Arrays;

@UtilityClass
public class IconUtil {

    private static final boolean USE_PROTOCOL_PROPERTY_TYPE;

    static {
        boolean classPresent = false;
        try {
            Class.forName("net.md_5.bungee.protocol.Property");
            classPresent = true;
        } catch (ClassNotFoundException ignored) {
        }
        USE_PROTOCOL_PROPERTY_TYPE = classPresent;
    }

    public Icon convert(codecrafter47.bungeetablistplus.api.bungee.Icon icon) {
        String[][] properties = icon.getProperties();
        if (properties.length == 0) {
            return Icon.DEFAULT_STEVE;
        }
        return new Icon(new ProfileProperty(properties[0][0], properties[0][1], properties[0][2]));
    }

    public codecrafter47.bungeetablistplus.api.bungee.Icon convert(Icon icon) {
        if (icon.hasTextureProperty()) {
            ProfileProperty property = icon.getTextureProperty();
            return new codecrafter47.bungeetablistplus.api.bungee.Icon(null, new String[][]{{property.getName(), property.getValue(), property.getSignature()}});
        } else {
            return new codecrafter47.bungeetablistplus.api.bungee.Icon(null, new String[0][]);
        }
    }

    @Nonnull
    public Icon getIconFromPlayer(ProxiedPlayer player) {
        LoginResult loginResult = ((UserConnection) player).getPendingConnection().getLoginProfile();
        if (loginResult != null) {
            String[][] properties;
            if(USE_PROTOCOL_PROPERTY_TYPE) {
                properties = PropertyUtil.getProperties(loginResult);
            } else {
                properties = Arrays.stream(loginResult.getProperties()).map(prop -> new String[]{prop.getName(), prop.getValue(), prop.getSignature()}).toArray(String[][]::new);
            }
            if (properties.length != 0) {
                for (String[] s : properties) {
                    if (s[0].equals("textures")) {
                        return new Icon(new ProfileProperty(s[0], s[1], s[2]));
                    }
                }
            }
        }
        if ((player.getUniqueId().hashCode() & 1) == 1) {
            return Icon.DEFAULT_ALEX;
        } else {
            return Icon.DEFAULT_STEVE;
        }
    }
}
