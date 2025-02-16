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

package codecrafter47.bungeetablistplus.protocol;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Team implements MinecraftPacket {

    public enum Mode {
        CREATE,
        REMOVE,
        UPDATE_INFO,
        ADD_PLAYER,
        REMOVE_PLAYER
    }

    private String name;
    private Mode mode;
    private ComponentHolder displayName;
    private ComponentHolder prefix;
    private ComponentHolder suffix;
    private NameTagVisibility nameTagVisibility;
    private CollisionRule collisionRule;
    private int color;
    private byte friendlyFire;
    private String[] players;

    // TODO: placeholder until release
    private int MINECRAFT_1_21_5 = 770;

    public Team(String name)
    {
        this.name = name;
        this.mode = Mode.REMOVE;
    }

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        name = ProtocolUtils.readString(buf);
        mode = Mode.values()[buf.readByte()];
        if (mode == Mode.CREATE || mode == Mode.UPDATE_INFO) {
            displayName = ComponentHolder.read(buf, version);
            if (version.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
                prefix = ComponentHolder.read(buf, version);
                suffix = ComponentHolder.read(buf, version);
            }
            friendlyFire = buf.readByte();
            // TODO: Replace this when released
            if (version.getProtocol() >= MINECRAFT_1_21_5) {
                nameTagVisibility = NameTagVisibility.BY_ID[ProtocolUtils.readVarInt( buf )];
                collisionRule = CollisionRule.BY_ID[ProtocolUtils.readVarInt( buf )];
            } else {
                nameTagVisibility = readStringToMap( buf, NameTagVisibility.BY_NAME );
                if (version.compareTo(ProtocolVersion.MINECRAFT_1_9) >= 0) {
                    collisionRule = readStringToMap( buf, CollisionRule.BY_NAME );
                }
            }
            color = (version.compareTo(ProtocolVersion.MINECRAFT_1_13) >= 0) ? ProtocolUtils.readVarInt(buf) : buf.readByte();
            if (version.compareTo(ProtocolVersion.MINECRAFT_1_13) >= 0) {
                prefix = ComponentHolder.read(buf, version);
                suffix = ComponentHolder.read(buf, version);
            }
        }
        if (mode == Mode.CREATE || mode == Mode.ADD_PLAYER || mode == Mode.REMOVE_PLAYER) {
            int len = ProtocolUtils.readVarInt(buf);
            players = new String[len];
            for (int i = 0; i < len; i++) {
                players[i] = ProtocolUtils.readString(buf);
            }
        }
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        ProtocolUtils.writeString(buf, name);
        buf.writeByte(mode.ordinal());
        if (mode == Mode.CREATE || mode == Mode.UPDATE_INFO) {
            displayName.write(buf);
            if (version.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
                prefix.write(buf);
                suffix.write(buf);
            }
            buf.writeByte(friendlyFire);
            // TODO: Replace this when released
            if (version.getProtocol() >= MINECRAFT_1_21_5) {
                ProtocolUtils.writeVarInt(buf, nameTagVisibility.ordinal());
                ProtocolUtils.writeVarInt(buf, collisionRule.ordinal());
            } else {
                ProtocolUtils.writeString(buf, nameTagVisibility.getKey());
                if (version.compareTo(ProtocolVersion.MINECRAFT_1_9) >= 0) {
                    ProtocolUtils.writeString(buf, collisionRule.getKey());
                }
            }
            if (version.compareTo(ProtocolVersion.MINECRAFT_1_13) >= 0) {
                ProtocolUtils.writeVarInt(buf, color);
                prefix.write(buf);
                suffix.write(buf);
            } else {
                buf.writeByte(color);
            }
        }
        if (mode == Mode.CREATE || mode == Mode.ADD_PLAYER || mode == Mode.REMOVE_PLAYER) {
            ProtocolUtils.writeVarInt(buf, players.length);
            for (String player : players) {
                ProtocolUtils.writeString(buf, player);
            }
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return false;
    }

    @Getter
    @RequiredArgsConstructor
    public enum NameTagVisibility {

        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        UNKNOWN( "" );

        private final String key;
        private static final Map<String, NameTagVisibility> BY_NAME;
        private static final NameTagVisibility[] BY_ID;

        static {
            NameTagVisibility[] values = NameTagVisibility.values();
            BY_ID = Arrays.copyOf( values, values.length - 1 );
            BY_NAME = Arrays.stream(values).collect(Collectors.toUnmodifiableMap(e -> e.key, e -> e));
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum CollisionRule {

        ALWAYS("always"),
        NEVER("never"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam");
        //
        private final String key;
        //
        private static final Map<String, CollisionRule> BY_NAME;
        private static final CollisionRule[] BY_ID;

        static {
            CollisionRule[] values = BY_ID = CollisionRule.values();
            BY_NAME = Arrays.stream(values).collect(Collectors.toUnmodifiableMap(e -> e.key, e -> e));
        }
    }

    public static <T> T readStringToMap(ByteBuf buf, Map<String, T> map) {
        String string = ProtocolUtils.readString( buf );
        T result = map.get( string );
        Preconditions.checkArgument( result != null, "Unknown string key %s", string );

        return result;
    }
}
