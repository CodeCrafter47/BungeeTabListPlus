package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class FakePlayerManager implements IPlayerProvider {
    List<IPlayer> online = new ArrayList<>();
    List<String> offline;
    BungeeTabListPlus plugin;

    public FakePlayerManager(final BungeeTabListPlus plugin) {
        this.plugin = plugin;
        if (plugin.getConfigManager().getMainConfig().fakePlayers.size() > 0) {
            offline = new ArrayList<>(plugin.getConfigManager().getMainConfig().fakePlayers);
            plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    if (Math.random() < 0.3 && online.size() > 0) {
                        // do a server switch
                        FakePlayer player = (FakePlayer) online.get((int) (Math.random() * online.size()));
                        player.server = new ArrayList<>(plugin.getProxy().getServers().values()).get((int) (Math.random() * plugin.getProxy().getServers().values().size()));
                    } else if (Math.random() < 0.9 && offline.size() > 0) {
                        // add player
                        String name = offline.get((int) (Math.random() * offline.size()));
                        FakePlayer player = new FakePlayer();
                        player.name = name;
                        player.server = new ArrayList<>(plugin.getProxy().getServers().values()).get((int) (Math.random() * plugin.getProxy().getServers().values().size()));
                        offline.remove(name);
                        online.add(player);
                    } else if (online.size() > 0) {
                        // remove player
                        offline.add(online.remove((int) (online.size() * Math.random())).getName());
                    }
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    }

    public void reload() {
        offline = new ArrayList<>(plugin.getConfigManager().getMainConfig().fakePlayers);
        online = new ArrayList<>();
    }

    @Override
    public Collection<IPlayer> getPlayers() {
        return Collections.unmodifiableCollection(online);
    }

}
