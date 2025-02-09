/*
 * Copyright (C) 2018-2023 Velocity Contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.protocol;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Team implements MinecraftPacket {

    public static final byte CREATE = 0;
    public static final byte REMOVE = 1;
    public static final byte UPDATE_INFO = 2;
    public static final byte ADD_PLAYER = 3;
    public static final byte REMOVE_PLAYER = 4;

    private String name;
    /**
     * 0 - create, 1 remove, 2 info update, 3 player add, 4 player remove.
     */
    private byte mode;
    private ComponentHolder displayName;
    private ComponentHolder prefix;
    private ComponentHolder suffix;
    private NameTagVisibility nameTagVisibility;
    private CollisionRule collisionRule;
    private int color;
    private byte friendlyFire;
    private String[] players;

    // placeholder until release
    private int MINECRAFT_1_21_5 = 770;

    /**
     * Packet to destroy a team.
     *
     * @param name team name
     */
    public Team(String name)
    {
        this.name = name;
        this.mode = 1;
    }

    @Override
    public void decode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion version) {
        name = ProtocolUtils.readString(buf);
        mode = buf.readByte();
        if (mode == CREATE || mode == UPDATE_INFO) {
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
                nameTagVisibility =readStringMapKey( buf, NameTagVisibility.BY_NAME );
                if (version.compareTo(ProtocolVersion.MINECRAFT_1_9) >= 0) {
                    collisionRule = readStringMapKey( buf, CollisionRule.BY_NAME );
                }
            }
            color = (version.compareTo(ProtocolVersion.MINECRAFT_1_13) >= 0) ? ProtocolUtils.readVarInt(buf) : buf.readByte();
            if (version.compareTo(ProtocolVersion.MINECRAFT_1_13) >= 0) {
                prefix = ComponentHolder.read(buf, version);
                suffix = ComponentHolder.read(buf, version);
            }
        }
        if (mode == CREATE || mode == ADD_PLAYER || mode == REMOVE_PLAYER) {
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
        buf.writeByte(mode);
        if (mode == CREATE || mode == UPDATE_INFO) {
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
        if (mode == CREATE || mode == ADD_PLAYER || mode == REMOVE_PLAYER) {
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
        // 1.9 (and possibly other versions) appear to treat unknown values differently (always render rather than subject to spectator mode, friendly invisibles, etc).
        // we allow the empty value to achieve this in case it is potentially useful even though this is unsupported and its usage may be a bug (#3780).
        UNKNOWN( "" );
        //
        private final String key;
        //
        private static final Map<String, NameTagVisibility> BY_NAME;
        private static final NameTagVisibility[] BY_ID;

        static {
            NameTagVisibility[] values = NameTagVisibility.values();
            ImmutableMap.Builder<String, NameTagVisibility> builder = ImmutableMap.builderWithExpectedSize(values.length);

            for (NameTagVisibility e : values) {
                builder.put(e.key, e);
            }

            BY_NAME = builder.build();
            BY_ID = Arrays.copyOf( values, values.length - 1 ); // Ignore dummy UNKNOWN value
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
            ImmutableMap.Builder<String, CollisionRule> builder = ImmutableMap.builderWithExpectedSize( values.length );

            for (CollisionRule e : values) {
                builder.put(e.key, e);
            }

            BY_NAME = builder.build();
        }
    }

    public static <T> T readStringMapKey(ByteBuf buf, Map<String, T> map) {
        String string = ProtocolUtils.readString( buf );
        T result = map.get( string );
        Preconditions.checkArgument( result != null, "Unknown string key %s", string );

        return result;
    }
}
