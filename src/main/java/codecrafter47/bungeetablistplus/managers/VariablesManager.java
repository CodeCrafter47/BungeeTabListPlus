/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.api.PlayerVariable;
import codecrafter47.bungeetablistplus.api.ServerVariable;
import codecrafter47.bungeetablistplus.api.Variable;
import codecrafter47.bungeetablistplus.player.IPlayer;
import codecrafter47.bungeetablistplus.variables.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VariablesManager {

    private final Map<String, Variable> variables = new HashMap<>();
    private final Map<String, PlayerVariable> playerVariables = new HashMap<>();
    private final Map<String, ServerVariable> serverVariables = new HashMap<>();

    public VariablesManager() {
        super();
        addVariable("server_player_count",
                new CurrentServerPlayerCountVariable());
        addVariable("player_count", new PlayerCountVariable());
        addVariable("gcount", new PlayerCountVariable());
        addVariable("players", new ServerPlayerCountVariable());
        addVariable("name", new PlayerNameVariable());
        addVariable("player", new PlayerNameVariable());
        addVariable("rawname", new PlayerRawNameVariable());
        addVariable("server", new ServerNameVariable());
        addVariable("permprefix", new PermPrefix());
        addVariable("prefix", new PermPrefix());
        addVariable("prefixColor", new PrefixColor());
        addVariable("permsuffix", new PermSuffix());
        addVariable("suffix", new PermSuffix());
        addVariable("displayprefix", new DisplayPrefix());
        addVariable("ping", new PingVariable());
        addVariable("time", new TimeVariable("HH:mm:ss"));
        addVariable("date", new TimeVariable("dd.MM.yyyy"));
        addVariable("second", new TimeVariable("ss"));
        addVariable("seconds", new TimeVariable("ss"));
        addVariable("sec", new TimeVariable("ss"));
        addVariable("minute", new TimeVariable("mm"));
        addVariable("minutes", new TimeVariable("mm"));
        addVariable("min", new TimeVariable("mm"));
        addVariable("hour", new TimeVariable("HH"));
        addVariable("hours", new TimeVariable("HH"));
        addVariable("day", new TimeVariable("dd"));
        addVariable("days", new TimeVariable("dd"));
        addVariable("month", new TimeVariable("MM"));
        addVariable("months", new TimeVariable("MM"));
        addVariable("year", new TimeVariable("yyyy"));
        addVariable("years", new TimeVariable("yyyy"));
        addVariable("group", new GroupVariable());
        addVariable("uuid", new UUIDVariable());
        addVariable("UUID", new UUIDVariable());
        addVariable("world", new WorldVariable());
        addVariable("balance", new BalanceVariable());
        addVariable("color", new ColorVariable());
        addVariable("factionName", new BukkitBridgeVariable("factionName"));
        addVariable("onlineFactionMembers", new BukkitBridgeVariable(
                "onlineFactionMembers"));
        addVariable("factionsWhere", new BukkitBridgeVariable("factionsWhere"));
        addVariable("health", new BukkitBridgeVariable("health"));
        addVariable("level", new BukkitBridgeVariable("level"));
        addVariable("currency", new BukkitBridgeVariable("currency"));
        addVariable("currencyPl", new BukkitBridgeVariable("currencyPl"));
        addVariable("tabName", new TabNameVariable());
        addVariable("tps", new BukkitBridgeVariable("tps"));
        addVariable("onlineState", new ServerState());
        addVariable("rplayers", new RedisPlayers());
        addVariable("internalServerName", new InternalServerNameVariable());
        addVariable("server_rplayer_count", new PerServerRedisPlayers());
        addVariable("serverPrefix", new ServerPrefixVariable());
    }

    public void addVariable(String name, Variable var) {
        variables.put(name, var);
    }

    public void addVariable(String name, PlayerVariable var) {
        playerVariables.put(name, var);
    }

    public void addVariable(String name, ServerVariable var) {
        serverVariables.put(name, var);
    }

    public String replaceVariables(ProxiedPlayer viewer, String s) {
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("\\{[^}]+\\}");
        Matcher matcher = pattern.matcher(s);

        while (matcher.find()) {
            String var = s.substring(matcher.start(), matcher.end());
            var = var.replaceAll("[\\{\\}]", "");
            String arg = null;
            String replacement = "{" + var + "}";
            if (var.contains(":")) {
                arg = var.substring(var.indexOf(":") + 1);
                var = var.substring(0, var.indexOf(":"));
            }

            Variable variable = this.variables.get(var);
            if (variable != null) {
                String str = variable.getReplacement(viewer, arg);
                if (str != null) {
                    replacement = str;
                }
            }

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String replacePlayerVariables(ProxiedPlayer viewer, String s, IPlayer player) {
        if (player.getServer().isPresent()) {
            s = replaceServerVariables(viewer, s, player.getServer().get());
        }

        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("\\{[^}]+\\}");
        Matcher matcher = pattern.matcher(s);

        while (matcher.find()) {
            String var = s.substring(matcher.start(), matcher.end());
            var = var.replaceAll("[\\{\\}]", "");
            String arg = null;
            String replacement = "{" + var + "}";
            if (var.contains(":")) {
                arg = var.substring(var.indexOf(":") + 1);
                var = var.substring(0, var.indexOf(":"));
            }

            PlayerVariable variable = this.playerVariables.get(var);
            if (variable != null) {
                String str = variable.getReplacement(viewer, player, arg);
                if (str != null) {
                    replacement = str;
                }
            }
            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String replaceServerVariables(ProxiedPlayer viewer, String s, ServerInfo server) {
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("\\{[^}]+\\}");
        Matcher matcher = pattern.matcher(s);

        while (matcher.find()) {
            String var = s.substring(matcher.start(), matcher.end());
            var = var.replaceAll("[\\{\\}]", "");
            String arg = null;
            String replacement = "{" + var + "}";
            if (var.contains(":")) {
                arg = var.substring(var.indexOf(":") + 1);
                var = var.substring(0, var.indexOf(":"));
            }

            ServerVariable variable = this.serverVariables.get(var);
            if (variable != null) {
                String str = variable.getReplacement(viewer, server, arg);
                if (str != null) {
                    replacement = str;
                }
            }

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
