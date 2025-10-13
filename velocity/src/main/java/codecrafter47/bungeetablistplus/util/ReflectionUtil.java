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

import codecrafter47.bungeetablistplus.protocol.Team;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.StateRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import static com.velocitypowered.api.network.ProtocolVersion.*;

public class ReflectionUtil {

    @SuppressWarnings("unchecked")
    public static <T> T getField(Class<?> clazz, Object instance, String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return (T) f.get(instance);
    }

    public static <T> T getField(Class<?> clazz, Object instance, String field, int tries) throws NoSuchFieldException, IllegalAccessException {
        while (--tries > 0) {
            try {
                return getField(clazz, instance, field);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return getField(clazz, instance, field);
    }

    // Five from Velocity team said this to adding the Team packet
    // "Most instances won't need this feature so why should we weigh them down with baggage that's entirely optional?"
    public static void injectTeamPacketRegistry() {
        int tries = 5;
        while (--tries > 0) {
            try {
                StateRegistry.PacketRegistry clientbound = getField(StateRegistry.class, StateRegistry.PLAY, "clientbound", tries);

                Method register = StateRegistry.PacketRegistry.class.getDeclaredMethod("register", Class.class, Supplier.class, StateRegistry.PacketMapping[].class);
                register.setAccessible(true);

                Constructor<?> packetMapping = StateRegistry.PacketMapping.class.getDeclaredConstructor(int.class, ProtocolVersion.class, ProtocolVersion.class, boolean.class);
                packetMapping.setAccessible(true);

                register.invoke(clientbound, Team.class, (Supplier<?>) Team::new, new StateRegistry.PacketMapping[]{
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x3E, MINECRAFT_1_8, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x41, MINECRAFT_1_9, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x43, MINECRAFT_1_12, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x44, MINECRAFT_1_12_1, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x47, MINECRAFT_1_13, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x4B, MINECRAFT_1_14, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x4C, MINECRAFT_1_15, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x55, MINECRAFT_1_17, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x58, MINECRAFT_1_19_1, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x56, MINECRAFT_1_19_3, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x5A, MINECRAFT_1_19_4, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x5C, MINECRAFT_1_20_2, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x5E, MINECRAFT_1_20_3, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x60, MINECRAFT_1_20_5, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x67, MINECRAFT_1_21_2, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x66, MINECRAFT_1_21_5, null, false),
                        (StateRegistry.PacketMapping) packetMapping.newInstance(0x6B, MINECRAFT_1_21_9, null, false)
                });
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
