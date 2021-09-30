package codecrafter47.bungeetablistplus.api.velocity.tablist;

import codecrafter47.bungeetablistplus.api.velocity.Icon;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.Optional;
import java.util.UUID;

public interface FakePlayer {
    
    String getName();
    
    UUID getUniqueID();
    
    Optional<ServerInfo> getServer();
    
    int getPing();
    
    Icon getIcon();
    
    void changeServer(ServerInfo newServer);
    
    void setIcon(de.codecrafter47.taboverlay.Icon icon);
    
    void setPing(int ping);
    
    boolean isRandomServerSwitchEnabled();
    
    void setRandomServerSwitchEnabled(boolean value);
}
