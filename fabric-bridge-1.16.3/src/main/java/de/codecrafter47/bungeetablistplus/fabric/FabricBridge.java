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

package de.codecrafter47.bungeetablistplus.fabric;

import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import de.codecrafter47.bungeetablistplus.bridge.AbstractBridge;
import de.codecrafter47.bungeetablistplus.fabric.event.PlayerDisconnectCallback;
import de.codecrafter47.bungeetablistplus.fabric.event.PlayerJoinCallback;
import de.codecrafter47.data.api.AbstractDataAccess;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyRegistry;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FabricBridge implements ModInitializer {

    private static final ScheduledExecutorService asyncScheduler = Executors.newScheduledThreadPool(2);

    private static final TypeAdapterRegistry typeRegistry = TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS;

    private static final DataKeyRegistry keyRegistry = DataKeyRegistry.of(
            MinecraftData.class,
            BukkitData.class,
            BTLPDataKeys.class);

    private static final Logger logger = LogManager.getLogger("BTLPFabricBridge");

    MyBridge bridge;

    public int monitorInterval = 40;
    public long prevtime;

    public double elapsedtimesec;
    public long elapsedtime;
    public double tps = 0;


    private void onStart(MinecraftServer server) {
        bridge = new MyBridge(server);

        prevtime = System.currentTimeMillis();
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);

        ServerSidePacketRegistry.INSTANCE.register(Identifier.tryParse(BridgeProtocolConstants.CHANNEL), (context, buffer) -> {
            byte[] data = new byte[buffer.readableBytes()];
            int readerIndex = buffer.readerIndex();
            buffer.getBytes(readerIndex, data);
            onPluginMessage(context.getPlayer(), data);
        });
        PlayerJoinCallback.EVENT.register(player -> bridge.onPlayerConnect(player));
        PlayerDisconnectCallback.EVENT.register(player -> bridge.onPlayerDisconnect(player));

        bridge.setPlayerDataAccess(new PlayerDataAccess());
        bridge.setServerDataAccess(new ServerDataAccess());

        asyncScheduler.scheduleWithFixedDelay(() -> {
            try {
                bridge.sendIntroducePackets();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);

        asyncScheduler.scheduleWithFixedDelay(() -> {
            try {
                bridge.updateData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 500, 500, TimeUnit.MILLISECONDS);

        asyncScheduler.scheduleWithFixedDelay(() -> {
            try {
                bridge.resendUnconfirmedMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void onStop(MinecraftServer server) {
        asyncScheduler.shutdownNow();
    }

    private void onTick(MinecraftServer server) {
        int ticks = server.getTicks();
        if (ticks % monitorInterval == 0) {
            long time = System.currentTimeMillis();
            elapsedtime = time - prevtime;
            elapsedtimesec = (double) elapsedtime / 1000;
            tps = monitorInterval / elapsedtimesec;
            prevtime = time;
        }
    }

    @SneakyThrows
    public void onPluginMessage(PlayerEntity player, byte[] data) {
        DataInput input = new DataInputStream(new ByteArrayInputStream(data));
        bridge.onMessage((ServerPlayerEntity) player, input);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onStop);
    }

    private class ServerDataAccess extends AbstractDataAccess<MinecraftServer> {

        public ServerDataAccess() {
            super();
            addProvider(MinecraftData.MinecraftVersion, MinecraftServer::getVersion);
            addProvider(MinecraftData.ServerModName, MinecraftServer::getServerModName);
            addProvider(MinecraftData.TPS, v -> tps);
            addProvider(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES, v -> Collections.emptyList());
            addProvider(BTLPDataKeys.REGISTERED_THIRD_PARTY_SERVER_VARIABLES, v -> Collections.emptyList());
            addProvider(BTLPDataKeys.PLACEHOLDERAPI_PRESENT, v -> false);
        }

        @Nullable
        @Override
        public <V> V get(DataKey<V> key, MinecraftServer context) {
            try {
                return super.get(key, context);
            } catch (Throwable th) {
                logger.warn("Failed to acquire data " + key + " from " + context, th);
            }
            return null;
        }
    }

    private static class PlayerDataAccess extends AbstractDataAccess<ServerPlayerEntity> {

        public PlayerDataAccess() {
            super();
            addProvider(MinecraftData.Health, player -> (double) player.getHealth());
            addProvider(MinecraftData.Level, player -> player.experienceLevel);
            addProvider(MinecraftData.MaxHealth, player -> (double) player.getMaxHealth());
            addProvider(MinecraftData.XP, player -> player.experienceProgress * player.getNextLevelExperience());
            addProvider(MinecraftData.TotalXP, player -> player.totalExperience);
            addProvider(MinecraftData.PosX, Entity::getX);
            addProvider(MinecraftData.PosY, Entity::getY);
            addProvider(MinecraftData.PosZ, Entity::getZ);
            addProvider(MinecraftData.Team, player -> {
                AbstractTeam team = player.getScoreboardTeam();
                return team != null ? team.getName() : null;
            });
            addProvider(MinecraftData.DisplayName, player -> player.getDisplayName().getString());
        }

        @Nullable
        @Override
        public <V> V get(DataKey<V> key, ServerPlayerEntity context) {
            try {
                return super.get(key, context);
            } catch (Throwable th) {
                logger.warn("Failed to acquire data " + key + " from " + context, th);
            }
            return null;
        }
    }


    private static class MyBridge extends AbstractBridge<ServerPlayerEntity, MinecraftServer> {

        public MyBridge(MinecraftServer server) {
            super(keyRegistry, typeRegistry, "dummy", server);
        }

        @Override
        protected void sendMessage(@Nonnull ServerPlayerEntity player, @Nonnull byte[] bytes) {
            if (player.networkHandler != null) {
                CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(Identifier.tryParse(BridgeProtocolConstants.CHANNEL), new PacketByteBuf(Unpooled.wrappedBuffer(bytes)));
                player.networkHandler.sendPacket(packet);
            }
        }

        @Override
        protected void runAsync(@Nonnull Runnable runnable) {
            asyncScheduler.submit(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
