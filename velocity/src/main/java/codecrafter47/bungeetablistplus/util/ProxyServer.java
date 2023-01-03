package codecrafter47.bungeetablistplus.util;

public class ProxyServer {

    private static com.velocitypowered.api.proxy.ProxyServer _proxyServer;

    public static com.velocitypowered.api.proxy.ProxyServer getInstance() {
        return _proxyServer;
    }

    public static void setProxyServer(com.velocitypowered.api.proxy.ProxyServer _proxyServer) {
        ProxyServer._proxyServer = _proxyServer;
    }
}
