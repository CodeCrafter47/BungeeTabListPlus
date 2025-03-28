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

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.protocol.Team;
import codecrafter47.bungeetablistplus.util.BitSet;
import codecrafter47.bungeetablistplus.util.ConcurrentBitSet;
import codecrafter47.bungeetablistplus.util.Property119Handler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooterPacket;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.ProfileProperty;
import de.codecrafter47.taboverlay.config.misc.ChatFormat;
import de.codecrafter47.taboverlay.config.misc.Unchecked;
import de.codecrafter47.taboverlay.handler.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

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

import static com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket.ADD_PLAYER;
import static com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket.REMOVE_PLAYER;
import static com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket.UPDATE_DISPLAY_NAME;
import static com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket.UPDATE_GAMEMODE;
import static com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket.UPDATE_LATENCY;

public abstract class AbstractTabOverlayHandler implements PacketHandler, TabOverlayHandler {

    // some options
    private static final boolean OPTION_ENABLE_CUSTOM_SLOT_USERNAME_COLLISION_CHECK = true;
    private static final boolean OPTION_ENABLE_CUSTOM_SLOT_UUID_COLLISION_CHECK = true;
    private static final boolean OPTION_ENABLE_CONSISTENCY_CHECKS = true;

    private static ComponentHolder EMPTY_COMPONENT;
    protected static final String[][] EMPTY_PROPERTIES_ARRAY = new String[0][];

    private static final ImmutableMap<RectangularTabOverlay.Dimension, BitSet> DIMENSION_TO_USED_SLOTS;
    private static final BitSet[] SIZE_TO_USED_SLOTS;

    private static final UUID[] CUSTOM_SLOT_UUID_STEVE;
    private static final UUID[] CUSTOM_SLOT_UUID_ALEX;
    private static final UUID[] CUSTOM_SLOT_UUID_SPACER;
    private static final Set<UUID> CUSTOM_SLOT_UUIDS;
    private static final String[] CUSTOM_SLOT_USERNAME;
    private static final String[] CUSTOM_SLOT_USERNAME_SMILEYS;
    private static final Set<String> CUSTOM_SLOT_USERNAMES;
    private static final String[] CUSTOM_SLOT_TEAMNAME;
    private static final Set<String> CUSTOM_SLOT_TEAMNAMES;

    private static final Set<String> blockedTeams = new HashSet<>();

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
        CUSTOM_SLOT_UUID_SPACER = new UUID[17];
        UUID base = UUID.randomUUID();
        long msb = base.getMostSignificantBits();
        long lsb = base.getLeastSignificantBits();
        lsb ^= base.hashCode();
        for (int i = 0; i < 80; i++) {
            CUSTOM_SLOT_UUID_STEVE[i] = new UUID(msb, lsb ^ (2 * i));
            CUSTOM_SLOT_UUID_ALEX[i] = new UUID(msb, lsb ^ (2 * i + 1));
        }
        for (int i = 0; i < 17; i++) {
            CUSTOM_SLOT_UUID_SPACER[i] = new UUID(msb, lsb ^ (160 + i));
        }
        if (OPTION_ENABLE_CUSTOM_SLOT_UUID_COLLISION_CHECK) {
            CUSTOM_SLOT_UUIDS = ImmutableSet.<UUID>builder()
                    .add(CUSTOM_SLOT_UUID_ALEX)
                    .add(CUSTOM_SLOT_UUID_STEVE)
                    .add(CUSTOM_SLOT_UUID_SPACER).build();
        } else {
            CUSTOM_SLOT_UUIDS = null;
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
            CUSTOM_SLOT_USERNAMES = null;
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
        CUSTOM_SLOT_TEAMNAMES = ImmutableSet.copyOf(CUSTOM_SLOT_TEAMNAME);
    }

    protected final Logger logger;
    private final Executor eventLoopExecutor;
    private final UUID viewerUuid;

    private final Object2ObjectMap<UUID, PlayerListEntry> serverPlayerList = new Object2ObjectOpenHashMap<>();
    protected final Set<String> serverTabListPlayers = new ObjectOpenHashSet<>();
    @Nullable
    protected Component serverHeader = null;
    @Nullable
    protected Component serverFooter = null;
    protected final Object2ObjectMap<String, TeamEntry> serverTeams = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<String, String> playerToTeamMap = new Object2ObjectOpenHashMap<>();

    private final Queue<AbstractContentOperationModeHandler<?>> nextActiveContentHandlerQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AbstractHeaderFooterOperationModeHandler<?>> nextActiveHeaderFooterHandlerQueue = new ConcurrentLinkedQueue<>();
    private AbstractContentOperationModeHandler<?> activeContentHandler;
    private AbstractHeaderFooterOperationModeHandler<?> activeHeaderFooterHandler;

    private boolean hasCreatedCustomTeams = false;

    private final AtomicBoolean updateScheduledFlag = new AtomicBoolean(false);
    private final Runnable updateTask = this::update;

    private final boolean is18;
    private boolean is13OrLater;
    private boolean is119OrLater;
    private boolean is1203OrLater;
    protected boolean active;

    public AbstractTabOverlayHandler(Logger logger, Executor eventLoopExecutor, UUID viewerUuid, boolean is18, boolean is13OrLater, boolean is119OrLater, boolean is1203OrLater) {
        this.logger = logger;
        this.eventLoopExecutor = eventLoopExecutor;
        this.viewerUuid = viewerUuid;
        this.is18 = is18;
        this.is13OrLater = is13OrLater;
        this.is119OrLater = is119OrLater;
        this.is1203OrLater = is1203OrLater;
        this.activeContentHandler = new PassThroughContentHandler();
        this.activeHeaderFooterHandler = new PassThroughHeaderFooterHandler();
        EMPTY_COMPONENT = new ComponentHolder(is13OrLater ? (is1203OrLater ? ProtocolVersion.MINECRAFT_1_20_3 : ProtocolVersion.MINECRAFT_1_13) : ProtocolVersion.MINECRAFT_1_12_2, Component.empty());
    }

    protected abstract void sendPacket(MinecraftPacket packet);

    protected abstract ProtocolVersion getProtocol();

