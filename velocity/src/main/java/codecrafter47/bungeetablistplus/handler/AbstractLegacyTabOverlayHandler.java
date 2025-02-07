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

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.protocol.Team;
import codecrafter47.bungeetablistplus.util.ColorParser;
import codecrafter47.bungeetablistplus.util.ConcurrentBitSet;
import codecrafter47.bungeetablistplus.util.Property119Handler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooterPacket;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfoPacket;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfoPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.config.misc.ChatFormat;
import de.codecrafter47.taboverlay.config.misc.Unchecked;
import de.codecrafter47.taboverlay.handler.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.val;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link de.codecrafter47.taboverlay.handler.TabOverlayHandler} for pre 1.8 Minecraft Versions.
 */
public abstract class AbstractLegacyTabOverlayHandler implements PacketHandler, TabOverlayHandler {


    private static final String[] slotID;
    private static final UUID[] slotUUID;
    private static final Set<String> slotIDSet = new HashSet<>();

    private static final Int2ObjectMap<Collection<RectangularTabOverlay.Dimension>> playerListSizeToSupportedSizesMap = new Int2ObjectOpenHashMap<>();


    static {

        // add a random character to the player and team names to prevent issues in multi-velocity setup (stupid!).
        int random = ThreadLocalRandom.current().nextInt(0x1e00, 0x2c00);

        slotID = new String[256];
        slotUUID = new UUID[256];

        for (int i = 0; i < 256; i++) {
            String hex = String.format("%02x", i);
            slotID[i] = String.format("§B§T§L§P§%c§%c§%c§r", random, hex.charAt(0), hex.charAt(1));
            slotIDSet.add(slotID[i]);
            slotUUID[i] = UUID.randomUUID();
        }
    }

    private static final String[][] EMPTY_PROPERTIES = new String[0][];
    private static final String EMPTY_JSON_TEXT = "{\"text\":\"\"}";

    private static Collection<RectangularTabOverlay.Dimension> getSupportedSizesByPlayerListSize(int playerListSize) {
        Preconditions.checkArgument(playerListSize >= 0, "playerListSize is negative");
        synchronized (playerListSizeToSupportedSizesMap) {
            Collection<RectangularTabOverlay.Dimension> collection = playerListSizeToSupportedSizesMap.get(playerListSize);
            if (collection != null) {
                return collection;
            }
            val builder = ImmutableSet.<RectangularTabOverlay.Dimension>builder();
            if (playerListSize == 0) {
                builder.add(new RectangularTabOverlay.Dimension(1, 0));
            } else {
                int columns = (playerListSize + 19) / 20;
                for (int rows = 0; rows <= 20; rows++) {
                    builder.add(new RectangularTabOverlay.Dimension(columns, rows));
                }
            }
            collection = builder.build();
            playerListSizeToSupportedSizesMap.put(playerListSize, collection);
            return collection;
        }
    }

    protected final Logger logger;
    private final int playerListSize;
    private final Executor eventLoopExecutor;

    private final Object2IntMap<String> serverPlayerList = new Object2IntLinkedOpenHashMap<>();
    private final Object2ObjectMap<UUID, ModernPlayerListEntry> modernServerPlayerList = new Object2ObjectOpenHashMap<>();

    private boolean is13OrLater;

    private final Queue<AbstractContentOperationModeHandler<?>> nextActiveHandlerQueue = new ConcurrentLinkedQueue<>();
    private AbstractContentOperationModeHandler<?> activeHandler;

    private final AtomicBoolean updateScheduledFlag = new AtomicBoolean(false);
    private final Runnable updateTask = this::update;

    AbstractLegacyTabOverlayHandler(Logger logger, int playerListSize, Executor eventLoopExecutor, boolean is13OrLater) {
        this.logger = logger;
        this.eventLoopExecutor = eventLoopExecutor;
        this.is13OrLater = is13OrLater;
        Preconditions.checkElementIndex(playerListSize, 256, "playerListSize");
        this.playerListSize = playerListSize;
        this.activeHandler = new PassThroughHandlerContent();
    }

    protected abstract void sendPacket(MinecraftPacket packet);

