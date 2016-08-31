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

package codecrafter47.bungeetablistplus.context;

import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import codecrafter47.bungeetablistplus.config.CustomPlaceholder;
import codecrafter47.bungeetablistplus.player.Player;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class Context {

    // Parent
    private Context parent;

    // Attributes
    private CustomTablist tablist;
    private Player player;
    private Player viewer;
    private int otherPlayersCount = -1;
    private int serverPlayerCount = -1;
    private int columns = -1;
    private String server;
    private PlayerSets playerSets = null;
    private Map<String, CustomPlaceholder> customPlaceholders = null;

    // Constructor
    public Context() {
        this(null);
    }

    private Context(Context parent) {
        this.parent = parent;
    }

    public List<Player> getPlayers(String playerSet) {
        return playerSets != null ? playerSets.get(playerSet) : parent.getPlayers(playerSet);
    }

    public Context derived() {
        return new Context(this);
    }

    public Context setPlayer(Player player) {
        this.player = player;
        return this;
    }

    public CustomTablist getTablist() {
        return tablist != null ? tablist : parent.getTablist();
    }

    public void setTablist(CustomTablist tablist) {
        this.tablist = tablist;
    }

    @Nullable
    public Player getPlayer() {
        return player != null ? player : parent != null ? parent.getPlayer() : null;
    }

    public int getOtherPlayersCount() {
        return otherPlayersCount != -1 ? otherPlayersCount : parent != null ? parent.getOtherPlayersCount() : -1;
    }

    public Context setOtherPlayersCount(int otherPlayersCount) {
        this.otherPlayersCount = otherPlayersCount;
        return this;
    }

    public int getColumns() {
        return columns != -1 ? columns : parent.getColumns();
    }

    public Context setColumns(int columns) {
        Preconditions.checkArgument(columns > 0);
        this.columns = columns;
        return this;
    }

    public Context setServer(String server) {
        this.server = server;
        return this;
    }

    @Nullable
    public String getServer() {
        return server != null ? server : parent != null ? parent.getServer() : null;
    }

    public Player getViewer() {
        return viewer != null ? viewer : parent.getViewer();
    }

    public Context setViewer(Player viewer) {
        this.viewer = viewer;
        return this;
    }

    public void setPlayerSets(PlayerSets playerSets) {
        this.playerSets = playerSets;
    }

    public PlayerSets getPlayerSets() {
        return playerSets != null ? playerSets : parent.getPlayerSets();
    }

    public int getServerPlayerCount() {
        return serverPlayerCount != -1 ? serverPlayerCount : parent != null ? parent.getServerPlayerCount() : -1;
    }

    public Context setServerPlayerCount(int serverPlayerCount) {
        this.serverPlayerCount = serverPlayerCount;
        return this;
    }

    public Map<String, CustomPlaceholder> getCustomPlaceholders() {
        return customPlaceholders != null ? customPlaceholders : parent != null ? parent.getCustomPlaceholders() : null;
    }

    public Context setCustomPlaceholders(Map<String, CustomPlaceholder> customPlaceholders) {
        this.customPlaceholders = customPlaceholders;
        return this;
    }
}
