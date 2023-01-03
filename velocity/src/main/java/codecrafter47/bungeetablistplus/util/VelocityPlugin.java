package codecrafter47.bungeetablistplus.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class VelocityPlugin {

    @Getter
    private final ProxyServer proxy;
    @Getter
    private final Logger logger;
    @Getter
    private final Path dataDirectory;
    @Getter
    private final String version;

    public VelocityPlugin(ProxyServer proxy, Logger logger, Path dataDirectory, String version){
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.version = version;
    }

    public static boolean isProxyRunning(ProxyServer proxyServer){
        try {
            Class<?> velocityServer = Class.forName("com.velocitypowered.proxy.VelocityServer");
            Field shutdownInProgress = velocityServer.getDeclaredField("shutdownInProgress");
            shutdownInProgress.setAccessible(true);

            return !((AtomicBoolean) shutdownInProgress.get(proxyServer)).get();
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ignored) { }
        // Return not running if it can't grab the shutdownInProgress value;
        return false;
    }
}