    @Override
    public PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet) {
        if (packet.getAction() == LegacyPlayerListItemPacket.ADD_PLAYER) {
            for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                if (item.getUuid() != null) {
                    modernServerPlayerList.put(item.getUuid(), new ModernPlayerListEntry(item.getName(), item.getLatency(), item.getGameMode()));
                } else {
                    serverPlayerList.put(getName(item), item.getLatency());
                }
            }
        } else {
            for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                if (item.getUuid() != null) {
                    modernServerPlayerList.remove(item.getUuid());
                } else {
                    serverPlayerList.removeInt(getName(item));
                }
            }
        }
        return activeHandler.onPlayerListPacket(packet);
    }

    @Override
    public PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfoPacket packet) {
        if (packet.getActions().contains(UpsertPlayerInfoPacket.Action.ADD_PLAYER)) {
            for (UpsertPlayerInfoPacket.Entry entry : packet.getEntries()) {
                if (entry.getProfileId() != null) {
                    modernServerPlayerList.put(entry.getProfileId(), new ModernPlayerListEntry(entry.getProfile().getName(), entry.getLatency(), entry.getGameMode()));
                } else {
                    serverPlayerList.put(getName(entry), entry.getLatency());
                }
            }
        }
        return activeHandler.onPlayerListUpdatePacket(packet);
    }

    @Override
    public PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfoPacket packet) {
        for (UUID uuid : packet.getProfilesToRemove()) {
            modernServerPlayerList.remove(uuid);
        }

        return activeHandler.onPlayerListRemovePacket(packet);
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        if (slotIDSet.contains(packet.getName())) {
            logger.log(Level.WARNING, "Team name collision, using multi-velocity setup? Packet: {0}", packet);
            return PacketListenerResult.CANCEL;
        }
        return PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onPlayerListHeaderFooterPacket(HeaderAndFooterPacket packet) {
        logger.log(Level.WARNING, "1.7 players should not receive tab list header/ footer");
        return PacketListenerResult.CANCEL;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        this.is13OrLater = is13OrLater;
        this.activeHandler.onServerSwitch();
        serverPlayerList.clear();
        modernServerPlayerList.clear();
    }

    @Override
    public <R> R enterContentOperationMode(ContentOperationMode<R> operationMode) {
        AbstractContentOperationModeHandler<?> handler;
        if (operationMode == ContentOperationMode.PASS_TROUGH) {
            handler = new PassThroughHandlerContent();
        } else if (operationMode == ContentOperationMode.SIMPLE) {
            handler = new SimpleContentOperationModeHandler();
        } else if (operationMode == ContentOperationMode.RECTANGULAR) {
            handler = new RectangularSizeHandlerContent();
        } else {
            throw new AssertionError("Missing operation mode handler for " + operationMode.getName());
        }
        nextActiveHandlerQueue.add(handler);
        scheduleUpdate();
        return Unchecked.cast(handler.getTabOverlay());
    }

    @Override
    public <R> R enterHeaderAndFooterOperationMode(HeaderAndFooterOperationMode<R> operationMode) {
        return Unchecked.cast(DummyHeaderFooterHandle.INSTANCE);
    }

    private void scheduleUpdate() {
        if (this.updateScheduledFlag.compareAndSet(false, true)) {
            try {
                eventLoopExecutor.execute(updateTask);
            } catch (RejectedExecutionException ignored) {
            }
        }
    }

    private void update() {
        this.updateScheduledFlag.set(false);
        AbstractContentOperationModeHandler<?> handler;
        while (null != (handler = nextActiveHandlerQueue.poll())) {
            this.activeHandler.invalidate();
            this.activeHandler = handler;
            this.activeHandler.onActivated();
        }
        this.activeHandler.update();
    }

    private void removeEntry(UUID uuid, String player) {
        LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(uuid);
        item.setName(player);
        item.setDisplayName(GsonComponentSerializer.gson().deserialize(player));
        item.setLatency(9999);
        LegacyPlayerListItemPacket pli = new LegacyPlayerListItemPacket(LegacyPlayerListItemPacket.REMOVE_PLAYER, List.of(item));
        sendPacket(pli);
    }

    private abstract static class AbstractContentOperationModeHandler<T extends AbstractTabOverlay> extends OperationModeHandler<T> {

        abstract PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet);

        abstract void onServerSwitch();

        abstract void update();

        final void invalidate() {
            getTabOverlay().invalidate();
            onDeactivated();
        }

        abstract void onDeactivated();

        abstract void onActivated();

        public abstract PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfoPacket packet);

        public abstract PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfoPacket packet);
    }

    private abstract static class AbstractTabOverlay implements TabOverlayHandle {
        private boolean valid = true;

        @Override
        public boolean isValid() {
            return valid;
        }

        final void invalidate() {
            valid = false;
        }
    }

    private class PassThroughHandlerContent extends AbstractContentOperationModeHandler<PassThroughTabOverlay> {

        @Override
        PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        public PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfoPacket packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        public PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfoPacket packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        void onActivated() {
            for (val entry : serverPlayerList.object2IntEntrySet()) {
                LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item();
                item.setDisplayName(GsonComponentSerializer.gson().deserialize(entry.getKey())); // TODO: Check Formatting
                item.setLatency(entry.getIntValue());
                LegacyPlayerListItemPacket pli = new LegacyPlayerListItemPacket(LegacyPlayerListItemPacket.ADD_PLAYER, List.of(item));
                sendPacket(pli);
            }
            for (val entry : modernServerPlayerList.entrySet()) {
                LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(entry.getKey());
                item.setName(entry.getValue().name);
                item.setGameMode(entry.getValue().gamemode);
                item.setLatency(entry.getValue().latency);
                Property119Handler.setProperties(item, EMPTY_PROPERTIES);
                LegacyPlayerListItemPacket pli = new LegacyPlayerListItemPacket(LegacyPlayerListItemPacket.ADD_PLAYER, List.of(item));
                sendPacket(pli);
            }
        }

        @Override
        void onDeactivated() {
            removeAllEntries();
        }

        private void removeAllEntries() {
            for (String player : serverPlayerList.keySet()) {
                removeEntry(null, player);
            }
            for (UUID player : modernServerPlayerList.keySet()) {
                removeEntry(player, null);
            }
        }

        @Override
        public void onServerSwitch() {
            removeAllEntries();
        }

        @Override
        void update() {
            // nothing to do
        }

        @Override
        public PassThroughTabOverlay createTabOverlay() {
            return new PassThroughTabOverlay();
        }
    }

    private static final class PassThroughTabOverlay extends AbstractTabOverlay {

    }

    private abstract class CustomTabOverlayHandlerContent<T extends CustomTabOverlay> extends AbstractContentOperationModeHandler<T> {
        private final IntConsumer updateTextTask;
        private final IntConsumer updatePingTask;
        private int size = 0;

        private CustomTabOverlayHandlerContent() {
            this.updateTextTask = index -> updateText(getTabOverlay(), index);
            this.updatePingTask = index -> updatePing(getTabOverlay(), index);
        }

        @Override
        PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet) {
            return PacketListenerResult.CANCEL;
        }

        @Override
        public PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfoPacket packet) {
            return PacketListenerResult.CANCEL;
        }

        @Override
        public PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfoPacket packet) {
            return PacketListenerResult.CANCEL;
        }

        @Override
        void onServerSwitch() {
            // nothing to do
        }

        @Override
        void update() {
            CustomTabOverlayHandlerContent.this.updateSize();
            getTabOverlay().dirtyFlagsText.iterateAndClear(updateTextTask);
            getTabOverlay().dirtyFlagsPing.iterateAndClear(updatePingTask);
        }

        @Override
        void onActivated() {
            // nothing to do
        }

        @Override
        void onDeactivated() {
            for (int index = this.size - 1; index >= 0; index--) {
                removeEntry(slotUUID[index], slotID[index]);
                Team t = new Team();
                t.setName(slotID[index]);
                t.setMode((byte) 1);
                sendPacket(t);
            }
        }

        private void updateSize() {
            CustomTabOverlay tabOverlay = getTabOverlay();
            int size = tabOverlay.size;
            if (size != this.size) {
                if (size > this.size) {
                    for (int index = this.size; index < size; index++) {
                        tabOverlay.dirtyFlagsText.clear(index);
                        tabOverlay.dirtyFlagsPing.clear(index);
                        // create new slot
                        updateSlot(tabOverlay, index);
                        Team t = new Team();
                        t.setName(slotID[index]);
                        t.setMode((byte) 0);
                        t.setPrefix(new ComponentHolder(is13OrLater ? ProtocolVersion.MINECRAFT_1_13 : ProtocolVersion.MINECRAFT_1_12_2, tabOverlay.text0[index]));
                        t.setDisplayName(new ComponentHolder(is13OrLater ? ProtocolVersion.MINECRAFT_1_13 : ProtocolVersion.MINECRAFT_1_12_2, ""));
                        t.setSuffix(new ComponentHolder(is13OrLater ? ProtocolVersion.MINECRAFT_1_13 : ProtocolVersion.MINECRAFT_1_12_2, tabOverlay.text1[index]));
                        t.setPlayers(new String[]{slotID[index]});
                        t.setNameTagVisibility(Team.NameTagVisibility.ALWAYS);
                        t.setCollisionRule(Team.CollisionRule.ALWAYS);
                        sendPacket(t);
                    }
                } else {
                    for (int index = this.size - 1; index >= size; index--) {
                        removeEntry(slotUUID[index], slotID[index]);
                        Team t = new Team();
                        t.setName(slotID[index]);
                        t.setMode((byte) 1);
                        sendPacket(t);
                    }
                }
                this.size = size;
            }
        }

        private void updateSlot(CustomTabOverlay tabOverlay, int index) {
            LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUUID[index]);
            item.setName(slotID[index]);
            Property119Handler.setProperties(item, EMPTY_PROPERTIES);
            item.setDisplayName(GsonComponentSerializer.gson().deserialize(slotID[index])); // TODO: Check Formatting
            item.setLatency(tabOverlay.ping[index]);
            LegacyPlayerListItemPacket pli = new LegacyPlayerListItemPacket(LegacyPlayerListItemPacket.ADD_PLAYER, List.of(item));
            sendPacket(pli);
        }

        private void updateText(CustomTabOverlay tabOverlay, int index) {
            if (index < size) {
                Team packet = new Team();
                packet.setName(slotID[index]);
                packet.setMode((byte) 2);
                packet.setPrefix(new ComponentHolder(is13OrLater ? ProtocolVersion.MINECRAFT_1_13 : ProtocolVersion.MINECRAFT_1_12_2, tabOverlay.text0[index]));
                packet.setDisplayName(new ComponentHolder(is13OrLater ? ProtocolVersion.MINECRAFT_1_13 : ProtocolVersion.MINECRAFT_1_12_2, ""));
                packet.setSuffix(new ComponentHolder(is13OrLater ? ProtocolVersion.MINECRAFT_1_13 : ProtocolVersion.MINECRAFT_1_12_2, tabOverlay.text1[index]));
                packet.setNameTagVisibility(Team.NameTagVisibility.ALWAYS);
                packet.setCollisionRule(Team.CollisionRule.ALWAYS);
                sendPacket(packet);
            }
        }

        private void updatePing(CustomTabOverlay tabOverlay, int index) {
            if (index < size) {
                updateSlot(tabOverlay, index);
            }
        }
    }

    private abstract class CustomTabOverlay extends AbstractTabOverlay implements TabOverlayHandle.BatchModifiable {

        final String[] text0;
        final String[] text1;
        final int[] ping;
        final AtomicInteger batchUpdateRecursionLevel;
        final ConcurrentBitSet dirtyFlagsText;
        final ConcurrentBitSet dirtyFlagsPing;

        protected int size;

        private CustomTabOverlay() {
            this.batchUpdateRecursionLevel = new AtomicInteger(0);
            this.text0 = new String[playerListSize];
            this.text1 = new String[playerListSize];
            this.ping = new int[playerListSize];
            this.dirtyFlagsText = new ConcurrentBitSet(playerListSize);
            this.dirtyFlagsPing = new ConcurrentBitSet(playerListSize);
            this.size = 0;
        }

        @Override
        public void beginBatchModification() {
            if (isValid()) {
                if (batchUpdateRecursionLevel.incrementAndGet() < 0) {
                    throw new AssertionError("Recursion level overflow");
                }
            }
        }

        @Override
        public void completeBatchModification() {
            if (isValid()) {
                int level = batchUpdateRecursionLevel.decrementAndGet();
                if (level == 0) {
                    scheduleUpdate();
                } else if (level < 0) {
                    throw new AssertionError("Recursion level underflow");
                }
            }
        }

        private void scheduleUpdateIfNotInBatch() {
            if (batchUpdateRecursionLevel.get() == 0) {
                scheduleUpdate();
            }
        }

        void setTextInternal(int index, String text) {
            // convert to legacy format
            text = ChatFormat.formattedTextToLegacy(text);

            // split string into two parts of at most 16 characters each to be displayed as prefix and suffix
            String text0, text1;
            if (text.length() <= 16) {
                text0 = text;
                text1 = "";
            } else {
                int end = text.charAt(15) == LegacyComponentSerializer.SECTION_CHAR ? 15 : 16;
                text0 = text.substring(0, end);
                int start = ColorParser.endofColor(text, end);
                String colors = ColorParser.extractColorCodes(text.substring(0, start));
                end = start + 16 - colors.length();
                if (end >= text.length()) {
                    end = text.length();
                }
                text1 = colors + text.substring(start, end);
            }

            if (!text0.equals(this.text0[index]) || !text1.equals(this.text1[index])) {
                this.text0[index] = text0;
                this.text1[index] = text1;
                dirtyFlagsText.set(index);
                scheduleUpdateIfNotInBatch();
            }
        }

        void setPingInternal(int index, int ping) {
            if (ping != this.ping[index]) {
                this.ping[index] = ping;
                dirtyFlagsPing.set(index);
                scheduleUpdateIfNotInBatch();
            }
        }

        void setSizeInternal(int newSize) {
            int oldSize = this.size;
            if (newSize > oldSize) {
                for (int index = oldSize; index < newSize; index++) {
                    text0[index] = "";
                    text1[index] = "";
                    ping[index] = 0;
                }
            }
            this.size = newSize;
            if (newSize < oldSize) {
                for (int index = oldSize - 1; index >= newSize; index--) {
                    text0[index] = "";
                    text1[index] = "";
                    ping[index] = 0;
                }
            }
            scheduleUpdateIfNotInBatch();
        }
    }

    private class RectangularSizeHandlerContent extends CustomTabOverlayHandlerContent<RectangularTabOverlayImpl> {

        @Override
        public RectangularTabOverlayImpl createTabOverlay() {
            return new RectangularTabOverlayImpl();
        }
    }

    private final class RectangularTabOverlayImpl extends CustomTabOverlay implements RectangularTabOverlay {

        private final Collection<Dimension> supportedSizes;

        private Dimension sizeAsDimension;

        private RectangularTabOverlayImpl() {
            super();
            this.supportedSizes = getSupportedSizesByPlayerListSize(playerListSize);
            Optional<Dimension> dimensionZero = supportedSizes.stream().filter(size -> size.getSize() == 0).findAny();
            if (!dimensionZero.isPresent()) {
                throw new AssertionError();
            }
            this.sizeAsDimension = dimensionZero.get();
        }

        @Override
        public Dimension getSize() {
            return sizeAsDimension;
        }

        @Override
        public Collection<Dimension> getSupportedSizes() {
            return supportedSizes;
        }

        @Override
        public void setSize(@Nonnull Dimension size) {
            if (!getSupportedSizes().contains(size)) {
                throw new IllegalArgumentException("Unsupported size " + size);
            }
            if (isValid() && !this.sizeAsDimension.equals(size)) {
                this.sizeAsDimension = size;
                setSizeInternal(size.getSize());
            }
        }

        @Override
        public void setSlot(int column, int row, @Nullable UUID uuid, @Nonnull Icon icon, @Nonnull String text, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, sizeAsDimension.getColumns(), "column");
                Preconditions.checkElementIndex(row, sizeAsDimension.getRows(), "row");
                int index = row * sizeAsDimension.getColumns() + column;

                beginBatchModification();
                try {
                    setTextInternal(index, text);
                    setPingInternal(index, ping);
                } finally {
                    completeBatchModification();
                }
            }
        }

        @Override
        public void setUuid(int column, int row, UUID uuid) {
            // nothing to do - not supported in 1.7
        }

        @Override
        public void setIcon(int column, int row, @Nonnull Icon icon) {
            // nothing to do - not supported in 1.7
        }

        @Override
        public void setText(int column, int row, @Nonnull String text) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, sizeAsDimension.getColumns(), "column");
                Preconditions.checkElementIndex(row, sizeAsDimension.getRows(), "row");
                int index = row * sizeAsDimension.getColumns() + column;
                setTextInternal(index, text);
            }
        }

        @Override
        public void setPing(int column, int row, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, sizeAsDimension.getColumns(), "column");
                Preconditions.checkElementIndex(row, sizeAsDimension.getRows(), "row");
                int index = row * sizeAsDimension.getColumns() + column;
                setPingInternal(index, ping);
            }
        }

    }

    private class SimpleContentOperationModeHandler extends CustomTabOverlayHandlerContent<SimpleTabOverlayImpl> {

        @Override
        public SimpleTabOverlayImpl createTabOverlay() {
            return new SimpleTabOverlayImpl();
        }
    }

    private final class SimpleTabOverlayImpl extends CustomTabOverlay implements SimpleTabOverlay {

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public int getMaxSize() {
            return playerListSize;
        }

        @Override
        public void setSize(int size) {
            if (size < 0 || size > playerListSize) {
                throw new IllegalArgumentException("Unsupported size " + size);
            }
            if (isValid() && this.size != size) {
                setSizeInternal(size);
            }
        }

        @Override
        public void setSlot(int index, @Nullable UUID uuid, @Nonnull Icon icon, @Nonnull String text, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size);

                beginBatchModification();
                try {
                    setTextInternal(index, text);
                    setPingInternal(index, ping);
                } finally {
                    completeBatchModification();
                }
            }
        }

        @Override
        public void setSlot(int index, @Nonnull Icon icon, @Nonnull String text, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size);

                beginBatchModification();
                try {
                    setTextInternal(index, text);
                    setPingInternal(index, ping);
                } finally {
                    completeBatchModification();
                }
            }
        }

        @Override
        public void setUuid(int index, UUID uuid) {
            // nothing to do - not supported in 1.7
        }

        @Override
        public void setIcon(int index, @Nonnull Icon icon) {
            // nothing to do - not supported in 1.7
        }

        @Override
        public void setText(int index, @Nonnull String text) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size);
                setTextInternal(index, text);
            }
        }

        @Override
        public void setPing(int index, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size);
                setPingInternal(index, ping);
            }
        }

    }

    private static class DummyHeaderFooterHandle implements HeaderAndFooterHandle {
        private static final DummyHeaderFooterHandle INSTANCE = new DummyHeaderFooterHandle();

        @Override
        public void setHeaderFooter(@Nullable String header, @Nullable String footer) {
            // dummy
        }

        @Override
        public void setHeader(@Nullable String header) {
            // dummy
        }

        @Override
        public void setFooter(@Nullable String footer) {
            // dummy
        }

        @Override
        public void beginBatchModification() {
            // dummy
        }

        @Override
        public void completeBatchModification() {
            // dummy
        }

        @Override
        public boolean isValid() {
            // dummy
            return true;
        }
    }

    /**
     * Utility method to get the name from an {@link LegacyPlayerListItemPacket.Item}.
     *
     * @param item the item
     * @return the name
     */
    private static String getName(LegacyPlayerListItemPacket.Item item) {
        if (item.getDisplayName() != null) {
            return LegacyComponentSerializer.legacySection().serialize(item.getDisplayName());
        } else if (item.getName() != null) {
            return item.getName();
        } else {
            throw new AssertionError("DisplayName and Username are null");
        }
    }

    private static String getName(UpsertPlayerInfoPacket.Entry entry) {
        if (entry.getDisplayName() != null) {
            return entry.getDisplayName().getJson();
        } else if (entry.getProfile().getName() != null) {
            return entry.getProfile().getName();
        } else {
            throw new AssertionError("DisplayName and Username are null");
        }
    }

    @AllArgsConstructor
    private static class ModernPlayerListEntry {
        String name;
        int latency;
        int gamemode;
    }
}