    @Override
    public PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet) {
        switch (packet.getAction()) {
            case ADD_PLAYER:
                for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                    if (OPTION_ENABLE_CUSTOM_SLOT_UUID_COLLISION_CHECK) {
                        if (CUSTOM_SLOT_UUIDS.contains(item.getUuid())) {
                            throw new AssertionError("UUID collision " + item.getUuid());
                        }
                    }
                    if (OPTION_ENABLE_CUSTOM_SLOT_USERNAME_COLLISION_CHECK) {
                        if (CUSTOM_SLOT_USERNAMES.contains(item.getName())) {
                            throw new AssertionError("Username collision" + item.getName());
                        }
                    }
                    PlayerListEntry old = serverPlayerList.put(item.getUuid(), new PlayerListEntry(item));
                    if (old != null) {
                        serverTabListPlayers.remove(old.getUsername());
                    }
                    serverTabListPlayers.add(item.getName());
                }
                break;
            case UPDATE_GAMEMODE:
                for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                    PlayerListEntry playerListEntry = serverPlayerList.get(item.getUuid());
                    if (playerListEntry != null) {
                        playerListEntry.setGamemode(item.getGameMode());
                    }
                }
                break;
            case UPDATE_LATENCY:
                for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                    PlayerListEntry playerListEntry = serverPlayerList.get(item.getUuid());
                    if (playerListEntry != null) {
                        playerListEntry.setPing(item.getLatency());
                    }
                }
                break;
            case UPDATE_DISPLAY_NAME:
                for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                    PlayerListEntry playerListEntry = serverPlayerList.get(item.getUuid());
                    if (playerListEntry != null) {
                        playerListEntry.setDisplayName(item.getDisplayName());
                    }
                }
                break;
            case REMOVE_PLAYER:
                for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                    PlayerListEntry removed = serverPlayerList.remove(item.getUuid());
                    if (removed != null) {
                        serverTabListPlayers.remove(removed.getUsername());
                    }
                }
                break;
        }

        try {
            return this.activeContentHandler.onPlayerListPacket(packet);
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Unexpected error", th);
            // try recover
            enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
            return PacketListenerResult.PASS;
        }
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {

        if (packet.getPlayers() != null) {
            boolean block = false;
            for (String player : packet.getPlayers()) {
                if (player.isEmpty()) {
                    block = true;
                    break;
                }
            }
            if (block) {
                if (!blockedTeams.contains(packet.getName())) {
                    logger.warning("Blocking Team Packet for Team " + packet.getName() + ", as it is incompatible with BungeeTabListPlus.");
                    blockedTeams.add(packet.getName());
                }
                return PacketListenerResult.CANCEL;
            }
        }

        try {
            this.activeContentHandler.onTeamPacketPreprocess(packet);
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Unexpected error", th);
            // try recover
            enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
        }

        if (Team.Mode.REMOVE.equals(packet.getMode())) {
            TeamEntry team = serverTeams.remove(packet.getName());
            if (team != null) {
                for (String player : team.getPlayers()) {
                    playerToTeamMap.remove(player, packet.getName());
                }
            }
        } else {
            // Create or get old team
            TeamEntry teamEntry;
            if (Team.Mode.CREATE.equals(packet.getMode())) {
                teamEntry = new TeamEntry();
                serverTeams.put(packet.getName(), teamEntry);
            } else {
                teamEntry = serverTeams.get(packet.getName());
            }

            if (teamEntry != null) {
                if (Team.Mode.CREATE.equals(packet.getMode()) || Team.Mode.UPDATE_INFO.equals(packet.getMode())) {
                    teamEntry.setDisplayName(packet.getDisplayName());
                    teamEntry.setPrefix(packet.getPrefix());
                    teamEntry.setSuffix(packet.getSuffix());
                    teamEntry.setFriendlyFire(packet.getFriendlyFire());
                    teamEntry.setNameTagVisibility(packet.getNameTagVisibility());
                    teamEntry.setCollisionRule(packet.getCollisionRule());
                    teamEntry.setColor(packet.getColor());
                }
                if (packet.getPlayers() != null) {
                    for (String s : packet.getPlayers()) {
                        if (Team.Mode.CREATE.equals(packet.getMode()) || Team.Mode.ADD_PLAYER.equals(packet.getMode())) {
                            if (playerToTeamMap.containsKey(s)) {
                                TeamEntry previousTeam = serverTeams.get(playerToTeamMap.get(s));
                                // previousTeam shouldn't be null (that's inconsistent with playerToTeamMap, but apparently it happens)
                                if (previousTeam != null) {
                                    previousTeam.removePlayer(s);
                                }
                            }
                            teamEntry.addPlayer(s);
                            playerToTeamMap.put(s, packet.getName());
                        } else {
                            teamEntry.removePlayer(s);
                            playerToTeamMap.remove(s, packet.getName());
                        }
                    }
                }
            }
        }

        try {
            return this.activeContentHandler.onTeamPacket(packet);
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "Unexpected error", th);
            // try recover
            enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
            return PacketListenerResult.PASS;
        }
    }

    @Override
    public PacketListenerResult onPlayerListHeaderFooterPacket(HeaderAndFooterPacket packet) {
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

        this.serverHeader = packet.getHeader() != null ? packet.getHeader().getComponent() : Component.empty();
        this.serverFooter = packet.getFooter() != null ? packet.getFooter().getComponent() : Component.empty();

        return result;
    }

    @Override
    public void onServerSwitch(boolean is13OrLater) {
        this.is13OrLater = is13OrLater;
        if (!active) {
            active = true;
            update();
        } else {

            serverTeams.clear();
            playerToTeamMap.clear();

            if (isUsingAltRespawn()) {
                hasCreatedCustomTeams = false;
            }

            try {
                this.activeContentHandler.onServerSwitch();
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Unexpected error", th);
                // try recover
                enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
            }
            try {
                this.activeHeaderFooterHandler.onServerSwitch();
            } catch (Throwable th) {
                logger.log(Level.SEVERE, "Unexpected error", th);
                // try recover
                enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
            }

            if (!serverPlayerList.isEmpty()) {
                List<LegacyPlayerListItemPacket.Item> items = new ArrayList<>();
                for(UUID uuid : serverPlayerList.keySet()){
                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(uuid);
                    items.add(item);
                }
                LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(REMOVE_PLAYER, items);
                sendPacket(packet);
            }

            serverPlayerList.clear();
            if (serverHeader != null) {
                serverHeader = Component.empty();
            }
            if (serverFooter != null) {
                serverFooter = Component.empty();
            }

            serverTabListPlayers.clear();
        }
    }

    protected boolean isUsingAltRespawn() {
        return false;
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
        if (!active) {
            return;
        }
        updateScheduledFlag.set(false);

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

    private abstract class AbstractContentOperationModeHandler<T extends AbstractContentTabOverlay> extends OperationModeHandler<T> {

        /**
         * Called when the player receives a {@link LegacyPlayerListItemPacket} packet.
         * <p>
         * This method is called after this {@link AbstractTabOverlayHandler} has updated the {@code serverPlayerList}.
         */
        abstract PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet);

        /**
         * Called when the player receives a {@link Team} packet.
         * <p>
         * This method is called before this {@link AbstractTabOverlayHandler} executes its own logic to update the
         * server team info.
         */
        abstract void onTeamPacketPreprocess(Team packet);

        /**
         * Called when the player receives a {@link Team} packet.
         * <p>
         * This method is called after this {@link AbstractTabOverlayHandler} executes its own logic to update the
         * server team info.
         */
        abstract PacketListenerResult onTeamPacket(Team packet);

        /**
         * Called when the player switches the server.
         * <p>
         * This method is called before this {@link AbstractTabOverlayHandler} executes its own logic to clear the
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
         * - all entries from {@link #serverPlayerList} are known to the client, but the client may know the wrong displayname, gamemode and ping
         * - player list header/ footer may be wrong
         * <p>
         * Additional information about the state of the player list may be obtained from the previous handler
         *
         * @param previous previous handler
         */
        abstract void onActivated(AbstractContentOperationModeHandler<?> previous);
    }

    private abstract class AbstractHeaderFooterOperationModeHandler<T extends AbstractHeaderFooterTabOverlay> extends OperationModeHandler<T> {

        /**
         * Called when the player receives a {@link HeaderAndFooterPacket} packet.
         * <p>
         * This method is called before this {@link AbstractTabOverlayHandler} executes its own logic to update the
         * server player list info.
         */
        abstract PacketListenerResult onPlayerListHeaderFooterPacket(HeaderAndFooterPacket packet);

        /**
         * Called when the player switches the server.
         * <p>
         * This method is called before this {@link AbstractTabOverlayHandler} executes its own logic to clear the
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
         * - all entries from {@link #serverPlayerList} are known to the client, but the client may know the wrong displayname, gamemode and ping
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
        PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        void onTeamPacketPreprocess(Team packet) {
            // nothing to do
        }

        @Override
        PacketListenerResult onTeamPacket(Team packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        void onServerSwitch() {
            sendPacket(HeaderAndFooterPacket.create(Component.empty(), Component.empty(), getProtocol()));
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

            // fix player list entries
            if (!serverPlayerList.isEmpty()) {
                // restore player ping
                LegacyPlayerListItemPacket packet;
                List<LegacyPlayerListItemPacket.Item> items = new ArrayList<>(serverPlayerList.size());
                for (PlayerListEntry entry : serverPlayerList.values()) {
                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(entry.getUuid());
                    item.setLatency(entry.getPing());
                    items.add(item);
                }
                packet = new LegacyPlayerListItemPacket(UPDATE_LATENCY, items);
                sendPacket(packet);

                // restore player gamemode
                items.clear();
                for (PlayerListEntry entry : serverPlayerList.values()) {
                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(entry.getUuid());
                    item.setGameMode(entry.getGamemode());
                    items.add(item);
                }
                packet = new LegacyPlayerListItemPacket(UPDATE_GAMEMODE, items);
                sendPacket(packet);

                // restore player display name
                items.clear();
                for (PlayerListEntry entry : serverPlayerList.values()) {
                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(entry.getUuid());
                    item.setDisplayName(entry.getDisplayName());
                    items.add(item);
                }
                packet = new LegacyPlayerListItemPacket(UPDATE_DISPLAY_NAME, items);
                sendPacket(packet);
            }
        }
    }

    private final class PassThroughContentTabOverlay extends AbstractContentTabOverlay {

    }

    private final class PassThroughHeaderFooterHandler extends AbstractHeaderFooterOperationModeHandler<PassThroughHeaderFooterTabOverlay> {

        @Override
        protected PassThroughHeaderFooterTabOverlay createTabOverlay() {
            return new PassThroughHeaderFooterTabOverlay();
        }

        @Override
        PacketListenerResult onPlayerListHeaderFooterPacket(HeaderAndFooterPacket packet) {
            return PacketListenerResult.PASS;
        }

        @Override
        void onServerSwitch() {
            sendPacket(HeaderAndFooterPacket.create(Component.empty(), Component.empty(), getProtocol()));
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
            sendPacket(HeaderAndFooterPacket.create(serverHeader != null ? serverHeader : Component.empty(), serverFooter != null ? serverFooter : Component.empty(), getProtocol()));
        }
    }

    private final class PassThroughHeaderFooterTabOverlay extends AbstractHeaderFooterTabOverlay {

    }

    private abstract class CustomContentTabOverlayHandler<T extends CustomContentTabOverlay> extends AbstractContentOperationModeHandler<T> {

        boolean viewerIsSpectator = false;
        final ObjectSet<UUID> freePlayers;

        @Nonnull
        BitSet usedSlots;
        BitSet dirtySlots;
        int highestUsedSlotIndex;
        private boolean using80Slots;
        private int usedSlotsCount;
        final SlotState[] slotState;
        /**
         * Uuid of the player list entry used for the slot.
         */
        final UUID[] slotUuid;
        /**
         * Username of the player list entry used for the slot.
         */
        final String[] slotUsername;
        /**
         * Player uuid mapped to the slot it is used for
         */
        final Object2IntMap<UUID> playerUuidToSlotMap;
        /**
         * Player username mapped to the slot it is used for
         */
        final Object2IntMap<String> playerUsernameToSlotMap;
        boolean canShrink = false;

        private final List<LegacyPlayerListItemPacket.Item> itemQueueAddPlayer;
        private final List<LegacyPlayerListItemPacket.Item> itemQueueRemovePlayer;
        private final List<LegacyPlayerListItemPacket.Item> itemQueueUpdateDisplayName;
        private final List<LegacyPlayerListItemPacket.Item> itemQueueUpdatePing;

        private final boolean experimentalTabCompleteFixForTabSize80 = isExperimentalTabCompleteFixForTabSize80();
        private final boolean experimentalTabCompleteSmileys = isExperimentalTabCompleteSmileys();

        private CustomContentTabOverlayHandler() {
            this.dirtySlots = new BitSet(80);
            this.usedSlots = SIZE_TO_USED_SLOTS[0];
            this.usedSlotsCount = 0;
            this.using80Slots = false;
            this.slotState = new SlotState[80];
            Arrays.fill(this.slotState, SlotState.UNUSED);
            this.slotUuid = new UUID[80];
            this.slotUsername = new String[80];
            this.highestUsedSlotIndex = -1;
            this.freePlayers = new ObjectOpenHashSet<>();
            this.playerUuidToSlotMap = new Object2IntOpenHashMap<>();
            this.playerUuidToSlotMap.defaultReturnValue(-1);
            this.playerUsernameToSlotMap = new Object2IntOpenHashMap<>();
            this.playerUsernameToSlotMap.defaultReturnValue(-1);
            this.itemQueueAddPlayer = new ArrayList<>(80);
            this.itemQueueRemovePlayer = new ArrayList<>(80);
            this.itemQueueUpdateDisplayName = new ArrayList<>(80);
            this.itemQueueUpdatePing = new ArrayList<>(80);
        }

        @Override
        PacketListenerResult onPlayerListPacket(LegacyPlayerListItemPacket packet) {

            int action = packet.getAction();

            if (using80Slots && action == UPDATE_GAMEMODE) {
                return PacketListenerResult.PASS;
            }

            // check whether viewer gamemode changed
            boolean viewerGamemodeChanged = false;
            T tabOverlay = getTabOverlay();
            if (action == ADD_PLAYER || action == UPDATE_GAMEMODE || action == REMOVE_PLAYER) {
                PlayerListEntry entry = serverPlayerList.get(viewerUuid);
                boolean viewerIsSpectator = entry != null && entry.getGamemode() == 3;
                if (this.viewerIsSpectator != viewerIsSpectator) {
                    this.viewerIsSpectator = viewerIsSpectator;
                    if (!using80Slots) {
                        if (highestUsedSlotIndex >= 0) {
                            dirtySlots.set(highestUsedSlotIndex);
                        }

                        if (viewerIsSpectator) {
                            // mark player slot as dirty
                            int i = playerUuidToSlotMap.getInt(viewerUuid);
                            if (i >= 0) {
                                dirtySlots.set(i);
                            }
                        } else {
                            // mark slots with player uuid as dirty
                            if (action == UPDATE_GAMEMODE) {
                                // if action is ADD_PLAYER slots are marked dirty below, so only do it here if action is UPDATE_GAMEMODE
                                for (int slot = 0; slot < 80; slot++) {
                                    UUID uuid = tabOverlay.uuid[slot];
                                    if (viewerUuid.equals(uuid)) {
                                        dirtySlots.set(slot);
                                    }
                                }
                            }
                        }
                    }
                    viewerGamemodeChanged = true;
                }
            }

            PacketListenerResult result;
            boolean needUpdate = !using80Slots && viewerGamemodeChanged;

            switch (action) {
                case ADD_PLAYER:
                    List<LegacyPlayerListItemPacket.Item> items = packet.getItems();
                    if (!using80Slots) {
                        for (LegacyPlayerListItemPacket.Item item : items) {
                            if (!viewerUuid.equals(item.getUuid())) {
                                item.setGameMode(0);
                            }
                        }

                        for (LegacyPlayerListItemPacket.Item item : items) {
                            UUID uuid = item.getUuid();
                            int index = playerUuidToSlotMap.getInt(uuid);
                            if (index == -1) {
                                freePlayers.add(uuid);
                                needUpdate = true;
                            } else if (!item.getName().equals(slotUsername[index])) {
                                dirtySlots.set(index);
                                needUpdate = true;
                            } else {
                                item.setDisplayName(tabOverlay.text[index]);
                                item.setLatency(tabOverlay.ping[index]);
                                tabOverlay.dirtyFlagsText.clear(index);
                                tabOverlay.dirtyFlagsPing.clear(index);
                            }
                        }

                        // mark slot to use for player as dirty
                        // a new player joining the server shouldn't happen too frequently, so we can accept
                        // the cost of searching all 80 slots
                        if (!freePlayers.isEmpty()) {
                            for (int slot = 0; slot < 80; slot++) {
                                UUID uuid = tabOverlay.uuid[slot];
                                if (uuid != null && freePlayers.contains(uuid)) {
                                    dirtySlots.set(slot);
                                }
                            }
                        }

                        // request size update if tab list too small
                        if (usedSlotsCount < serverPlayerList.size()) {
                            tabOverlay.dirtyFlagSize = true;
                        }
                    } else {
                        for (LegacyPlayerListItemPacket.Item item : packet.getItems()) {
                            if (!playerToTeamMap.containsKey(item.getName())) {
                                sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{item.getName()}));
                            }
                        }
                    }
                    if (needUpdate) {
                        sendPacket(packet);
                        result = PacketListenerResult.CANCEL;
                    } else {
                        result = PacketListenerResult.MODIFIED;
                    }
                    break;
                case UPDATE_GAMEMODE:
                    if (viewerGamemodeChanged) {
                        items = packet.getItems();
                        for (LegacyPlayerListItemPacket.Item item : items) {
                            if (!viewerUuid.equals(item.getUuid())) {
                                item.setGameMode(0);
                            }
                        }
                        sendPacket(packet);
                    }
                    result = PacketListenerResult.CANCEL;
                    break;
                case UPDATE_LATENCY:
                    result = PacketListenerResult.CANCEL;
                    break;
                case UPDATE_DISPLAY_NAME:
                    result = PacketListenerResult.CANCEL;
                    break;
                case REMOVE_PLAYER:
                    if (!using80Slots) {
                        items = packet.getItems();
                        for (LegacyPlayerListItemPacket.Item item : items) {
                            int index = playerUuidToSlotMap.removeInt(item.getUuid());
                            if (index == -1) {
                                if (OPTION_ENABLE_CONSISTENCY_CHECKS) {
                                    if (serverPlayerList.containsKey(item.getUuid())) {
                                        logger.severe("Inconsistent data: player in serverPlayerList but not in playerUuidToSlotMap");
                                    }
                                }
                            } else {
                                // Switch slot 'index' from player to custom mode - restoring player teams

                                // 1. remove player from team
                                if (item.getUuid().version() != 2) { // hack for Citizens compatibility
                                    sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{slotUsername[index]}));
                                    playerUsernameToSlotMap.removeInt(slotUsername[index]);
                                    String playerTeamName;
                                    if ((playerTeamName = playerToTeamMap.get(slotUsername[index])) != null) {
                                        // 2. add player to correct team
                                        sendPacket(createPacketTeamAddPlayers(playerTeamName, new String[]{slotUsername[index]}));
                                        // 3. reset custom slot team
                                        sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                                    }
                                }

                                // 4. create new custom slot
                                tabOverlay.dirtyFlagsIcon.clear(index);
                                tabOverlay.dirtyFlagsText.clear(index);
                                tabOverlay.dirtyFlagsPing.clear(index);
                                Icon icon = tabOverlay.icon[index];
                                UUID customSlotUuid;
                                if (icon.isAlex()) {
                                    customSlotUuid = CUSTOM_SLOT_UUID_ALEX[index];
                                } else {
                                    customSlotUuid = CUSTOM_SLOT_UUID_STEVE[index];
                                }
                                slotState[index] = SlotState.CUSTOM;
                                slotUuid[index] = customSlotUuid;
                                LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(customSlotUuid);
                                item1.setName(slotUsername[index] = getCustomSlotUsername(index));
                                Property119Handler.setProperties(item1, toPropertiesArray(icon.getTextureProperty()));
                                item1.setDisplayName(tabOverlay.text[index]);
                                item1.setLatency(tabOverlay.ping[index]);
                                item1.setGameMode(0);
                                LegacyPlayerListItemPacket packet1 = new LegacyPlayerListItemPacket(ADD_PLAYER, List.of(item1));
                                sendPacket(packet1);
                                if (is18) {
                                    packet1 = new LegacyPlayerListItemPacket(UPDATE_DISPLAY_NAME, List.of(item1));
                                    sendPacket(packet1);
                                }
                            }
                        }
                    }
                    if (canShrink) {
                        sendPacket(packet);
                        tabOverlay.dirtyFlagSize = true;
                        needUpdate = true;
                        result = PacketListenerResult.CANCEL;
                    } else {
                        result = PacketListenerResult.PASS;
                    }
                    break;
                default:
                    throw new AssertionError("Unknown action: " + action);
            }
            if (needUpdate) {
                update();
            }
            return result;
        }

        private String getCustomSlotUsername(int index) {
            if (experimentalTabCompleteFixForTabSize80 && using80Slots) {
                return "";
            }
            if (experimentalTabCompleteSmileys) {
                return CUSTOM_SLOT_USERNAME_SMILEYS[index];
            } else {
                return CUSTOM_SLOT_USERNAME[index];
            }
        }

        @Override
        void onTeamPacketPreprocess(Team packet) {
            if (!using80Slots) {
                if (Team.Mode.REMOVE.equals(packet.getMode())) {
                    TeamEntry teamEntry = serverTeams.get(packet.getName());
                    if (teamEntry != null) {
                        for (String playerName : teamEntry.getPlayers()) {
                            int slot = playerUsernameToSlotMap.getInt(playerName);
                            if (slot != -1) {
                                // reset slot team
                                sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[slot], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                            }
                        }
                    }
                }
            } else {
                if (Team.Mode.REMOVE.equals(packet.getMode())) {
                    TeamEntry teamEntry = serverTeams.get(packet.getName());
                    if (teamEntry != null) {
                        for (String playerName : teamEntry.getPlayers()) {
                            if (serverTabListPlayers.contains(playerName)) {
                                // reset slot team
                                sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{playerName}));
                            }
                        }
                    }
                }
            }
        }

        @Override
        PacketListenerResult onTeamPacket(Team packet) {
            if (CUSTOM_SLOT_TEAMNAMES.contains(packet.getName())) {
                throw new AssertionError("Team name collision: " + packet);
            }
            if (!using80Slots) {
                boolean modified = false;
                switch (packet.getMode()) {
                    case CREATE:
                    case ADD_PLAYER:
                        int count = 0;
                        String[] players = packet.getPlayers();
                        for (int i = 0; i < players.length; i++) {
                            String playerName = players[i];
                            if (-1 == playerUsernameToSlotMap.getInt(playerName)) {
                                count++;
                            }
                        }
                        if (count < players.length) {
                            modified = true;
                            String[] filteredPlayers = new String[count];
                            int j = 0;
                            for (int i = 0; i < players.length; i++) {
                                String playerName = players[i];
                                int slot;
                                if (-1 == (slot = playerUsernameToSlotMap.getInt(playerName))) {
                                    filteredPlayers[j++] = playerName;
                                } else {
                                    // update slot team
                                    TeamEntry teamEntry = serverTeams.get(packet.getName());
                                    if (teamEntry != null) {
                                        sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[slot], teamEntry.getDisplayName(), teamEntry.getPrefix(), teamEntry.getSuffix(), teamEntry.getNameTagVisibility(), teamEntry.getCollisionRule(), teamEntry.getColor(), teamEntry.getFriendlyFire()));
                                    }
                                }
                            }
                            packet.setPlayers(filteredPlayers);
                        }
                        break;
                    case REMOVE_PLAYER:
                        count = 0;
                        players = packet.getPlayers();
                        for (int i = 0; i < players.length; i++) {
                            String playerName = players[i];
                            if (-1 == playerUsernameToSlotMap.getInt(playerName)) {
                                count++;
                            }
                        }
                        if (count < players.length) {
                            modified = true;
                            String[] filteredPlayers = new String[count];
                            int j = 0;
                            for (int i = 0; i < players.length; i++) {
                                String playerName = players[i];
                                int slot;
                                if (-1 == (slot = playerUsernameToSlotMap.getInt(playerName))) {
                                    filteredPlayers[j++] = playerName;
                                } else {
                                    // reset slot team
                                    sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[slot], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                                }
                            }
                            packet.setPlayers(filteredPlayers);
                        }
                        break;
                    case UPDATE_INFO:
                        TeamEntry teamEntry = serverTeams.get(packet.getName());
                        if (teamEntry != null) {
                            for (String playerName : teamEntry.getPlayers()) {
                                int slot = playerUsernameToSlotMap.getInt(playerName);
                                if (slot != -1) {
                                    sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[slot], teamEntry.getDisplayName(), teamEntry.getPrefix(), teamEntry.getSuffix(), teamEntry.getNameTagVisibility(), teamEntry.getCollisionRule(), teamEntry.getColor(), teamEntry.getFriendlyFire()));
                                }
                            }
                        }

                        break;

                }
                if (modified) {
                    return PacketListenerResult.MODIFIED;
                }
            } else {

                switch (packet.getMode()) {
                    case CREATE:
                    case ADD_PLAYER:
                        /*
                        // Don't need this. Adding the player to another team will remove him from the current one.
                        String[] players = packet.getPlayers();
                        for (int i = 0; i < players.length; i++) {
                            String playerName = players[i];
                            if (serverTabListPlayers.contains(playerName)) {
                                // remove player from overflow team
                                sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{playerName}));
                            }
                        }*/
                        break;
                    case REMOVE_PLAYER:
                        String[] players = packet.getPlayers();
                        for (int i = 0; i < players.length; i++) {
                            String playerName = players[i];
                            if (serverTabListPlayers.contains(playerName)) {
                                // add player to overflow team
                                sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{playerName}));
                            }
                        }
                        break;
                }
            }
            return PacketListenerResult.PASS;
        }

        @Override
        void onServerSwitch() {
            boolean altRespawn = isUsingAltRespawn();

            if (altRespawn) {
                createTeamsIfNecessary();
            }

            if (!using80Slots) {
                T tabOverlay = getTabOverlay();

                // all players are gone
                for (int index = 0; index < 80; index++) {
                    if (slotState[index] == SlotState.PLAYER) {
                        // Switch slot 'index' from player to custom mode

                        if (!altRespawn) {
                            // 1. remove player from team
                            sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{slotUsername[index]}));
                            // reset slot team
                            sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                        }

                        // 2. create new custom slot
                        tabOverlay.dirtyFlagsIcon.clear(index);
                        tabOverlay.dirtyFlagsText.clear(index);
                        tabOverlay.dirtyFlagsPing.clear(index);
                        Icon icon = tabOverlay.icon[index];
                        UUID customSlotUuid;
                        if (icon.isAlex()) {
                            customSlotUuid = CUSTOM_SLOT_UUID_ALEX[index];
                        } else {
                            customSlotUuid = CUSTOM_SLOT_UUID_STEVE[index];
                        }
                        slotState[index] = SlotState.CUSTOM;
                        slotUuid[index] = customSlotUuid;
                        LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(customSlotUuid);
                        item1.setName(slotUsername[index] = getCustomSlotUsername(index));
                        Property119Handler.setProperties(item1, toPropertiesArray(icon.getTextureProperty()));
                        item1.setDisplayName(tabOverlay.text[index]);
                        item1.setLatency(tabOverlay.ping[index]);
                        item1.setGameMode(0);
                        LegacyPlayerListItemPacket packet1 = new LegacyPlayerListItemPacket(ADD_PLAYER, List.of(item1));
                        sendPacket(packet1);
                        if (is18) {
                            packet1 = new LegacyPlayerListItemPacket(UPDATE_DISPLAY_NAME, List.of(item1));
                            sendPacket(packet1);
                        }
                    }
                }
                freePlayers.clear();
                playerUuidToSlotMap.clear();
                playerUsernameToSlotMap.clear();
            }
            viewerIsSpectator = false;
        }

        @Override
        void onActivated(AbstractContentOperationModeHandler<?> previous) {
            if (previous instanceof CustomContentTabOverlayHandler<?>) {
                this.viewerIsSpectator = ((CustomContentTabOverlayHandler) previous).viewerIsSpectator;
            } else {
                PlayerListEntry viewerEntry = serverPlayerList.get(viewerUuid);
                this.viewerIsSpectator = viewerEntry != null && viewerEntry.getGamemode() == 3;

                // switch all players except for viewer in survival mode if they are in spectator mode
                if (!using80Slots) {
                    int count = 0;
                    for (PlayerListEntry entry : serverPlayerList.values()) {
                        if (entry != viewerEntry && entry.getGamemode() == 3) {
                            count++;
                        }
                    }

                    if (count > 0) {
                        LegacyPlayerListItemPacket.Item[] items = new LegacyPlayerListItemPacket.Item[count];
                        int index = 0;

                        for (Map.Entry<UUID, PlayerListEntry> mEntry : serverPlayerList.entrySet()) {
                            PlayerListEntry entry = mEntry.getValue();
                            if (entry != viewerEntry && entry.getGamemode() == 3) {
                                LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(mEntry.getKey());
                                item.setGameMode(0);
                                items[index++] = item;
                            }
                        }

                        LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(UPDATE_GAMEMODE, Arrays.asList(items));
                        sendPacket(packet);
                    }
                }
                createTeamsIfNecessary();

            }

            if (!using80Slots) {
                this.freePlayers.addAll(serverPlayerList.keySet());

                if (!this.freePlayers.isEmpty()) {

                    for (PlayerListEntry entry : serverPlayerList.values()) {
                        if (!playerToTeamMap.containsKey(entry.username)) {
                            sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{entry.username}));
                        }
                    }
                    getTabOverlay().dirtyFlagSize = true;
                }
            }
        }

        private void createTeamsIfNecessary() {
            // create teams if not already created
            if (!hasCreatedCustomTeams) {
                hasCreatedCustomTeams = true;

                sendPacket(createPacketTeamCreate(CUSTOM_SLOT_TEAMNAME[0], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1, new String[]{CUSTOM_SLOT_USERNAME[0], CUSTOM_SLOT_USERNAME_SMILEYS[0], ""}));

                for (int i = 1; i < 80; i++) {
                    sendPacket(createPacketTeamCreate(CUSTOM_SLOT_TEAMNAME[i], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1, new String[]{CUSTOM_SLOT_USERNAME[i], CUSTOM_SLOT_USERNAME_SMILEYS[i]}));
                }
                sendPacket(createPacketTeamCreate(CUSTOM_SLOT_TEAMNAME[80], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1, new String[]{CUSTOM_SLOT_USERNAME[80]}));
            }
        }

        @Override
        void onDeactivated() {
            int customSlots = 0;
            for (int index = 0; index < 80; index++) {
                if (slotState[index] != SlotState.UNUSED) {
                    if (slotState[index] == SlotState.PLAYER) {
                        // switch slot from player to unused permanently freeing the associated player

                        // 1. remove player from team
                        sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{slotUsername[index]}));
                        String playerTeamName;
                        if ((playerTeamName = playerToTeamMap.get(slotUsername[index])) != null) {
                            // 2. add player to correct team
                            sendPacket(createPacketTeamAddPlayers(playerTeamName, new String[]{slotUsername[index]}));
                            // 3. reset custom slot team
                            sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                        }
                    } else {
                        customSlots++;
                    }
                }
            }

            if (using80Slots) {
                for (String player : serverTabListPlayers) {
                    if (playerToTeamMap.get(player) == null) {
                        // remove player from overflow team
                        sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{player}));
                    }
                }
                // account for spacer players
                if (experimentalTabCompleteFixForTabSize80) {
                    customSlots += 17;
                }
            }

            int i = 0;
            if (customSlots > 0) {
                LegacyPlayerListItemPacket.Item[] items = new LegacyPlayerListItemPacket.Item[customSlots];
                for (int index = 0; index < 80; index++) {
                    // switch slot from custom to unused
                    if (slotState[index] == SlotState.CUSTOM) {
                        LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                        items[i++] = item;
                    }
                }
                if (experimentalTabCompleteFixForTabSize80 && using80Slots) {
                    for (int j = 0; j < 17; j++) {
                        LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(CUSTOM_SLOT_UUID_SPACER[j]);
                        items[i++] = item;
                    }
                }
                LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(REMOVE_PLAYER, Arrays.asList(items));
                sendPacket(packet);
            }
        }

        @Override
        void update() {
            T tabOverlay = getTabOverlay();

            if (OPTION_ENABLE_CONSISTENCY_CHECKS) {
                if (!using80Slots && usedSlotsCount < serverPlayerList.size() && !tabOverlay.dirtyFlagSize) {
                    logger.severe("tabOverlay.dirtyFlagSize not set but resize required");
                    tabOverlay.dirtyFlagSize = true;
                }
            }

            boolean updateAllCustomSlots = false;

            if (tabOverlay.dirtyFlagSize) {
                tabOverlay.dirtyFlagSize = false;
                updateSize();
                if (usedSlotsCount != (usedSlotsCount = usedSlots.cardinality())) {

                    if (using80Slots) {
                        // when previously using 80 slots
                        for (int index = 0; index < 80; index++) {
                            if (tabOverlay.uuid[index] != null) {
                                dirtySlots.set(index);
                            }
                        }
                        freePlayers.addAll(serverPlayerList.keySet());

                        // switch all players except for viewer in survival mode if they are in spectator mode
                        PlayerListEntry viewerEntry = serverPlayerList.get(viewerUuid);
                        int count = 0;
                        for (PlayerListEntry entry : serverPlayerList.values()) {
                            if (entry != viewerEntry && entry.getGamemode() == 3) {
                                count++;
                            }
                        }

                        if (count > 0) {
                            LegacyPlayerListItemPacket.Item[] items = new LegacyPlayerListItemPacket.Item[count];
                            int index = 0;

                            for (Map.Entry<UUID, PlayerListEntry> mEntry : serverPlayerList.entrySet()) {
                                PlayerListEntry entry = mEntry.getValue();
                                if (entry != viewerEntry && entry.getGamemode() == 3) {
                                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(mEntry.getKey());
                                    item.setGameMode(0);
                                    items[index++] = item;
                                }
                            }

                            LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(UPDATE_GAMEMODE, Arrays.asList(items));
                            sendPacket(packet);
                        }

                        // remove spacer slots
                        if (experimentalTabCompleteFixForTabSize80) {
                            for (int i = 0; i < 17; i++) {
                                LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(CUSTOM_SLOT_UUID_SPACER[i]);
                                itemQueueRemovePlayer.add(item1);
                            }
                        }

                        dirtySlots.set(0, 80);
                        updateAllCustomSlots = true;
                    } else if (viewerIsSpectator && highestUsedSlotIndex >= 0) {
                        dirtySlots.set(highestUsedSlotIndex);
                    }

                    highestUsedSlotIndex = usedSlots.previousSetBit(79);
                    using80Slots = this.usedSlotsCount == 80;
                    if (using80Slots) {
                        // we switched to 80 slots
                        for (int index = 0; index < 80; index++) {
                            if (slotState[index] == SlotState.PLAYER) {

                                // 1. remove player from team
                                sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{slotUsername[index]}));
                                playerUsernameToSlotMap.removeInt(slotUsername[index]);
                                String playerTeamName;
                                if ((playerTeamName = playerToTeamMap.get(slotUsername[index])) != null) {
                                    // 2. add player to correct team
                                    sendPacket(createPacketTeamAddPlayers(playerTeamName, new String[]{slotUsername[index]}));
                                    // 3. reset custom slot team
                                    sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                                } else {
                                    // 2. add player to overflow team
                                    sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{slotUsername[index]}));
                                }

                                // 4. create new custom slot
                                tabOverlay.dirtyFlagsIcon.clear(index);
                                tabOverlay.dirtyFlagsText.clear(index);
                                tabOverlay.dirtyFlagsPing.clear(index);
                                Icon icon = tabOverlay.icon[index];
                                UUID customSlotUuid;
                                if (icon.isAlex()) {
                                    customSlotUuid = CUSTOM_SLOT_UUID_ALEX[index];
                                } else {
                                    customSlotUuid = CUSTOM_SLOT_UUID_STEVE[index];
                                }
                                slotState[index] = SlotState.CUSTOM;
                                slotUuid[index] = customSlotUuid;
                                LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(customSlotUuid);
                                item1.setName(slotUsername[index] = getCustomSlotUsername(index));
                                Property119Handler.setProperties(item1, toPropertiesArray(icon.getTextureProperty()));
                                item1.setDisplayName(tabOverlay.text[index]);
                                item1.setLatency(tabOverlay.ping[index]);
                                item1.setGameMode(0);
                                itemQueueAddPlayer.add(item1);
                            } else {
                                // custom
                                if (slotState[index] == SlotState.CUSTOM) {
                                    LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                                    itemQueueRemovePlayer.add(item1);
                                }
                                // unused
                                tabOverlay.dirtyFlagsIcon.clear(index);
                                tabOverlay.dirtyFlagsText.clear(index);
                                tabOverlay.dirtyFlagsPing.clear(index);
                                Icon icon = tabOverlay.icon[index];
                                UUID customSlotUuid;
                                if (icon.isAlex()) {
                                    customSlotUuid = CUSTOM_SLOT_UUID_ALEX[index];
                                } else {
                                    customSlotUuid = CUSTOM_SLOT_UUID_STEVE[index];
                                }
                                slotState[index] = SlotState.CUSTOM;
                                slotUuid[index] = customSlotUuid;
                                LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(customSlotUuid);
                                item1.setName(slotUsername[index] = getCustomSlotUsername(index));
                                Property119Handler.setProperties(item1, toPropertiesArray(icon.getTextureProperty()));
                                item1.setDisplayName(tabOverlay.text[index]);
                                item1.setLatency(tabOverlay.ping[index]);
                                item1.setGameMode(0);
                                itemQueueAddPlayer.add(item1);
                            }
                        }

                        // restore player gamemode
                        LegacyPlayerListItemPacket packet;
                        List<LegacyPlayerListItemPacket.Item> items = new ArrayList<>(serverPlayerList.size());
                        for (PlayerListEntry entry : serverPlayerList.values()) {
                            LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(entry.getUuid());
                            item.setGameMode(entry.getGamemode());
                            items.add(item);
                        }
                        packet = new LegacyPlayerListItemPacket(UPDATE_GAMEMODE, items);
                        sendPacket(packet);

                        for (UUID player : freePlayers) {
                            String username = serverPlayerList.get(player).username;
                            String playerTeamName = playerToTeamMap.get(username);
                            if (playerTeamName != null) {
                                // add player to correct team
                                sendPacket(createPacketTeamAddPlayers(playerTeamName, new String[]{username}));
                            } else {
                                // add player to overflow team
                                sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[80], new String[]{username}));
                            }
                        }

                        //  create spacer slots
                        if (experimentalTabCompleteFixForTabSize80) {
                            for (int i = 0; i < 17; i++) {
                                LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(CUSTOM_SLOT_UUID_SPACER[i]);
                                item1.setName("");
                                Property119Handler.setProperties(item1, EMPTY_PROPERTIES_ARRAY);
                                item1.setDisplayName(null);
                                item1.setLatency(0);
                                item1.setGameMode(0);
                                itemQueueAddPlayer.add(item1);
                            }
                        }

                        //  save some memory
                        freePlayers.clear();
                        playerUuidToSlotMap.clear();
                        playerUsernameToSlotMap.clear();
                    } else {

                        if (viewerIsSpectator && highestUsedSlotIndex >= 0) {
                            dirtySlots.set(highestUsedSlotIndex);
                        }
                    }

                    sendQueuedItems();
                }
            }


            if (OPTION_ENABLE_CONSISTENCY_CHECKS) {
                if (!using80Slots && usedSlotsCount < serverPlayerList.size()) {
                    throw new AssertionError("resize failed");
                }
            }

            if (OPTION_ENABLE_CONSISTENCY_CHECKS) {
                if (!using80Slots && freePlayers.size() + playerUuidToSlotMap.size() != serverPlayerList.size()) {
                    // inconsistent data -> rebuild
                    logger.severe("Detected inconsistency: freePlayers set or playerUuidToSlotMap is inconsistent");
                    freePlayers.clear();
                    freePlayers.addAll(serverPlayerList.keySet());
                    playerUuidToSlotMap.clear();
                    for (int index = 0; index <= highestUsedSlotIndex; index++) {
                        if (freePlayers.remove(slotUuid[index])) {
                            playerUuidToSlotMap.put(slotUuid[index], index);
                        }
                    }
                }
            }

            if (!using80Slots) {
                dirtySlots.orAndClear(tabOverlay.dirtyFlagsUuid);

                if (!dirtySlots.isEmpty() || !freePlayers.isEmpty()) {
                    // mark slots as dirty currently being used with the uuid of dirty slots
                    for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                        int i = index;
                        do {
                            UUID uuid = tabOverlay.uuid[i];
                            if (uuid == null) {
                                break;
                            }
                            i = playerUuidToSlotMap.getInt(uuid);
                            if (i == -1) {
                                break;
                            }
                            if (dirtySlots.get(i)) {
                                break;
                            } else {
                                dirtySlots.set(i);
                            }
                        } while (i < index);
                    }

                    if (OPTION_ENABLE_CONSISTENCY_CHECKS) {
                        if (viewerIsSpectator) {
                            int i;
                            if (highestUsedSlotIndex != (i = playerUuidToSlotMap.getInt(viewerUuid))) {
                                if (!dirtySlots.get(highestUsedSlotIndex)) {
                                    logger.severe("Spectator mode handling issue: highestUsedSlotIndex not marked as dirty");
                                    dirtySlots.set(highestUsedSlotIndex);
                                } else if (i == -1 && !freePlayers.contains(viewerUuid)) {
                                    logger.severe("Spectator mode handling issue: viewer neither in freePlayers set and nor in tab list");
                                } else if (i != -1 && !dirtySlots.get(i)) {
                                    logger.severe("Spectator mode handling issue: viewer slot not marked as dirty");
                                    dirtySlots.set(i);
                                }
                            }
                        }
                    }

                    // pass 1: free players
                    for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                        if (usedSlots.get(index)) {
                            if (slotState[index] == SlotState.PLAYER) {
                                // temporarily free associated player - set slot state to unused

                                // 1. remove player from team
                                if (slotUuid[index].version() != 2) { // dirty hack for Citizens compatibility
                                    sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{slotUsername[index]}));
                                    playerUsernameToSlotMap.removeInt(slotUsername[index]);

                                    // reset slot team
                                    sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                                }

                                // 2. update slot state
                                slotState[index] = SlotState.UNUSED;
                                freePlayers.add(slotUuid[index]);
                                slotUuid[index] = null;
                                slotUsername[index] = null;
                            }
                        } else {
                            if (slotState[index] != SlotState.UNUSED) {
                                // remove slot - free (temporarily) associated player if any - set slot state to unused
                                if (slotState[index] == SlotState.PLAYER) {
                                    // temporarily free associated player - set slot state to unused

                                    // 1. remove player from team
                                    if (slotUuid[index].version() != 2) { // dirty hack for Citizens compatibility
                                        sendPacket(createPacketTeamRemovePlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{slotUsername[index]}));
                                        playerUsernameToSlotMap.removeInt(slotUsername[index]);

                                        // reset slot team
                                        sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], EMPTY_COMPONENT, EMPTY_COMPONENT, EMPTY_COMPONENT, Team.NameTagVisibility.ALWAYS, Team.CollisionRule.ALWAYS, is13OrLater ? 21 : 0, (byte) 1));
                                    }

                                    freePlayers.add(slotUuid[index]);
                                } else {
                                    // 1. remove custom slot player
                                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                                    itemQueueRemovePlayer.add(item);
                                }

                                // 2. update slot state
                                slotState[index] = SlotState.UNUSED;
                                slotUuid[index] = null;
                                slotUsername[index] = null;
                            }
                        }
                    }

                    if (viewerIsSpectator && !viewerUuid.equals(slotUuid[highestUsedSlotIndex])) {
                        if (slotState[highestUsedSlotIndex] != SlotState.UNUSED) {
                            if (OPTION_ENABLE_CONSISTENCY_CHECKS) {
                                if (slotState[highestUsedSlotIndex] == SlotState.PLAYER) {
                                    throw new AssertionError("slotState[highestUsedSlotIndex] == SlotState.PLAYER");
                                }
                            }
                            // switch slot 'highestUsedSlotIndex' from custom to unused
                            LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUuid[highestUsedSlotIndex]);
                            itemQueueRemovePlayer.add(item);
                        }
                        // switch slot 'highestUsedSlotIndex' from unused to player with 'viewerUuid'
                        String playerUsername = serverPlayerList.get(viewerUuid).getUsername();
                        String playerTeamName;
                        if (null != (playerTeamName = playerToTeamMap.get(playerUsername))) {
                            // 1. remove player from old team
                            sendPacket(createPacketTeamRemovePlayers(playerTeamName, new String[]{playerUsername}));
                            // 2. update properties of new team
                            TeamEntry teamEntry = serverTeams.get(playerTeamName);
                            sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[highestUsedSlotIndex], teamEntry.getDisplayName(), teamEntry.getPrefix(), teamEntry.getSuffix(), teamEntry.getNameTagVisibility(), teamEntry.getCollisionRule(), teamEntry.getColor(), teamEntry.getFriendlyFire()));
                        }
                        // 3. Add to new team
                        sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[highestUsedSlotIndex], new String[]{playerUsername}));
                        // 4. Update display name
                        LegacyPlayerListItemPacket.Item itemUpdateDisplayName = new LegacyPlayerListItemPacket.Item(viewerUuid);
                        tabOverlay.dirtyFlagsText.clear(highestUsedSlotIndex);
                        itemUpdateDisplayName.setDisplayName(tabOverlay.text[highestUsedSlotIndex]);
                        itemQueueUpdateDisplayName.add(itemUpdateDisplayName);
                        // 5. Update ping
                        LegacyPlayerListItemPacket.Item itemUpdatePing = new LegacyPlayerListItemPacket.Item(viewerUuid);
                        tabOverlay.dirtyFlagsPing.clear(highestUsedSlotIndex);
                        itemUpdatePing.setLatency(tabOverlay.ping[highestUsedSlotIndex]);
                        itemQueueUpdatePing.add(itemUpdatePing);
                        // 6. Update slot state
                        slotState[highestUsedSlotIndex] = SlotState.PLAYER;
                        slotUuid[highestUsedSlotIndex] = viewerUuid;
                        slotUsername[highestUsedSlotIndex] = playerUsername;
                        playerUsernameToSlotMap.put(playerUsername, highestUsedSlotIndex);
                        playerUuidToSlotMap.put(viewerUuid, highestUsedSlotIndex);

                        freePlayers.remove(viewerUuid);
                    }

                    // pass 2: assign players to new slots
                    for (int repeat = 1; repeat > 0; repeat--) {
                        for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                            if (usedSlots.get(index) && slotState[index] != SlotState.PLAYER) {
                                UUID uuid = tabOverlay.uuid[index];
                                if (uuid != null && freePlayers.remove(uuid)) {
                                    // switch slot to player mode using player with 'uuid'
                                    if (slotState[index] == SlotState.CUSTOM) {
                                        // custom -> unused
                                        LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                                        itemQueueRemovePlayer.add(item);
                                    }
                                    String playerUsername = serverPlayerList.get(uuid).getUsername();
                                    String playerTeamName;
                                    if (null != (playerTeamName = playerToTeamMap.get(playerUsername))) {
                                        // 1. remove player from old team
                                        sendPacket(createPacketTeamRemovePlayers(playerTeamName, new String[]{playerUsername}));
                                        // 2. update properties of new team
                                        TeamEntry teamEntry = serverTeams.get(playerTeamName);
                                        sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], teamEntry.getDisplayName(), teamEntry.getPrefix(), teamEntry.getSuffix(), teamEntry.getNameTagVisibility(), teamEntry.getCollisionRule(), teamEntry.getColor(), teamEntry.getFriendlyFire()));
                                    }
                                    // 3. Add to new team
                                    sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{playerUsername}));
                                    // 4. Update display name
                                    LegacyPlayerListItemPacket.Item itemUpdateDisplayName = new LegacyPlayerListItemPacket.Item(uuid);
                                    tabOverlay.dirtyFlagsText.clear(index);
                                    itemUpdateDisplayName.setDisplayName(tabOverlay.text[index]);
                                    itemQueueUpdateDisplayName.add(itemUpdateDisplayName);
                                    // 5. Update ping
                                    LegacyPlayerListItemPacket.Item itemUpdatePing = new LegacyPlayerListItemPacket.Item(uuid);
                                    tabOverlay.dirtyFlagsPing.clear(index);
                                    itemUpdatePing.setLatency(tabOverlay.ping[index]);
                                    itemQueueUpdatePing.add(itemUpdatePing);
                                    // 6. Update slot state
                                    slotState[index] = SlotState.PLAYER;
                                    slotUuid[index] = uuid;
                                    slotUsername[index] = playerUsername;
                                    playerUsernameToSlotMap.put(playerUsername, index);
                                    playerUuidToSlotMap.put(uuid, index);
                                }
                            }
                        }

                        // should not happen too often
                        if (!freePlayers.isEmpty()) {
                            for (int slot = 0; slot < 80; slot++) {
                                UUID uuid;
                                if (slotState[slot] == SlotState.CUSTOM && (uuid = tabOverlay.uuid[slot]) != null && freePlayers.contains(uuid)) {
                                    dirtySlots.set(slot);
                                    repeat = 2;
                                }
                            }
                        }
                    }

                    // pass 3: distribute remaining 'freePlayers' on the tab list
                    int index = 80;
                    for (ObjectIterator<UUID> iterator = freePlayers.iterator(); iterator.hasNext(); ) {
                        UUID uuid = iterator.next();
                        for (index = usedSlots.previousSetBit(index - 1); index >= 0; index = usedSlots.previousSetBit(index - 1)) {
                            if (slotState[index] != SlotState.PLAYER) {
                                // switch slot to player mode using the player 'uuid'
                                if (slotState[index] == SlotState.CUSTOM) {
                                    // custom -> unused
                                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                                    itemQueueRemovePlayer.add(item);
                                }
                                String playerUsername = serverPlayerList.get(uuid).getUsername();
                                if (uuid.version() != 2) { // dirty hack for Citizens compatibility
                                    String playerTeamName;
                                    if (null != (playerTeamName = playerToTeamMap.get(playerUsername))) {
                                        // 1. remove player from old team
                                        sendPacket(createPacketTeamRemovePlayers(playerTeamName, new String[]{playerUsername}));
                                        // 2. update properties of new team
                                        TeamEntry teamEntry = serverTeams.get(playerTeamName);
                                        sendPacket(createPacketTeamUpdate(CUSTOM_SLOT_TEAMNAME[index], teamEntry.getDisplayName(), teamEntry.getPrefix(), teamEntry.getSuffix(), teamEntry.getNameTagVisibility(), teamEntry.getCollisionRule(), teamEntry.getColor(), teamEntry.getFriendlyFire()));
                                    }
                                    // 3. Add to new team
                                    sendPacket(createPacketTeamAddPlayers(CUSTOM_SLOT_TEAMNAME[index], new String[]{playerUsername}));
                                    playerUsernameToSlotMap.put(playerUsername, index);
                                }
                                // 4. Update display name
                                LegacyPlayerListItemPacket.Item itemUpdateDisplayName = new LegacyPlayerListItemPacket.Item(uuid);
                                tabOverlay.dirtyFlagsText.clear(index);
                                itemUpdateDisplayName.setDisplayName(tabOverlay.text[index]);
                                itemQueueUpdateDisplayName.add(itemUpdateDisplayName);
                                // 5. Update ping
                                LegacyPlayerListItemPacket.Item itemUpdatePing = new LegacyPlayerListItemPacket.Item(uuid);
                                tabOverlay.dirtyFlagsPing.clear(index);
                                itemUpdatePing.setLatency(tabOverlay.ping[index]);
                                itemQueueUpdatePing.add(itemUpdatePing);
                                // 6. Update slot state
                                slotState[index] = SlotState.PLAYER;
                                slotUuid[index] = uuid;
                                slotUsername[index] = playerUsername;
                                playerUuidToSlotMap.put(uuid, index);
                                iterator.remove();
                                break;
                            }
                        }
                        if (index < 0) {
                            throw new AssertionError("Not enough space on player list.");
                        }
                    }

                    // pass 4: switch some slots from unused to custom
                    for (index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                        if (usedSlots.get(index)) {
                            if (slotState[index] == SlotState.UNUSED || (updateAllCustomSlots && slotState[index] == SlotState.CUSTOM)) {
                                if (slotState[index] == SlotState.CUSTOM) {
                                    LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                                    itemQueueRemovePlayer.add(item1);
                                }
                                tabOverlay.dirtyFlagsIcon.clear(index);
                                tabOverlay.dirtyFlagsText.clear(index);
                                tabOverlay.dirtyFlagsPing.clear(index);
                                Icon icon = tabOverlay.icon[index];
                                UUID customSlotUuid;
                                if (icon.isAlex()) {
                                    customSlotUuid = CUSTOM_SLOT_UUID_ALEX[index];
                                } else {
                                    customSlotUuid = CUSTOM_SLOT_UUID_STEVE[index];
                                }
                                slotState[index] = SlotState.CUSTOM;
                                slotUuid[index] = customSlotUuid;
                                LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(customSlotUuid);
                                item1.setName(slotUsername[index] = getCustomSlotUsername(index));
                                Property119Handler.setProperties(item1, toPropertiesArray(icon.getTextureProperty()));
                                item1.setDisplayName(tabOverlay.text[index]);
                                item1.setLatency(tabOverlay.ping[index]);
                                item1.setGameMode(0);
                                itemQueueAddPlayer.add(item1);
                            }
                        }
                    }

                    // send first packet batch here to avoid conflicts when updating icons
                    sendQueuedItems();
                }
            }

            // update icons
            dirtySlots.copyAndClear(tabOverlay.dirtyFlagsIcon);
            for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                if (slotState[index] == SlotState.CUSTOM) {
                    Icon icon = tabOverlay.icon[index];
                    UUID customSlotUuid;
                    if (icon.hasTextureProperty()) {
                        customSlotUuid = slotUuid[index];
                    } else if (icon.isAlex()) {
                        customSlotUuid = CUSTOM_SLOT_UUID_ALEX[index];
                    } else { // steve
                        customSlotUuid = CUSTOM_SLOT_UUID_STEVE[index];
                    }
                    if (!customSlotUuid.equals(slotUuid[index]) || is119OrLater) {
                        LegacyPlayerListItemPacket.Item itemRemove = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                        itemQueueRemovePlayer.add(itemRemove);
                    }
                    tabOverlay.dirtyFlagsText.clear(index);
                    tabOverlay.dirtyFlagsPing.clear(index);
                    slotState[index] = SlotState.CUSTOM;
                    slotUuid[index] = customSlotUuid;
                    LegacyPlayerListItemPacket.Item item1 = new LegacyPlayerListItemPacket.Item(customSlotUuid);
                    item1.setName(slotUsername[index] = getCustomSlotUsername(index));
                    Property119Handler.setProperties(item1, toPropertiesArray(icon.getTextureProperty()));
                    item1.setDisplayName(tabOverlay.text[index]);
                    item1.setLatency(tabOverlay.ping[index]);
                    item1.setGameMode(0);
                    itemQueueAddPlayer.add(item1);
                }
            }

            // update text
            dirtySlots.copyAndClear(tabOverlay.dirtyFlagsText);
            for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                if (slotState[index] != SlotState.UNUSED) {
                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                    item.setDisplayName(tabOverlay.text[index]);
                    itemQueueUpdateDisplayName.add(item);
                }
            }

            // update ping
            dirtySlots.copyAndClear(tabOverlay.dirtyFlagsPing);
            for (int index = dirtySlots.nextSetBit(0); index >= 0; index = dirtySlots.nextSetBit(index + 1)) {
                if (slotState[index] != SlotState.UNUSED) {
                    LegacyPlayerListItemPacket.Item item = new LegacyPlayerListItemPacket.Item(slotUuid[index]);
                    item.setLatency(tabOverlay.ping[index]);
                    itemQueueUpdatePing.add(item);
                }
            }

            dirtySlots.clear();

            // send packets
            sendQueuedItems();
        }

        private void sendQueuedItems() {
            if (!itemQueueRemovePlayer.isEmpty()) {
                LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(REMOVE_PLAYER, itemQueueRemovePlayer);
                sendPacket(packet);
                itemQueueRemovePlayer.clear();
            }
            if (!itemQueueAddPlayer.isEmpty()) {
                LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(ADD_PLAYER, itemQueueAddPlayer);
                sendPacket(packet);
                if (is18) {
                    packet = new LegacyPlayerListItemPacket(UPDATE_DISPLAY_NAME, itemQueueAddPlayer);
                    sendPacket(packet);
                }
                itemQueueAddPlayer.clear();
            }
            if (!itemQueueUpdateDisplayName.isEmpty()) {
                LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(UPDATE_DISPLAY_NAME, itemQueueUpdateDisplayName);
                sendPacket(packet);
                itemQueueUpdateDisplayName.clear();
            }
            if (!itemQueueUpdatePing.isEmpty()) {
                LegacyPlayerListItemPacket packet = new LegacyPlayerListItemPacket(UPDATE_LATENCY, itemQueueUpdatePing);
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

    protected abstract boolean isExperimentalTabCompleteSmileys();

    protected abstract boolean isExperimentalTabCompleteFixForTabSize80();

    private abstract class CustomContentTabOverlay extends AbstractContentTabOverlay implements TabOverlayHandle.BatchModifiable {
        final UUID[] uuid;
        final Icon[] icon;
        final Component[] text;
        final int[] ping;

        final AtomicInteger batchUpdateRecursionLevel;
        volatile boolean dirtyFlagSize;
        final ConcurrentBitSet dirtyFlagsUuid;
        final ConcurrentBitSet dirtyFlagsIcon;
        final ConcurrentBitSet dirtyFlagsText;
        final ConcurrentBitSet dirtyFlagsPing;

        private CustomContentTabOverlay() {
            this.uuid = new UUID[80];
            this.icon = new Icon[80];
            Arrays.fill(this.icon, Icon.DEFAULT_STEVE);
            this.text = new Component[80];
            Arrays.fill(this.text, Component.empty());
            this.ping = new int[80];
            this.batchUpdateRecursionLevel = new AtomicInteger(0);
            this.dirtyFlagSize = true;
            this.dirtyFlagsUuid = new ConcurrentBitSet(80);
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

        void setUuidInternal(int index, @Nullable UUID uuid) {
            if (!Objects.equals(uuid, this.uuid[index])) {
                this.uuid[index] = uuid;
                dirtyFlagsUuid.set(index);
                scheduleUpdateIfNotInBatch();
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
            Component component = GsonComponentSerializer.gson().deserialize(ChatFormat.formattedTextToJson(text));
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
            if (size.getSize() < serverPlayerList.size() && size.getSize() != 80) {
                for (RectangularTabOverlay.Dimension dimension : tabOverlay.getSupportedSizes()) {
                    if (dimension.getColumns() < tabOverlay.getSize().getColumns())
                        continue;
                    if (dimension.getRows() < tabOverlay.getSize().getRows())
                        continue;
                    if (size.getSize() < serverPlayerList.size() && size.getSize() != 80) {
                        size = dimension;
                    } else if (size.getSize() > dimension.getSize() && dimension.getSize() > serverPlayerList.size()) {
                        size = dimension;
                    }
                }
                canShrink = true;
            } else {
                canShrink = false;
            }
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
                        uuid[index] = null;
                        icon[index] = Icon.DEFAULT_STEVE;
                        text[index] = Component.empty();
                        ping[index] = 0;
                    }
                }
                this.size = size;
                this.dirtyFlagSize = true;
                scheduleUpdateIfNotInBatch();
                for (int index = oldUsedSlots.nextSetBit(0); index >= 0; index = oldUsedSlots.nextSetBit(index + 1)) {
                    if (!newUsedSlots.get(index)) {
                        uuid[index] = null;
                        icon[index] = Icon.DEFAULT_STEVE;
                        text[index] = Component.empty();
                        ping[index] = 0;
                    }
                }
            }
        }

        @Override
        public void setSlot(int column, int row, @Nullable UUID uuid, @Nonnull Icon icon, @Nonnull String text, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, size.getColumns(), "column");
                Preconditions.checkElementIndex(row, size.getRows(), "row");
                beginBatchModification();
                try {
                    int index = index(column, row);
                    setUuidInternal(index, uuid);
                    setIconInternal(index, icon);
                    setTextInternal(index, text);
                    setPingInternal(index, ping);
                } finally {
                    completeBatchModification();
                }
            }
        }

        @Override
        public void setSlot(int column, int row, @Nonnull Icon icon, @Nonnull String text, int ping) {
            if (isValid()) {
                Preconditions.checkElementIndex(column, size.getColumns(), "column");
                Preconditions.checkElementIndex(row, size.getRows(), "row");
                beginBatchModification();
                try {
                    int index = index(column, row);
                    setUuidInternal(index, null);
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
            if (isValid()) {
                Preconditions.checkElementIndex(column, size.getColumns(), "column");
                Preconditions.checkElementIndex(row, size.getRows(), "row");
                setUuidInternal(index(column, row), uuid);
            }
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

        @Override
        void updateSize() {
            int newSize = getTabOverlay().size;
            if (newSize != 80 && newSize < serverPlayerList.size()) {
                newSize = Integer.min(serverPlayerList.size(), 80);
                canShrink = true;
            } else {
                canShrink = false;
            }
            if (newSize > highestUsedSlotIndex + 1) {
                dirtySlots.set(highestUsedSlotIndex + 1, newSize);
            } else if (newSize <= highestUsedSlotIndex) {
                dirtySlots.set(newSize, highestUsedSlotIndex + 1);
            }
            usedSlots = SIZE_TO_USED_SLOTS[newSize];
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
                    setUuidInternal(index, uuid);
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
                    setUuidInternal(index, null);
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
            if (isValid()) {
                Preconditions.checkElementIndex(index, size, "index");
                setUuidInternal(index, uuid);
            }
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
        PacketListenerResult onPlayerListHeaderFooterPacket(HeaderAndFooterPacket packet) {
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
            sendPacket(HeaderAndFooterPacket.create(Component.empty(), Component.empty(), getProtocol()));
        }

        @Override
        void update() {
            CustomHeaderAndFooterImpl tabOverlay = getTabOverlay();
            if (tabOverlay.headerOrFooterDirty) {
                tabOverlay.headerOrFooterDirty = false;
                sendPacket(HeaderAndFooterPacket.create(tabOverlay.header, tabOverlay.footer, getProtocol()));
            }
        }
    }

    private final class CustomHeaderAndFooterImpl extends AbstractHeaderFooterTabOverlay implements HeaderAndFooterHandle {
        private Component header = Component.empty();
        private Component footer = Component.empty();

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
            this.header = GsonComponentSerializer.gson().deserialize(ChatFormat.formattedTextToJson(header));
            this.footer = GsonComponentSerializer.gson().deserialize(ChatFormat.formattedTextToJson(footer));
            headerOrFooterDirty = true;
            scheduleUpdateIfNotInBatch();
        }

        @Override
        public void setHeader(@Nullable String header) {
            this.header = GsonComponentSerializer.gson().deserialize(ChatFormat.formattedTextToJson(header));
            headerOrFooterDirty = true;
            scheduleUpdateIfNotInBatch();
        }

        @Override
        public void setFooter(@Nullable String footer) {
            this.footer = GsonComponentSerializer.gson().deserialize(ChatFormat.formattedTextToJson(footer));
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
            // todo maybe add warning on unsigned properties?
            return new String[][]{{textureProperty.getName(), textureProperty.getValue()}};
        }
    }

    private static Team createPacketTeamCreate(String name, ComponentHolder displayName, ComponentHolder prefix, ComponentHolder suffix, Team.NameTagVisibility nameTagVisibility, Team.CollisionRule collisionRule, int color, byte friendlyFire, String[] players) {
        Team team = new Team();
        team.setName(name);
        team.setMode(Team.Mode.CREATE);
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

    private static Team createPacketTeamRemove(String name) {
        Team team = new Team();
        team.setName(name);
        team.setMode(Team.Mode.REMOVE);
        return team;
    }

    private static Team createPacketTeamUpdate(String name, ComponentHolder displayName, ComponentHolder prefix, ComponentHolder suffix, Team.NameTagVisibility nameTagVisibility, Team.CollisionRule collisionRule, int color, byte friendlyFire) {
        Team team = new Team();
        team.setName(name);
        team.setMode(Team.Mode.UPDATE_INFO);
        team.setDisplayName(displayName);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.setNameTagVisibility(nameTagVisibility);
        team.setCollisionRule(collisionRule);
        team.setColor(color);
        team.setFriendlyFire(friendlyFire);
        return team;
    }

    private static Team createPacketTeamAddPlayers(String name, String[] players) {
        Team team = new Team();
        team.setName(name);
        team.setMode(Team.Mode.ADD_PLAYER);
        team.setPlayers(players);
        return team;
    }

    private static Team createPacketTeamRemovePlayers(String name, String[] players) {
        Team team = new Team();
        team.setName(name);
        team.setMode(Team.Mode.REMOVE_PLAYER);
        team.setPlayers(players);
        return team;
    }

    private enum SlotState {
        UNUSED, CUSTOM, PLAYER
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class PlayerListEntry {
        private UUID uuid;
        private String[][] properties;
        private String username;
        private Component displayName;
        private int ping;
        private int gamemode;

        private PlayerListEntry(LegacyPlayerListItemPacket.Item item) {
            this(item.getUuid(), null, item.getName(), item.getDisplayName(), item.getLatency(), item.getGameMode()); // TODO: Check Display Name
            properties = Property119Handler.getProperties(item);
        }
    }

    @Data
    static class TeamEntry {
        private ComponentHolder displayName;
        private ComponentHolder prefix;
        private ComponentHolder suffix;
        private byte friendlyFire;
        private Team.NameTagVisibility nameTagVisibility;
        private Team.CollisionRule collisionRule;
        private int color;
        private Set<String> players = new ObjectOpenHashSet<>();

        void addPlayer(String name) {
            players.add(name);
        }

        void removePlayer(String name) {
            players.remove(name);
        }
    }
}
