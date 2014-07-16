package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.variables.BalanceVariable;
import codecrafter47.bungeetablistplus.variables.CurrentServerPlayerCountVariable;
import codecrafter47.bungeetablistplus.variables.DisplayPrefix;
import codecrafter47.bungeetablistplus.variables.GroupVariable;
import codecrafter47.bungeetablistplus.variables.PermPrefix;
import codecrafter47.bungeetablistplus.variables.PermSuffix;
import codecrafter47.bungeetablistplus.variables.PingVariable;
import codecrafter47.bungeetablistplus.variables.PlayerCountVariable;
import codecrafter47.bungeetablistplus.variables.PlayerNameVariable;
import codecrafter47.bungeetablistplus.variables.PlayerRawNameVariable;
import codecrafter47.bungeetablistplus.variables.PlayerVariable;
import codecrafter47.bungeetablistplus.variables.ServerNameVariable;
import codecrafter47.bungeetablistplus.variables.ServerPlayerCountVariable;
import codecrafter47.bungeetablistplus.variables.ServerVariable;
import codecrafter47.bungeetablistplus.variables.TimeVariable;
import codecrafter47.bungeetablistplus.variables.UUIDVariable;
import codecrafter47.bungeetablistplus.variables.Variable;
import codecrafter47.bungeetablistplus.variables.WorldVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class VariablesManager {

    private final List<Variable> variables = new ArrayList<>();
    private final List<PlayerVariable> playerVariables = new ArrayList<>();
    private final List<ServerVariable> serverVariables = new ArrayList<>();

    public VariablesManager() {
        super();
        // Adding default Variables
        addVariable(new CurrentServerPlayerCountVariable(
                "server_player_count"));
        VariablesManager.this.addVariable(new PlayerCountVariable("player_count"));
        VariablesManager.this.addVariable(new PlayerCountVariable("gcount"));
        VariablesManager.this.addVariable(new PlayerCountVariable("players"));
        VariablesManager.this.addVariable(new PlayerNameVariable("name"));
        VariablesManager.this.addVariable(new PlayerNameVariable("player"));
        VariablesManager.this.addVariable(new PlayerRawNameVariable("rawname"));
        addVariable(new ServerNameVariable("server"));
        VariablesManager.this.addVariable(new PermPrefix("permprefix"));
        VariablesManager.this.addVariable(new PermPrefix("prefix"));
        VariablesManager.this.addVariable(new PermSuffix("permsuffix"));
        VariablesManager.this.addVariable(new PermSuffix("suffix"));
        VariablesManager.this.addVariable(new DisplayPrefix("displayprefix"));
        VariablesManager.this.addVariable(new PingVariable("ping"));

        Map<String, ServerInfo> server_map = ProxyServer.getInstance().getServers();
        for (String server_name : server_map.keySet()) {
            VariablesManager.this.addVariable(new ServerPlayerCountVariable("players:"
                    + server_name, server_name));
            VariablesManager.this.addVariable(new ServerPlayerCountVariable("players:"
                    + server_name.toLowerCase(), server_name));
        }

        VariablesManager.this.addVariable(new TimeVariable("time", "HH:mm:ss"));
        VariablesManager.this.addVariable(new TimeVariable("date", "dd.MM.yyyy"));
        VariablesManager.this.addVariable(new TimeVariable("second", "ss"));
        VariablesManager.this.addVariable(new TimeVariable("seconds", "ss"));
        VariablesManager.this.addVariable(new TimeVariable("sec", "ss"));
        VariablesManager.this.addVariable(new TimeVariable("minute", "mm"));
        VariablesManager.this.addVariable(new TimeVariable("minutes", "mm"));
        VariablesManager.this.addVariable(new TimeVariable("min", "mm"));
        VariablesManager.this.addVariable(new TimeVariable("hour", "HH"));
        VariablesManager.this.addVariable(new TimeVariable("hours", "HH"));
        VariablesManager.this.addVariable(new TimeVariable("day", "dd"));
        VariablesManager.this.addVariable(new TimeVariable("days", "dd"));
        VariablesManager.this.addVariable(new TimeVariable("month", "MM"));
        VariablesManager.this.addVariable(new TimeVariable("months", "MM"));
        VariablesManager.this.addVariable(new TimeVariable("year", "yyyy"));
        VariablesManager.this.addVariable(new TimeVariable("years", "yyyy"));
        VariablesManager.this.addVariable(new GroupVariable("group"));
        addVariable(new UUIDVariable("uuid"));
        addVariable(new UUIDVariable("UUID"));
        addVariable(new WorldVariable());
        addVariable(new BalanceVariable());
    }

    public void addVariable(Variable var) {
        variables.add(var);
    }

    public void addVariable(PlayerVariable var) {
        playerVariables.add(var);
    }

    public void addVariable(ServerVariable var) {
        serverVariables.add(var);
    }

    public String replaceVariables(String s) {
        for (Variable v : variables) {
            if(s.contains("{" + v.getName() + "}"))s = s.replace("{" + v.getName() + "}", v.getReplacement());
        }
        return s;

    }

    public String replacePlayerVariables(String s, ProxiedPlayer player) {
        if (player.getServer() != null) {
            s = replaceServerVariables(s, player.getServer().getInfo());
        }
        for (PlayerVariable v : playerVariables) {
            if(s.contains("{" + v.getName() + "}"))s = s.replace("{" + v.getName() + "}", v.getReplacement(player));
        }
        return s;

    }

    public String replaceServerVariables(String s, ServerInfo server) {
        for (ServerVariable v : serverVariables) {
            if(s.contains("{" + v.getName() + "}"))s = s.replace("{" + v.getName() + "}", v.getReplacement(server));
        }
        return s;

    }

    public boolean hasPlayerVariables(String s) {
        for (PlayerVariable v : playerVariables) {
            if (s.contains("{" + v.getName() + "}")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasServerVariables(String s) {
        for (ServerVariable v : serverVariables) {
            if (s.contains("{" + v.getName() + "}")) {
                return true;
            }
        }
        return false;
    }

}
