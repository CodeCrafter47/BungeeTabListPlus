/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Preconditions;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLegacyTabList implements PacketHandler {
    protected static final String[] slotID;
    protected static final Set<String> slotIDSet = new HashSet<>();

    static {
        slotID = new String[256];

        for (int i = 0; i < 256; i++) {
            String hex = String.format("%02x", i);
            slotID[i] = String.format("§k§B§T§L§P§%c§%c§r", hex.charAt(0), hex.charAt(1));
            slotIDSet.add(slotID[i]);
        }
    }

    @Getter
    protected final int maxSize;
    protected int[] clientPing;
    protected String[] clientText;
    protected Map<String, Integer> serverTabList = new ConcurrentHashMap<>();
    protected int clientSize = 0;
    protected int usedSlots = 0;
    protected boolean passThrough = true;

    public AbstractLegacyTabList(int maxSize) {
        this.maxSize = maxSize;
        clientText = new String[maxSize];
        clientPing = new int[maxSize];
    }

    private static String[] splitText(String s) {
        String[] ret = new String[3];
        int left = s.length();
        if (left <= 16) {
            ret[0] = s;
            ret[1] = "";
            ret[2] = "";
        } else {
            int end = s.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16;
            ret[0] = s.substring(0, end);
            int start = ColorParser.endofColor(s, end);
            String colors = ColorParser.extractColorCodes(s.substring(0, start));
            end = start + 16 - colors.length();
            if (end >= s.length()) {
                end = s.length();
            }
            ret[1] = colors + s.substring(start, end);
            start = end;
            end += 16;
            if (end >= s.length()) {
                end = s.length();
            }
            ret[2] = s.substring(start, end);
        }
        return ret;
    }

    public void setSize(int size) {
        resize(size);
        clientSize = size;
    }

    public void setPassTrough(boolean passThrough) {
        if (passThrough != this.passThrough) {
            this.passThrough = passThrough;
            if (passThrough) {
                resize(0);
                for (Map.Entry<String, Integer> entry : serverTabList.entrySet()) {
                    PlayerListItem pli = new PlayerListItem();
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setDisplayName(entry.getKey());
                    item.setPing(entry.getValue());
                    pli.setItems(new PlayerListItem.Item[]{item});
                    pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                    sendPacket(pli);
                }
            } else {
                for (String player : serverTabList.keySet()) {
                    PlayerListItem pli = new PlayerListItem();
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setDisplayName(player);
                    item.setPing(9999);
                    pli.setItems(new PlayerListItem.Item[]{item});
                    pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
                    sendPacket(pli);
                }
                resize(clientSize);
                for (int i = 0; i < clientSize; i++) {
                    updateSlot(i, clientText[i], clientPing[i], true);
                }
            }
        }
    }

    public void setSlot(int i, String text, int ping) {
        if (i >= 0 && i < usedSlots) {
            updateSlot(i, text, ping, false);
        }
    }

    private void resize(int size) {
        Preconditions.checkArgument(size >= 0 && size <= this.maxSize, "maxSize");

        if (!passThrough) {
            if (size > usedSlots) {
                for (int i = usedSlots; i < size; i++) {
                    createSlot(i);
                }
            } else if (size < usedSlots) {
                for (int i = size; i < usedSlots; i++) {
                    removeSlot(i);
                }
            }
            usedSlots = size;
        }
    }

    private void createSlot(int row) {
        clientPing[row] = 0;
        clientText[row] = "";
        PlayerListItem pli = new PlayerListItem();
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(slotID[row]);
        item.setPing(clientPing[row]);
        pli.setItems(new PlayerListItem.Item[]{item});
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        sendPacket(pli);
        Team t = new Team();
        t.setName(slotID[row]);
        t.setMode((byte) 0);
        t.setPrefix(" ");
        t.setDisplayName(" ");
        t.setSuffix(" ");
        t.setPlayers(new String[]{slotID[row]});
        sendPacket(t);
    }

    private void updateSlot(int row, String text, int ping, boolean force) {
        if (!passThrough) {
            if (force || ping != clientPing[row]) {
                PlayerListItem pli = new PlayerListItem();
                PlayerListItem.Item item = new PlayerListItem.Item();
                item.setDisplayName(slotID[row]);
                item.setPing(ping);
                pli.setItems(new PlayerListItem.Item[]{item});
                pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                sendPacket(pli);
            }
            if (force || !Objects.equals(text, clientText[row])) {
                String[] split = splitText(text);
                Team t = new Team();
                t.setName(slotID[row]);
                t.setMode((byte) 2);
                t.setPrefix(split[0]);
                t.setDisplayName("");
                t.setSuffix(split[1]);
                sendPacket(t);
            }
        }
        clientText[row] = text;
        clientPing[row] = ping;
    }

    private void removeSlot(int i) {
        PlayerListItem pli = new PlayerListItem();
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(slotID[i]);
        item.setPing(9999);
        pli.setItems(new PlayerListItem.Item[]{item});
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        sendPacket(pli);
        Team t = new Team();
        t.setName(slotID[i]);
        t.setMode((byte) 1);
        sendPacket(t);
    }

    protected abstract void sendPacket(DefinedPacket packet);

    @Override
    public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
        if (packet.getAction() == PlayerListItem.Action.ADD_PLAYER) {
            for (PlayerListItem.Item item : packet.getItems()) {
                serverTabList.put(item.getDisplayName(), item.getPing());
            }
        } else {
            for (PlayerListItem.Item item : packet.getItems()) {
                serverTabList.remove(item.getDisplayName());
            }
        }
        return passThrough ? PacketListenerResult.PASS : PacketListenerResult.CANCEL;
    }

    @Override
    public PacketListenerResult onTeamPacket(Team packet) {
        if (slotIDSet.contains(packet.getName())) {
            throw new AssertionError("Team name collision: " + packet);
        }
        return PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
        throw new AssertionError("1.7 players should not receive tab list header/ footer");
    }

    @Override
    public void onServerSwitch() {
        for (String player : serverTabList.keySet()) {
            PlayerListItem pli = new PlayerListItem();
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setDisplayName(player);
            item.setPing(9999);
            pli.setItems(new PlayerListItem.Item[]{item});
            pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
            sendPacket(pli);
        }
        serverTabList.clear();
    }
}
