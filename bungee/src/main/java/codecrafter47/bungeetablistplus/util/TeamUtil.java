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

    //Same as above but just to fetch the team color, returning an optional instead of an int
    @Nullable
    private static final Method TEAM_GET_COLOR_OPTIONAL;

    static {
        // Newer BungeeCord versions changed Team.setColor to accept Optional<Integer>.
        TEAM_SET_COLOR_OPTIONAL = initSetColorMethod();
        TEAM_GET_COLOR_OPTIONAL = initGetColorMethod();
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

    @SuppressWarnings("unchecked")
    public static int getColor(Team team) {
        if (TEAM_GET_COLOR_OPTIONAL != null) {
            try {
                Optional<Integer> colorOpt = (Optional<Integer>) TEAM_GET_COLOR_OPTIONAL.invoke(team);
                return colorOpt.orElse(0);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to set team color", e);
            }
        }
        return team.getColor();
    }

    private static @Nullable Method initGetColorMethod() {
        try {
            Method m = Team.class.getMethod("getColor");
            return m.getReturnType() == Optional.class ? m : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static @Nullable Method initSetColorMethod() {
        try {
            return Team.class.getMethod("setColor", Optional.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
