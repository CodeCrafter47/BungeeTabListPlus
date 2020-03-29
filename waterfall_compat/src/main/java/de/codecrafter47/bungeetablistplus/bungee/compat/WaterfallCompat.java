package de.codecrafter47.bungeetablistplus.bungee.compat;

import net.md_5.bungee.api.ProxyServer;

public class WaterfallCompat {

    public static boolean isDisableEntityMetadataRewrite() {
        ProxyServer proxy = ProxyServer.getInstance();
        if (proxy != null && "Waterfall".equals(proxy.getName())) {
            return proxy.getConfig().isDisableEntityMetadataRewrite();
        } else {
            return false;
        }
    }
}
