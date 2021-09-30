package codecrafter47.bungeetablistplus.api.velocity;

import codecrafter47.bungeetablistplus.api.velocity.tablist.FakePlayer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.Collection;

public interface FakePlayerManager {
    
    Collection<FakePlayer> getOnlineFakePlayers();
    
    boolean isRandomJoinLeaveEnabled();
    
    void setRandomJoinLeaveEnabled(boolean value);
    
    FakePlayer createFakePlayer(String name, ServerInfo server);
    
    void removeFakePlayer(FakePlayer fakePlayer);
}
