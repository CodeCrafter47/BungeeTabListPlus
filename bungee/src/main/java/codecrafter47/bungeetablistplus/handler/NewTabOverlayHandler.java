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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.util.BitSet;
import codecrafter47.bungeetablistplus.util.ConcurrentBitSet;
import codecrafter47.bungeetablistplus.util.Property119Handler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.ProfileProperty;
import de.codecrafter47.taboverlay.config.misc.ChatFormat;
import de.codecrafter47.taboverlay.config.misc.Unchecked;
import de.codecrafter47.taboverlay.handler.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.*;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewTabOverlayHandler implements PacketHandler, TabOverlayHandler {

    // some options
    private static final boolean OPTION_ENABLE_CUSTOM_SLOT_USERNAME_COLLISION_CHECK = true;
    private static final boolean OPTION_ENABLE_CUSTOM_SLOT_UUID_COLLISION_CHECK = true;

    private static final BaseComponent EMPTY_TEXT_COMPONENT = new TextComponent();
    private static final Either<String, BaseComponent> EMPTY_EITHER_TEXT_COMPONENT = Either.right(new TextComponent());
    protected static final String[][] EMPTY_PROPERTIES_ARRAY = new String[0][];

    private static final ImmutableMap<RectangularTabOverlay.Dimension, BitSet> DIMENSION_TO_USED_SLOTS;
    private static final BitSet[] SIZE_TO_USED_SLOTS;

    private static final UUID[] CUSTOM_SLOT_UUID_STEVE;
    private static final UUID[] CUSTOM_SLOT_UUID_ALEX;
    @Nonnull
    private static final Set<UUID> CUSTOM_SLOT_UUIDS;
    private static final String[] CUSTOM_SLOT_USERNAME;
    private static final String[] CUSTOM_SLOT_USERNAME_SMILEYS;
    @Nonnull
    private static final Set<String> CUSTOM_SLOT_USERNAMES;
    private static final String[] CUSTOM_SLOT_TEAMNAME;

    static {

        // build the dimension to used slots map (for the rectangular tab overlay)
        val builder = ImmutableMap.<RectangularTabOverlay.Dimension, BitSet>builder();
        for (int columns = 1; columns <= 4; columns++) {
            for (int rows = 0; rows <= 20; rows++) {
                if (columns != 1 && rows != 0 && columns * rows <= (columns - 1) * 20)
                    continue;
                BitSet usedSlots = new BitSet(80);
                for (int column = 0; column < columns; column++) {
                    for (int row = 0; row < rows; row++) {
                        usedSlots.set(index(column, row));
                    }
                }
                builder.put(new RectangularTabOverlay.Dimension(columns, rows), usedSlots);
            }
        }
        DIMENSION_TO_USED_SLOTS = builder.build();

        // build the size to used slots map (for the simple tab overlay)
        SIZE_TO_USED_SLOTS = new BitSet[81];
        for (int size = 0; size <= 80; size++) {
            BitSet usedSlots = new BitSet(80);
            usedSlots.set(0, size);
            SIZE_TO_USED_SLOTS[size] = usedSlots;
        }

        // generate random uuids for our custom slots
        CUSTOM_SLOT_UUID_ALEX = new UUID[80];
        CUSTOM_SLOT_UUID_STEVE = new UUID[80];
        UUID base = UUID.randomUUID();
        long msb = base.getMostSignificantBits();
        long lsb = base.getLeastSignificantBits();
        lsb ^= base.hashCode();
        for (int i = 0; i < 80; i++) {
            CUSTOM_SLOT_UUID_STEVE[i] = new UUID(msb, lsb ^ (2 * i));
            CUSTOM_SLOT_UUID_ALEX[i] = new UUID(msb, lsb ^ (2 * i + 1));
        }
        if (OPTION_ENABLE_CUSTOM_SLOT_UUID_COLLISION_CHECK) {
            CUSTOM_SLOT_UUIDS = ImmutableSet.<UUID>builder()
                    .add(CUSTOM_SLOT_UUID_ALEX)
                    .add(CUSTOM_SLOT_UUID_STEVE).build();
        } else {
            CUSTOM_SLOT_UUIDS = Collections.emptySet();
        }

        // generate usernames for custom slots
        int unique = ThreadLocalRandom.current().nextInt();
        CUSTOM_SLOT_USERNAME = new String[81];
        for (int i = 0; i < 81; i++) {
            CUSTOM_SLOT_USERNAME[i] = String.format("~BTLP%08x %02d", unique, i);
        }
        if (OPTION_ENABLE_CUSTOM_SLOT_USERNAME_COLLISION_CHECK) {
            CUSTOM_SLOT_USERNAMES = ImmutableSet.copyOf(CUSTOM_SLOT_USERNAME);
        } else {
            CUSTOM_SLOT_USERNAMES = Collections.emptySet();
        }
        CUSTOM_SLOT_USERNAME_SMILEYS = new String[80];
        String emojis = "\u263a\u2639\u2620\u2763\u2764\u270c\u261d\u270d\u2618\u2615\u2668\u2693\u2708\u231b\u231a\u2600\u2b50\u2601\u2602\u2614\u26a1\u2744\u2603\u2604\u2660\u2665\u2666\u2663\u265f\u260e\u2328\u2709\u270f\u2712\u2702\u2692\u2694\u2699\u2696\u2697\u26b0\u26b1\u267f\u26a0\u2622\u2623\u2640\u2642\u267e\u267b\u269c\u303d\u2733\u2734\u2747\u203c\u2b1c\u2b1b\u25fc\u25fb\u25aa\u25ab\u2049\u26ab\u26aa\u3030\u00a9\u00ae\u2122\u2139\u24c2\u3297\u2716\u2714\u2611\u2695\u2b06\u2197\u27a1\u2198\u2b07\u2199\u3299\u2b05\u2196\u2195\u2194\u21a9\u21aa\u2934\u2935\u269b\u2721\u2638\u262f\u271d\u2626\u262a\u262e\u2648\u2649\u264a\u264b\u264c\u264d\u264e\u264f\u2650\u2651\u2652\u2653\u25b6\u25c0\u23cf";
        for (int i = 0; i < 80; i++) {
            CUSTOM_SLOT_USERNAME_SMILEYS[i] = String.format("" + emojis.charAt(i), unique, i);
        }

        // generate teams for custom slots
        CUSTOM_SLOT_TEAMNAME = new String[81];
        for (int i = 0; i < 81; i++) {
            CUSTOM_SLOT_TEAMNAME[i] = String.format(" BTLP%08x %02d", unique, i);
        }
    }

    private final Logger logger;
    private final Executor eventLoopExecutor;

    private final Object2BooleanMap<UUID> serverPlayerListListed = new Object2BooleanOpenHashMap<>();
    @Nullable
    protected BaseComponent serverHeader = null;
    @Nullable
    protected BaseComponent serverFooter = null;

    private final Queue<AbstractContentOperationModeHandler<?>> nextActiveContentHandlerQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AbstractHeaderFooterOperationModeHandler<?>> nextActiveHeaderFooterHandlerQueue = new ConcurrentLinkedQueue<>();
    private AbstractContentOperationModeHandler<?> activeContentHandler;
    private AbstractHeaderFooterOperationModeHandler<?> activeHeaderFooterHandler;

    private boolean hasCreatedCustomTeams = false;

    private final AtomicBoolean updateScheduledFlag = new AtomicBoolean(false);
    private final Runnable updateTask = this::update;

    protected boolean active;
    
    private boolean logVersionMismatch = false;

    private final ProxiedPlayer player;

    public NewTabOverlayHandler(Logger logger, Executor eventLoopExecutor, ProxiedPlayer player) {
        this.logger = logger;
        this.eventLoopExecutor = eventLoopExecutor;
        this.player = player;
        this.activeContentHandler = new PassThroughContentHandler();
        this.activeHeaderFooterHandler = new PassThroughHeaderFooterHandler();
    }

    private void sendPacket(DefinedPacket packet) {
        if (((packet instanceof PlayerListItemUpdate) || (packet instanceof PlayerListItemRemove)) && (player.getPendingConnection().getVersion() < 761)) {
            // error
            if (!logVersionMismatch) {
                logVersionMismatch = true;
                logger.warning("Cannot correctly update tablist for player " + player.getName() + "\nThe client and server versions do not match. Client >= 1.19.3, server < 1.19.3.\nUse ViaVersion on the spigot server for the best experience.");
            }
        } else if (player.getPendingConnection().getVersion() >= 764) {
            // Ensure that unsafe packets are not sent in the config phase
            // Why bungee doesn't expose this via api beyond me...
            // https://github.com/SpigotMC/BungeeCord/blob/1ef4d27dbea48a1d47501ad2be0d75e42cc2cc12/proxy/src/main/java/net/md_5/bungee/UserConnection.java#L182-L192
            try {
                ((UserConnection) player).sendPacketQueued(packet);
            } catch (Exception ignored) {

            }
        } else {
            player.unsafe().sendPacket(packet);
        }
    }

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
        return PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onPlayerListUpdatePacket(PlayerListItemUpdate packet) {

        if (!active) {
            active = true;
            scheduleUpdate();
        }

        if (packet.getActions().contains(PlayerListItemUpdate.Action.ADD_PLAYER)) {
            for (PlayerListItem.Item item : packet.getItems()) {
                if (OPTION_ENABLE_CUSTOM_SLOT_UUID_COLLISION_CHECK) {
                    if (CUSTOM_SLOT_UUIDS.contains(item.getUuid())) {
                        throw new AssertionError("UUID collision " + item.getUuid());
                    }
                }
                if (OPTION_ENABLE_CUSTOM_SLOT_USERNAME_COLLISION_CHECK) {
                    if (CUSTOM_SLOT_USERNAMES.contains(item.getUsername())) {
                        throw new AssertionError("Username collision" + item.getUsername());
                    }
                }
                serverPlayerListListed.putIfAbsent(item.getUuid(), false);
            }
        }
        if (packet.getActions().contains(PlayerListItemUpdate.Action.UPDATE_LISTED)) {
            for (PlayerListItem.Item item : packet.getItems()) {
                serverPlayerListListed.put(item.getUuid(), item.getListed().booleanValue());
            }
        }

        try {
            return this.activeContentHandler.onPlayerListUpdatePacket(packet);
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Unexpected error", th);
            // try recover
            enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
            return PacketListenerResult.PASS;
        }
    }

    @Override
    public PacketListenerResult onPlayerListRemovePacket(PlayerListItemRemove packet) {
        for (UUID uuid : packet.getUuids()) {
            serverPlayerListListed.removeBoolean(uuid);
        }
        return PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        return PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
        PacketListenerResult result = PacketListenerResult.PASS;
        try {
            result = this.activeHeaderFooterHandler.onPlayerListHeaderFooterPacket(packet);
            if (result == PacketListenerResult.MODIFIED) {
                throw new AssertionError("PacketListenerResult.MODIFIED must not be used");
            }
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Unexpected error", th);
            // try recover
            enterHeaderAndFooterOperationMode(HeaderAndFooterOperationMode.PASS_TROUGH);
        }

        this.serverHeader = packet.getHeader() != null ? packet.getHeader() : EMPTY_TEXT_COMPONENT;
        this.serverFooter = packet.getFooter() != null ? packet.getFooter() : EMPTY_TEXT_COMPONENT;

        return result;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {

        hasCreatedCustomTeams = false;

        try {
            this.activeContentHandler.onServerSwitch();
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Unexpected error", th);
        }
        try {
            this.activeHeaderFooterHandler.onServerSwitch();
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Unexpected error", th);
        }

        if (!serverPlayerListListed.isEmpty()) {
            PlayerListItemRemove packet = new PlayerListItemRemove();
            packet.setUuids(serverPlayerListListed.keySet().toArray(new UUID[0]));
            sendPacket(packet);
        }

        serverPlayerListListed.clear();
        if (serverHeader != null) {
            serverHeader = EMPTY_TEXT_COMPONENT;
        }
        if (serverFooter != null) {
            serverFooter = EMPTY_TEXT_COMPONENT;
        }

        active = false;
    }

    @Override
    public <R> R enterContentOperationMode(ContentOperationMode<R> operationMode) {
        AbstractContentOperationModeHandler<?> handler;
        if (operationMode == ContentOperationMode.PASS_TROUGH) {
            handler = new PassThroughContentHandler();
        } else if (operationMode == ContentOperationMode.SIMPLE) {
            handler = new SimpleOperationModeHandler();
        } else if (operationMode == ContentOperationMode.RECTANGULAR) {
            handler = new RectangularSizeHandler();
        } else {
            throw new UnsupportedOperationException();
        }
        nextActiveContentHandlerQueue.add(handler);
        scheduleUpdate();
        return Unchecked.cast(handler.getTabOverlay());
    }

    @Override
    public <R> R enterHeaderAndFooterOperationMode(HeaderAndFooterOperationMode<R> operationMode) {
        AbstractHeaderFooterOperationModeHandler<?> handler;
        if (operationMode == HeaderAndFooterOperationMode.PASS_TROUGH) {
            handler = new PassThroughHeaderFooterHandler();
        } else if (operationMode == HeaderAndFooterOperationMode.CUSTOM) {
            handler = new CustomHeaderAndFooterOperationModeHandler();
        } else {
            throw new UnsupportedOperationException(Objects.toString(operationMode));
        }
        nextActiveHeaderFooterHandlerQueue.add(handler);
        scheduleUpdate();
        return Unchecked.cast(handler.getTabOverlay());
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
        updateScheduledFlag.set(false);

        ChannelWrapper ch = ((UserConnection) player).getCh();
        if (!active || ch.isClosed() || ch.getEncodeProtocol() != Protocol.GAME) {
            return;
        }

        // update content handler
        AbstractContentOperationModeHandler<?> contentHandler;
        while (null != (contentHandler = nextActiveContentHandlerQueue.poll())) {
            this.activeContentHandler.invalidate();
            contentHandler.onActivated(this.activeContentHandler);
            this.activeContentHandler = contentHandler;
        }
        this.activeContentHandler.update();

        // update header and footer handler
        AbstractHeaderFooterOperationModeHandler<?> heaerFooterHandler;
        while (null != (heaerFooterHandler = nextActiveHeaderFooterHandlerQueue.poll())) {
            this.activeHeaderFooterHandler.invalidate();
            heaerFooterHandler.onActivated(this.activeHeaderFooterHandler);
            this.activeHeaderFooterHandler = heaerFooterHandler;
        }
        this.activeHeaderFooterHandler.update();
    }

    private abstract static class AbstractContentOperationModeHandler<T extends AbstractContentTabOverlay> extends OperationModeHandler<T> {

        /**
         * Called when the player receives a {@link PlayerListItem} packet.
         * <p>
         * This method is called after this {@link NewTabOverlayHandler} has updated the {@code serverPlayerList}.
         */
        abstract PacketListenerResult onPlayerListUpdatePacket(PlayerListItemUpdate packet);

        /**
         * Called when the player switches the server.
         * <p>
         * This method is called before this {@link NewTabOverlayHandler} executes its own logic to clear the
         * server player list info.
         */
        abstract void onServerSwitch();

        abstract void update();

        final void invalidate() {
            getTabOverlay().invalidate();
            onDeactivated();
        }

        /**
         * Called when this {@link OperationModeHandler} is deactivated.
         * <p>
         * This method must put the client player list in the state expected by {@link #onActivated(AbstractContentOperationModeHandler)}. It must
         * especially remove all custom entries and players must be part of the correct teams.
         */
        abstract void onDeactivated();

        /**
         * Called when this {@link OperationModeHandler} becomes the active one.
         * <p>
         * State of the player list when this method is called:
         * - there are no custom entries on the client
         * - all entries from {@link #serverPlayerListListed} but may not be listed
         * - player list header/ footer may be wrong
         * <p>
         * Additional information about the state of the player list may be obtained from the previous handler
         *
         * @param previous previous handler
         */
        abstract void onActivated(AbstractContentOperationModeHandler<?> previous);
    }

    private abstract static class AbstractHeaderFooterOperationModeHandler<T extends AbstractHeaderFooterTabOverlay> extends OperationModeHandler<T> {

        /**
         * Called when the player receives a {@link PlayerListHeaderFooter} packet.
         * <p>
         * This method is called before this {@link NewTabOverlayHandler} executes its own logic to update the
         * server player list info.
         */
        abstract PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet);

        /**
         * Called when the player switches the server.
         * <p>
         * This method is called before this {@link NewTabOverlayHandler} executes its own logic to clear the
         * server player list info.
         */
        abstract void onServerSwitch();

        abstract void update();

        final void invalidate() {
            getTabOverlay().invalidate();
            onDeactivated();
        }

        /**
         * Called when this {@link OperationModeHandler} is deactivated.
         * <p>
         * This method must put the client player list in the state expected by {@link #onActivated(AbstractHeaderFooterOperationModeHandler)}. It must
         * especially remove all custom entries and players must be part of the correct teams.
         */
        abstract void onDeactivated();

        /**
         * Called when this {@link OperationModeHandler} becomes the active one.
         * <p>
         * State of the player list when this method is called:
         * - there are no custom entries on the client
         * - all entries from {@link #serverPlayerListListed} are known to the client, but might not be listed
         * - player list header/ footer may be wrong
         * <p>
         * Additional information about the state of the player list may be obtained from the previous handler
         *
         * @param previous previous handler
         */
        abstract void onActivated(AbstractHeaderFooterOperationModeHandler<?> previous);
    }

    private abstract static class AbstractContentTabOverlay implements TabOverlayHandle {
        private boolean valid = true;

        @Override
        public boolean isValid() {
            return valid;
        }

        final void invalidate() {
            valid = false;
        }
    }

    private abstract static class AbstractHeaderFooterTabOverlay implements TabOverlayHandle {
        private boolean valid = true;

        @Override
        public boolean isValid() {
            return valid;
        }

        final void invalidate() {
            valid = false;
        }
    }

    private final class PassThroughContentHandler extends AbstractContentOperationModeHandler<PassThroughContentTabOverlay> {

        @Override
        protected PassThroughContentTabOverlay createTabOverlay() {
            return new PassThroughContentTabOverlay();
        }

        @Override
        PacketListenerResult onPlayerListUpdatePacket(PlayerListItemUpdate packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        void onServerSwitch() {
            sendPacket(new PlayerListHeaderFooter(EMPTY_TEXT_COMPONENT, EMPTY_TEXT_COMPONENT));
        }

        @Override
        void update() {
            // nothing to do
        }

        @Override
        void onDeactivated() {
            // nothing to do
        }

        @Override
        void onActivated(AbstractContentOperationModeHandler<?> previous) {
            if (previous instanceof PassThroughContentHandler) {
                // we're lucky, nothing to do
                return;
            }

            // update visibility
            if (!serverPlayerListListed.isEmpty()) {
                List<PlayerListItem.Item> items = new ArrayList<>(serverPlayerListListed.size());
                for (Object2BooleanMap.Entry<UUID> entry : serverPlayerListListed.object2BooleanEntrySet()) {
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(entry.getKey());
                    item.setListed(entry.getBooleanValue());
                    items.add(item);
                }
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LISTED));
                packet.setItems(items.toArray(new PlayerListItem.Item[0]));
                sendPacket(packet);
            }
        }
    }

    private static final class PassThroughContentTabOverlay extends AbstractContentTabOverlay {

    }

    private final class PassThroughHeaderFooterHandler extends AbstractHeaderFooterOperationModeHandler<PassThroughHeaderFooterTabOverlay> {

        @Override
        protected PassThroughHeaderFooterTabOverlay createTabOverlay() {
            return new PassThroughHeaderFooterTabOverlay();
        }

        @Override
        PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        void onServerSwitch() {
            sendPacket(new PlayerListHeaderFooter(EMPTY_TEXT_COMPONENT, EMPTY_TEXT_COMPONENT));
        }

        @Override
        void update() {
            // nothing to do
        }

        @Override
        void onDeactivated() {
            // nothing to do
        }

        @Override
        void onActivated(AbstractHeaderFooterOperationModeHandler<?> previous) {
            if (previous instanceof PassThroughHeaderFooterHandler) {
                // we're lucky, nothing to do
                return;
            }

            // fix header/ footer
            sendPacket(new PlayerListHeaderFooter(serverHeader != null ? serverHeader : EMPTY_TEXT_COMPONENT, serverFooter != null ? serverFooter : EMPTY_TEXT_COMPONENT));
        }
    }

    private static final class PassThroughHeaderFooterTabOverlay extends AbstractHeaderFooterTabOverlay {

    }

    private abstract class CustomContentTabOverlayHandler<T extends CustomContentTabOverlay> extends AbstractContentOperationModeHandler<T> {

        @Nonnull
        BitSet usedSlots;
        BitSet dirtySlots;
        final SlotState[] slotState;
        /**
         * Uuid of the player list entry used for the slot.
         */
        final UUID[] slotUuid;
        /**
         * Username of the player list entry used for the slot.
         */
        final String[] slotUsername;

        private final List<PlayerListItem.Item> itemQueueAddPlayer;
        private final List<UUID> itemQueueRemovePlayer;
        private final List<PlayerListItem.Item> itemQueueUpdateDisplayName;
        private final List<PlayerListItem.Item> itemQueueUpdatePing;

        private final boolean experimentalTabCompleteSmileys = isExperimentalTabCompleteSmileys();

        private CustomContentTabOverlayHandler() {
            this.dirtySlots = new BitSet(80);
            this.usedSlots = SIZE_TO_USED_SLOTS[0];
            this.slotState = new SlotState[80];
            Arrays.fill(this.slotState, SlotState.UNUSED);
            this.slotUuid = new UUID[80];
            this.slotUsername = new String[80];
            this.itemQueueAddPlayer = new ArrayList<>(80);
            this.itemQueueRemovePlayer = new ArrayList<>(80);
            this.itemQueueUpdateDisplayName = new ArrayList<>(80);
            this.itemQueueUpdatePing = new ArrayList<>(80);
        }

        @Override
        PacketListenerResult onPlayerListUpdatePacket(PlayerListItemUpdate packet) {

            if (packet.getActions().contains(PlayerListItemUpdate.Action.UPDATE_LISTED)) {
                for (PlayerListItem.Item item : packet.getItems()) {
                    item.setListed(false);
                }
            }
            return PacketListenerResult.MODIFIED;
        }

        private String getCustomSlotUsername(int index) {
            if (experimentalTabCompleteSmileys) {
                return CUSTOM_SLOT_USERNAME_SMILEYS[index];
            } else {
                return CUSTOM_SLOT_USERNAME[index];
            }
        }

        @Override
        void onServerSwitch() {
            if (player.getPendingConnection().getVersion() >= 764) {
                clearCustomSlots();
            }
        }

        @Override
        void onActivated(AbstractContentOperationModeHandler<?> previous) {

            // make all players unlisted
            if (!serverPlayerListListed.isEmpty()) {
                List<PlayerListItem.Item> items = new ArrayList<>(serverPlayerListListed.size());
                for (Object2BooleanMap.Entry<UUID> entry : serverPlayerListListed.object2BooleanEntrySet()) {
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(entry.getKey());
                    item.setListed(false);
                    items.add(item);
                }
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LISTED));
                packet.setItems(items.toArray(new PlayerListItem.Item[0]));
                sendPacket(packet);
            }
            
            createTeamsIfNecessary();
        }

        private void createTeamsIfNecessary() {
            // create teams if not already created
            if (!hasCreatedCustomTeams) {
                hasCreatedCustomTeams = true;

                for (int i = 0; i < 80; i++) {
                    sendPacket(createPacketTeamCreate(CUSTOM_SLOT_TEAMNAME[i], EMPTY_EITHER_TEXT_COMPONENT, EMPTY_EITHER_TEXT_COMPONENT, EMPTY_EITHER_TEXT_COMPONENT, "always", "always", 21, (byte) 1, new String[]{CUSTOM_SLOT_USERNAME[i], CUSTOM_SLOT_USERNAME_SMILEYS[i]}));
                }
            }
        }

        @Override
        void onDeactivated() {
            clearCustomSlots();
        }

        private void clearCustomSlots() {
            int customSlots = 0;
            for (int index = 0; index < 80; index++) {
                if (slotState[index] != SlotState.UNUSED) {
                    customSlots++;
                    dirtySlots.set(index);
                }
            }

            int i = 0;
            if (customSlots > 0) {
                UUID[] uuids = new UUID[customSlots];
                for (int index = 0; index < 80; index++) {
                    // switch slot from custom to unused
                    if (slotState[index] == SlotState.CUSTOM) {
                        uuids[i++] = slotUuid[index];
                    }
                }
                PlayerListItemRemove packet = new PlayerListItemRemove();
                packet.setUuids(uuids);
                sendPacket(packet);
            }
        }

        @Override
        void update() {

            createTeamsIfNecessary();

            T tabOverlay = getTabOverlay();

            if (tabOverlay.dirtyFlagSize) {
                tabOverlay.dirtyFlagSize = false;
                updateSize();
            }

            // update icons
            dirtySlots.orAndClear(tabOverlay.dirtyFlagsIcon);
            for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                if (slotState[index] == SlotState.CUSTOM) {
                    // remove item
                    itemQueueRemovePlayer.add(slotUuid[index]);
                    slotState[index] = SlotState.UNUSED;
                }
                
                if (usedSlots.get(index)) {
                    Icon icon = tabOverlay.icon[index];
                    UUID customSlotUuid;
                    if (icon.isAlex()) {
                        customSlotUuid = CUSTOM_SLOT_UUID_ALEX[index];
                    } else { // steve
                        customSlotUuid = CUSTOM_SLOT_UUID_STEVE[index];
                    }
                    tabOverlay.dirtyFlagsText.clear(index);
                    tabOverlay.dirtyFlagsPing.clear(index);
                    slotState[index] = SlotState.CUSTOM;
                    slotUuid[index] = customSlotUuid;
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(customSlotUuid);
                    item.setUsername(slotUsername[index] = getCustomSlotUsername(index));
                    Property119Handler.setProperties(item, toPropertiesArray(icon.getTextureProperty()));
                    item.setDisplayName(tabOverlay.text[index]);
                    item.setPing(tabOverlay.ping[index]);
                    item.setGamemode(0);
                    item.setListed(true);
                    itemQueueAddPlayer.add(item);
                }
            }

            // update text
            dirtySlots.copyAndClear(tabOverlay.dirtyFlagsText);
            for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                if (slotState[index] != SlotState.UNUSED) {
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(slotUuid[index]);
                    item.setDisplayName(tabOverlay.text[index]);
                    itemQueueUpdateDisplayName.add(item);
                }
            }

            // update ping
            dirtySlots.copyAndClear(tabOverlay.dirtyFlagsPing);
            for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                if (slotState[index] != SlotState.UNUSED) {
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setUuid(slotUuid[index]);
                    item.setPing(tabOverlay.ping[index]);
                    itemQueueUpdatePing.add(item);
                }
            }

            dirtySlots.clear();

            // send packets
            sendQueuedItems();
        }

        private void sendQueuedItems() {
            if (!itemQueueRemovePlayer.isEmpty()) {
                PlayerListItemRemove packet = new PlayerListItemRemove();
                packet.setUuids(itemQueueRemovePlayer.toArray(new UUID[0]));
                sendPacket(packet);
                itemQueueRemovePlayer.clear();
            }
            if (!itemQueueAddPlayer.isEmpty()) {
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.ADD_PLAYER, PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME, PlayerListItemUpdate.Action.UPDATE_LATENCY, PlayerListItemUpdate.Action.UPDATE_LISTED));
                packet.setItems(itemQueueAddPlayer.toArray(new PlayerListItem.Item[0]));
                sendPacket(packet);
                itemQueueAddPlayer.clear();
            }
            if (!itemQueueUpdateDisplayName.isEmpty()) {
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
                packet.setItems(itemQueueUpdateDisplayName.toArray(new PlayerListItem.Item[0]));
                sendPacket(packet);
                itemQueueUpdateDisplayName.clear();
            }
            if (!itemQueueUpdatePing.isEmpty()) {
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY));
                packet.setItems(itemQueueUpdatePing.toArray(new PlayerListItem.Item[0]));
                sendPacket(packet);
                itemQueueUpdatePing.clear();
            }
        }

        /**
         * Updates the usedSlots BitSet. Sets the {@link #dirtySlots uuid dirty flag} for all added
         * and removed slots.
         */
        abstract void updateSize();
    }

    private boolean isExperimentalTabCompleteSmileys() {
        return BungeeTabListPlus.getInstance().getConfig().experimentalTabCompleteSmileys;
    }

    private abstract class CustomContentTabOverlay extends AbstractContentTabOverlay implements TabOverlayHandle.BatchModifiable {
        final Icon[] icon;
        final BaseComponent[] text;
        final int[] ping;

        final AtomicInteger batchUpdateRecursionLevel;
        volatile boolean dirtyFlagSize;
        final ConcurrentBitSet dirtyFlagsIcon;
        final ConcurrentBitSet dirtyFlagsText;
        final ConcurrentBitSet dirtyFlagsPing;

        private CustomContentTabOverlay() {
            this.icon = new Icon[80];
            Arrays.fill(this.icon, Icon.DEFAULT_STEVE);
            this.text = new BaseComponent[80];
            Arrays.fill(this.text, EMPTY_TEXT_COMPONENT);
            this.ping = new int[80];
            this.batchUpdateRecursionLevel = new AtomicInteger(0);
            this.dirtyFlagSize = true;
            this.dirtyFlagsIcon = new ConcurrentBitSet(80);
            this.dirtyFlagsText = new ConcurrentBitSet(80);
            this.dirtyFlagsPing = new ConcurrentBitSet(80);
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

        void scheduleUpdateIfNotInBatch() {
            if (batchUpdateRecursionLevel.get() == 0) {
                scheduleUpdate();
            }
        }

        void setIconInternal(int index, @Nonnull @NonNull Icon icon) {
            if (!icon.equals(this.icon[index])) {
                this.icon[index] = icon;
                dirtyFlagsIcon.set(index);
                scheduleUpdateIfNotInBatch();
            }
        }

        void setTextInternal(int index, @Nonnull @NonNull String text) {
            String jsonText = ChatFormat.formattedTextToJson(text);
            BaseComponent component = ComponentSerializer.deserialize(jsonText);
            if (!component.equals(this.text[index])) {
                this.text[index] = component;
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
    }

    private class RectangularSizeHandler extends CustomContentTabOverlayHandler<RectangularTabOverlayImpl> {

        @Override
        void updateSize() {
            RectangularTabOverlayImpl tabOverlay = getTabOverlay();
            RectangularTabOverlay.Dimension size = tabOverlay.getSize();
            BitSet newUsedSlots = DIMENSION_TO_USED_SLOTS.get(size);
            dirtySlots.orXor(usedSlots, newUsedSlots);
            usedSlots = newUsedSlots;
        }

        @Override
        protected RectangularTabOverlayImpl createTabOverlay() {
            return new RectangularTabOverlayImpl();
        }
    }

    private class RectangularTabOverlayImpl extends CustomContentTabOverlay implements RectangularTabOverlay {

        @Nonnull
        private Dimension size;

        private RectangularTabOverlayImpl() {
            Optional<Dimension> dimensionZero = getSupportedSizes().stream().filter(size -> size.getSize() == 0).findAny();
            if (!dimensionZero.isPresent()) {
                throw new AssertionError();
            }
            this.size = dimensionZero.get();
        }

        @Nonnull
        @Override
        public Dimension getSize() {
            return size;
        }

        @Override
        public Collection<Dimension> getSupportedSizes() {
            return DIMENSION_TO_USED_SLOTS.keySet();
        }

        @Override
        public void setSize(@Nonnull Dimension size) {
            if (!getSupportedSizes().contains(size)) {
                throw new IllegalArgumentException("Unsupported size " + size);
            }
            if (isValid() && !this.size.equals(size)) {
                BitSet oldUsedSlots = DIMENSION_TO_USED_SLOTS.get(this.size);
                BitSet newUsedSlots = DIMENSION_TO_USED_SLOTS.get(size);
                for (int index = newUsedSlots.nextSetBit(0); index >= 0; index = newUsedSlots.nextSetBit(index + 1)) {
                    if (!oldUsedSlots.get(index)) {
                        icon[index] = Icon.DEFAULT_STEVE;
                        text[index] = EMPTY_TEXT_COMPONENT;
                        ping[index] = 0;
                    }
                }
                this.size = size;
                this.dirtyFlagSize = true;
                scheduleUpdateIfNotInBatch();
                for (int index = oldUsedSlots.nextSetBit(0); index >= 0; index = oldUsedSlots.nextSetBit(index + 1)) {
                    if (!newUsedSlots.get(index)) {
                        icon[index] = Icon.DEFAULT_STEVE;
                        text[index] = EMPTY_TEXT_COMPONENT;
                        ping[index] = 0;
                    }
                }
            }
        }

        @Override
        public void setSlot(int column, int row, @Nullable UUID uuid, @Nonnull Icon icon, @Nonnull String text, int ping) {
            setSlot(column, row, icon, text, ping);
        }

        @Override
        public void setSlot(int column, int row, @Nonnull Icon icon, @Nonnull String text, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, size.getColumns(), "column");
                Preconditions.checkElementIndex(row, size.getRows(), "row");
                beginBatchModification();
                try {
                    int index = index(column, row);
                    setIconInternal(index, icon);
                    setTextInternal(index, text);
                    setPingInternal(index, ping);
                } finally {
                    completeBatchModification();
                }
            }
        }

        @Override
        public void setUuid(int column, int row, @Nullable UUID uuid) {
            // no op
        }

        @Override
        public void setIcon(int column, int row, @Nonnull Icon icon) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, size.getColumns(), "column");
                Preconditions.checkElementIndex(row, size.getRows(), "row");
                setIconInternal(index(column, row), icon);
            }
        }

        @Override
        public void setText(int column, int row, @Nonnull String text) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, size.getColumns(), "column");
                Preconditions.checkElementIndex(row, size.getRows(), "row");
                setTextInternal(index(column, row), text);
            }
        }

        @Override
        public void setPing(int column, int row, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, size.getColumns(), "column");
                Preconditions.checkElementIndex(row, size.getRows(), "row");
                setPingInternal(index(column, row), ping);
            }
        }
    }

    private class SimpleOperationModeHandler extends CustomContentTabOverlayHandler<SimpleTabOverlayImpl> {

        private int size = 0;

        @Override
        void updateSize() {
            int newSize = getTabOverlay().size;
            if (newSize > size) {
                dirtySlots.set(size, newSize);
            } else if (newSize < size) {
                dirtySlots.set(newSize, size);
            }
            usedSlots = SIZE_TO_USED_SLOTS[newSize];
            size = newSize;
        }

        @Override
        protected SimpleTabOverlayImpl createTabOverlay() {
            return new SimpleTabOverlayImpl();
        }
    }

    private class SimpleTabOverlayImpl extends CustomContentTabOverlay implements SimpleTabOverlay {
        int size = 0;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public int getMaxSize() {
            return 80;
        }

        @Override
        public void setSize(int size) {
            if (size < 0 || size > 80) {
                throw new IllegalArgumentException("size");
            }
            this.size = size;
            dirtyFlagSize = true;
            scheduleUpdateIfNotInBatch();
        }

        @Override
        public void setSlot(int index, @Nullable UUID uuid, @Nonnull Icon icon, @Nonnull String text, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size, "index");
                beginBatchModification();
                try {
                    setIconInternal(index, icon);
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
                Preconditions.checkElementIndex(index, size, "index");
                beginBatchModification();
                try {
                    setIconInternal(index, icon);
                    setTextInternal(index, text);
                    setPingInternal(index, ping);
                } finally {
                    completeBatchModification();
                }
            }
        }

        @Override
        public void setUuid(int index, UUID uuid) {
            // no op
        }

        @Override
        public void setIcon(int index, @Nonnull @NonNull Icon icon) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size, "index");
                setIconInternal(index, icon);
            }
        }

        @Override
        public void setText(int index, @Nonnull @NonNull String text) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size, "index");
                setTextInternal(index, text);
            }
        }

        @Override
        public void setPing(int index, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(index, size, "index");
                setPingInternal(index, ping);
            }
        }
    }

    private final class CustomHeaderAndFooterOperationModeHandler extends AbstractHeaderFooterOperationModeHandler<CustomHeaderAndFooterImpl> {

        @Override
        protected CustomHeaderAndFooterImpl createTabOverlay() {
            return new CustomHeaderAndFooterImpl();
        }

        @Override
        PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
            return PacketListenerResult.CANCEL;
        }

        @Override
        void onServerSwitch() {
            // do nothing
        }

        @Override
        void onDeactivated() {
            //do nothing
        }

        @Override
        void onActivated(AbstractHeaderFooterOperationModeHandler<?> previous) {
            // remove header/ footer
            sendPacket(new PlayerListHeaderFooter(EMPTY_TEXT_COMPONENT, EMPTY_TEXT_COMPONENT));
        }

        @Override
        void update() {
            CustomHeaderAndFooterImpl tabOverlay = getTabOverlay();
            if (tabOverlay.headerOrFooterDirty) {
                tabOverlay.headerOrFooterDirty = false;
                sendPacket(new PlayerListHeaderFooter(tabOverlay.header, tabOverlay.footer));
            }
        }
    }

    private final class CustomHeaderAndFooterImpl extends AbstractHeaderFooterTabOverlay implements HeaderAndFooterHandle {
        private BaseComponent header = EMPTY_TEXT_COMPONENT;
        private BaseComponent footer = EMPTY_TEXT_COMPONENT;

        private volatile boolean headerOrFooterDirty = false;

        final AtomicInteger batchUpdateRecursionLevel = new AtomicInteger(0);

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

        void scheduleUpdateIfNotInBatch() {
            if (batchUpdateRecursionLevel.get() == 0) {
                scheduleUpdate();
            }
        }

        @Override
        public void setHeaderFooter(@Nullable String header, @Nullable String footer) {
            this.header = ComponentSerializer.deserialize(ChatFormat.formattedTextToJson(header));
            this.footer = ComponentSerializer.deserialize(ChatFormat.formattedTextToJson(footer));
            headerOrFooterDirty = true;
            scheduleUpdateIfNotInBatch();
        }

        @Override
        public void setHeader(@Nullable String header) {
            this.header = ComponentSerializer.deserialize(ChatFormat.formattedTextToJson(header));
            headerOrFooterDirty = true;
            scheduleUpdateIfNotInBatch();
        }

        @Override
        public void setFooter(@Nullable String footer) {
            this.footer = ComponentSerializer.deserialize(ChatFormat.formattedTextToJson(footer));
            headerOrFooterDirty = true;
            scheduleUpdateIfNotInBatch();
        }
    }

    private static int index(int column, int row) {
        return column * 20 + row;
    }

    private static String[][] toPropertiesArray(ProfileProperty textureProperty) {
        if (textureProperty == null) {
            return EMPTY_PROPERTIES_ARRAY;
        } else if (textureProperty.isSigned()) {
            return new String[][]{{textureProperty.getName(), textureProperty.getValue(), textureProperty.getSignature()}};
        } else {
            return new String[][]{{textureProperty.getName(), textureProperty.getValue()}};
        }
    }

    private static Team createPacketTeamCreate(String name, Either<String, BaseComponent> displayName, Either<String, BaseComponent> prefix, Either<String, BaseComponent> suffix, String nameTagVisibility, String collisionRule, int color, byte friendlyFire, String[] players) {
        Team team = new Team();
        team.setName(name);
        team.setMode((byte) 0);
        team.setDisplayName(displayName);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.setNameTagVisibility(nameTagVisibility);
        team.setCollisionRule(collisionRule);
        team.setColor(color);
        team.setFriendlyFire(friendlyFire);
        team.setPlayers(players);
        return team;
    }

    private enum SlotState {
        UNUSED, CUSTOM
    }
}
