package codecrafter47.bungeetablistplus.util;

import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

public interface VelocityPlugin {
    ProxyServer getProxy();
    Logger getLogger();
    Path getDataDirectory();
    String getVersion();
}
