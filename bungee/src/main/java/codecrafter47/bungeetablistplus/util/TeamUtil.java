package codecrafter47.bungeetablistplus.util;

import net.md_5.bungee.protocol.packet.Team;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;

public class TeamUtil {

    // The latest BungeeCord uses Optional<Integer> instead of int for the team color.
    // If that setter is present we invoke it via reflection, otherwise we fall back to Team.setColor(int).
    @Nullable
    private static final Method TEAM_SET_COLOR_OPTIONAL;

    static {
        // Newer BungeeCord versions changed Team.setColor to accept Optional<Integer>.
        Method setColorOptional;
        try {
            setColorOptional = Team.class.getMethod("setColor", Optional.class);
        } catch (NoSuchMethodException e) {
            setColorOptional = null;
        }
        TEAM_SET_COLOR_OPTIONAL = setColorOptional;
    }

    public static void setColor(Team team, int color) {
        if (TEAM_SET_COLOR_OPTIONAL != null) {
            try {
                TEAM_SET_COLOR_OPTIONAL.invoke(team, Optional.of(color));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to set team color", e);
            }
        } else {
            team.setColor(color);
        }

    }

}
