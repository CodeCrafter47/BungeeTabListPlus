package de.codecrafter47.bungeetablistplus.bungee.compat;

import net.md_5.bungee.api.ProxyServer;

public class WaterfallCompat {

    public static boolean isDisableEntityMetadataRewrite() {
        if ("Waterfall".equals(ProxyServer.getInstance().getName())) {
            return ProxyServer.getInstance().getConfig().isDisableEntityMetadataRewrite();
        } else {
            return false;
        }
    }
}
