package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.tab.TabListHandler;

public class PlayerManager {

    private final BungeeTabListPlus plugin;

    public PlayerManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isServer(String s) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if (s.equalsIgnoreCase(server.getName())) {
                return true;
            }
            int i = s.indexOf('#');
            if (i > 1) {
                if (s.substring(0, i).equalsIgnoreCase(server.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ProxiedPlayer> getPlayers(Collection<String> filter,
            ProxiedPlayer who) {
        List<ProxiedPlayer> list = new ArrayList<>();
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            boolean areGroupRules = false;
            boolean areServerRules = false;
            boolean fitGroupRules = false;
            boolean fitServerRules = false;
            String group = plugin.getPermissionManager().getMainGroup(p);
            for (String rule : filter) {
                if (rule.isEmpty()) {
                    // ignore
                } else if (rule.equalsIgnoreCase("currentserver")) {
                    areServerRules = true;
                    if (p.getServer() != null && who.getServer() != null) {
                        if (p.getServer().getInfo().getName().equalsIgnoreCase(
                                who.getServer().getInfo().getName())) {
                            fitServerRules = true;
                        }
                    }
                } else if (isServer(rule)) {
                    areServerRules = true;
                    Server server = p.getServer();
                    if (server != null) {
                        if (server.getInfo().getName().equalsIgnoreCase(rule)) {
                            fitServerRules = true;
                        }
                        String[] s = rule.split("#");
                        if (s.length == 2) {
                            if (server.getInfo().getName().
                                    equalsIgnoreCase(s[0])) {
                                String world = plugin.getBridge().
                                        getPlayerInformation(p, "world");
                                if (world != null) {
                                    if (world.equalsIgnoreCase(s[1])) {
                                        fitServerRules = true;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    areGroupRules = true;
                    if (group != null) {
                        if (group.equalsIgnoreCase(rule)) {
                            fitGroupRules = true;
                        }
                    }
                }
            }
            if (((!areServerRules) || fitServerRules) && ((!areGroupRules) || fitGroupRules) && !BungeeTabListPlus.
                    isHidden(p)) {
                list.add(p);
            }
        }
        /*
         for (int i = 0; i < 5 + Math.random() * 10; i++) {
         final String name = "player" + (int) (Math.random() * 100);
         list.add(new ProxiedPlayer() {
         final List<String> groups;
         {

         if(Math.random() < 0.2)groups = Arrays.asList(new String[]{"default", "admin"});
         else if(Math.random() < 0.4)groups = Arrays.asList(new String[]{"default", "vip"});
         else groups = Arrays.asList(new String[]{"default"});
         }

         @Override
         public String getDisplayName() {
         return name;
         }

         @Override
         public void setDisplayName(String string) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void connect(ServerInfo si) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void connect(ServerInfo si, Callback<Boolean> clbck) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public Server getServer() {
         return null;
         }

         @Override
         public int getPing() {
         return (int) (Math.random() * 1000);
         }

         @Override
         public void sendData(String string, byte[] bytes) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public PendingConnection getPendingConnection() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void chat(String string) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void setTabList(TabListHandler tlh) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public TabListHandler getTabList() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public ServerInfo getReconnectServer() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void setReconnectServer(ServerInfo si) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public String getUUID() {
         return name;
         }

         @Override
         public UUID getUniqueId() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public InetSocketAddress getAddress() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void disconnect(String string) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void disconnect(BaseComponent... bcs) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void disconnect(BaseComponent bc) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public Connection.Unsafe unsafe() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public String getName() {
         return name;
         }

         @Override
         public void sendMessage(String string) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void sendMessages(String... strings) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void sendMessage(BaseComponent... bcs) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void sendMessage(BaseComponent bc) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public Collection<String> getGroups() {
         return groups;
         }

         @Override
         public void addGroups(String... strings) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void removeGroups(String... strings) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public boolean hasPermission(String string) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public void setPermission(String string, boolean bln) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         @Override
         public Collection<String> getPermissions() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
         }

         });
         }*/
        return list;
    }

    public int getServerPlayerCount(String server) {
        int num = 0;
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            Server s = p.getServer();
            if (s != null) {
                if (s.getInfo().getName().equalsIgnoreCase(server) && !BungeeTabListPlus.
                        isHidden(p)) {
                    num++;
                }
            }
        }
        return num;
    }

    public int getGlobalPlayerCount() {
        int num = 0;
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            if (!BungeeTabListPlus.isHidden(p)) {
                num++;
            }
        }
        return num;
    }
}
