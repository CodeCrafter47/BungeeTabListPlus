package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import com.google.common.base.Charsets;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by florian on 12.11.14.
 */
public class FakePlayerManager {
    List<ProxiedPlayer> online = new ArrayList<>();
    List<String> offline;
    BungeeTabListPlus plugin;

    public FakePlayerManager(final BungeeTabListPlus plugin) {
        this.plugin = plugin;
        if(plugin.getConfigManager().getMainConfig().fakePlayers.size() > 0){
            offline = new ArrayList<>(plugin.getConfigManager().getMainConfig().fakePlayers);
            plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    if(Math.random() < 0.3 && online.size() > 0){
                        // do a server switch
                        FakePlayer player = (FakePlayer) online.get((int) (Math.random() * online.size()));
                        ServerInfo server = new ArrayList<>(plugin.getProxy().getServers().values()).get((int) (Math.random()*plugin.getProxy().getServers().values().size()));
                        player.server = server;
                    } else if(Math.random() < 0.9 && offline.size() > 0){
                        // add player
                        String name = offline.get((int) (Math.random()*offline.size()));
                        FakePlayer player = new FakePlayer();
                        player.name = name;
                        ServerInfo server = new ArrayList<>(plugin.getProxy().getServers().values()).get((int) (Math.random()*plugin.getProxy().getServers().values().size()));
                        player.server = server;
                        player.group = "default";
                        offline.remove(name);
                        online.add(player);
                    } else if(online.size() > 0){
                        // remove player
                        offline.add(online.remove((int)(online.size()*Math.random())).getName());
                    }
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    }

    public void reload(){
        offline = new ArrayList<>(plugin.getConfigManager().getMainConfig().fakePlayers);
        online = new ArrayList<>();
    }

    public List<ProxiedPlayer> getFakePlayers(){
        return Collections.unmodifiableList(online);
    }

    public static class FakePlayer implements ProxiedPlayer{
        String name;
        ServerInfo server;
        String group;

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public void setDisplayName(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void connect(ServerInfo serverInfo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void connect(ServerInfo serverInfo, Callback<Boolean> booleanCallback) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Server getServer() {
            return new Server() {
                @Override
                public ServerInfo getInfo() {
                    return server;
                }

                @Override
                public void sendData(String s, byte[] bytes) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public InetSocketAddress getAddress() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void disconnect(String s) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void disconnect(BaseComponent... baseComponents) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void disconnect(BaseComponent baseComponent) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Unsafe unsafe() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int getPing() {
            return 0;
        }

        @Override
        public void sendData(String s, byte[] bytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PendingConnection getPendingConnection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void chat(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServerInfo getReconnectServer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setReconnectServer(ServerInfo serverInfo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getUUID() {
            return UUID.nameUUIDFromBytes(new String("OfflinePlayer:"+name).getBytes(Charsets.UTF_8)).toString();
        }

        @Override
        public UUID getUniqueId() {
            return UUID.nameUUIDFromBytes(new String("OfflinePlayer:"+name).getBytes(Charsets.UTF_8));
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTabHeader(BaseComponent baseComponent, BaseComponent baseComponent2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTabHeader(BaseComponent[] baseComponents, BaseComponent[] baseComponents2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resetTabHeader() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendTitle(Title title) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
           return name;
        }

        @Override
        public void sendMessage(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendMessages(String... strings) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendMessage(BaseComponent... baseComponents) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendMessage(BaseComponent baseComponent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getGroups() {
            return Arrays.asList(group);
        }

        @Override
        public void addGroups(String... strings) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeGroups(String... strings) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPermission(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPermission(String s, boolean b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getPermissions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InetSocketAddress getAddress() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disconnect(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disconnect(BaseComponent... baseComponents) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disconnect(BaseComponent baseComponent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Unsafe unsafe() {
            throw new UnsupportedOperationException();
        }
    }
}
