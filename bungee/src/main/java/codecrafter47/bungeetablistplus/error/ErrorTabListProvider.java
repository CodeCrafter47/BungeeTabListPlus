/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.error;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.ITabListProvider;
import codecrafter47.bungeetablistplus.layout.LayoutException;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.function.Predicate;

public class ErrorTabListProvider implements ITabListProvider {
    private final String message;
    private final Throwable throwable;
    private final Predicate<ProxiedPlayer> appliesTo;

    public ErrorTabListProvider(String message, Throwable throwable) {
        this(message, throwable, player -> true);
    }

    public ErrorTabListProvider(String message, Throwable throwable, Predicate<ProxiedPlayer> appliesTo) {
        this.message = message;
        this.throwable = throwable;
        this.appliesTo = appliesTo;
    }

    @Override
    public void fillTabList(ProxiedPlayer player, ITabList tabList, TabListContext context) throws LayoutException {
        constructErrorTabList(player, tabList, message, throwable);
    }

    @SneakyThrows
    public static void constructErrorTabList(ProxiedPlayer player, ITabList tabList, String message, Throwable throwable) {
        boolean is18 = BungeeTabListPlus.getInstance().getProtocolVersionProvider().getProtocolVersion(player) >= 47;

        if (is18) {
            tabList.setHeader("&c&lERROR * ERROR * ERROR * ERROR * ERROR * ERROR * ERROR * ERROR");
            tabList.setDefaultPing(-1);
            tabList.setFooter(String.format("&cBungeeTabListPlus %s\nBungeeCord %s",
                    BungeeTabListPlus.getInstance().getPlugin().getDescription().getVersion(),
                    ProxyServer.getInstance().getVersion()));
        }

        // message
        int partLength = 15;

        int pos = 0;
        while (pos * partLength < message.length() && pos < tabList.getSize()) {
            int endIndex = pos * partLength + partLength;
            tabList.setSlot(pos, new Slot(message.substring(pos * partLength, endIndex > message.length() ? message.length() : endIndex), -1, SkinManager.defaultSkin));
            pos += 1;
        }

        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        BufferedReader reader = new BufferedReader(new StringReader(stringWriter.toString()));
        String line;
        while ((line = reader.readLine()) != null) {
            int lineStart = pos = ((pos + tabList.getColumns() - 1) / tabList.getColumns()) * tabList.getColumns();
            while ((pos - lineStart) * partLength < line.length() && pos < tabList.getSize()) {
                int endIndex = (pos - lineStart) * partLength + partLength;
                tabList.setSlot(pos, new Slot(line.substring((pos - lineStart) * partLength, endIndex > line.length() ? line.length() : endIndex), -1, SkinManager.defaultSkin));
                pos += 1;
            }
        }
    }

    @Override
    public boolean appliesTo(ProxiedPlayer player) {
        return appliesTo.test(player);
    }
}
