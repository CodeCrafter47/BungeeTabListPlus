package codecrafter47.bungeetablistplus.util;

import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.ProfileProperty;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;

import javax.annotation.Nonnull;

@UtilityClass
public class IconUtil {

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
            LoginResult.Property[] properties = loginResult.getProperties();
            if (properties != null) {
                for (LoginResult.Property s : properties) {
                    if (s.getName().equals("textures")) {
                        return new Icon(new ProfileProperty(s.getName(), s.getValue(), s.getSignature()));
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
