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

package de.codecrafter47.bungeetablistplus.demo;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.api.bungee.Variable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static de.codecrafter47.bungeetablistplus.demo.ColorUtil.getAWTColor;
import static de.codecrafter47.bungeetablistplus.demo.ColorUtil.getSimilarChatColor;
import static java.lang.Math.*;

public class DemoPlugin extends Plugin {

    private CustomTablist customTablist;
    private Icon icon = Icon.DEFAULT;

    @Override
    public void onLoad() {

        // variables should be registered in onLoad to avoid warnings when BTLP loads your config files
        BungeeTabListPlusAPI.registerVariable(this, new Variable("uppercase_name") {
            @Override
            public String getReplacement(ProxiedPlayer player) {
                return player.getName().toUpperCase();
            }
        });
    }

    @Override
    public void onEnable() {
        // create a custom tab list
        customTablist = BungeeTabListPlusAPI.createCustomTablist();
        // with 19 rows and 1 column
        customTablist.setSize(19);

        // if the player types /tabdemo he will see the custom tab list
        getProxy().getPluginManager().registerCommand(this, new Command("tabdemo") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (sender instanceof ProxiedPlayer) {
                    BungeeTabListPlusAPI.setCustomTabList(((ProxiedPlayer) sender), customTablist);
                }
            }
        });

        // every second call updateCustomTablist to update the content of our custom tab list
        getProxy().getScheduler().schedule(this, this::updateCustomTablist, 1, 1, TimeUnit.SECONDS);

        // Create our icon. Use the default icon until the custom one is created.
        try {
            BufferedImage image = ImageIO.read(getResourceAsStream("icon.png"));
            BungeeTabListPlusAPI.createIcon(image, icon -> this.icon = icon);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to load icon.", ex);
        }
    }

    /**
     * This method renders an analogue clock to the tab list.
     */
    private void updateCustomTablist() {
        // create an image
        BufferedImage image = new BufferedImage(19, 19, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        // background
        g.setColor(getAWTColor(ChatColor.DARK_GRAY));
        g.fillRect(0, 0, 19, 19);
        // circle
        g.setColor(getAWTColor(ChatColor.GRAY));
        for (int x = 0; x < 19; x++)
            for (int y = 0; y < 19; y++)
                if ((8.5 - x) * (8.5 - x) + (8.5 - y) * (8.5 - y) < 81)
                    g.drawRect(x, y, 1, 1);
        // arrows
        int hour = Calendar.getInstance().get(Calendar.HOUR);
        g.setColor(getAWTColor(ChatColor.DARK_RED));
        g.drawLine(9, 9, (int) round(9 + 8 * sin(hour / 6.0 * PI)), (int) round(9 - 8 * cos(hour / 6.0 * PI)));
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        g.setColor(getAWTColor(ChatColor.RED));
        g.drawLine(9, 9, (int) round(9 + 8 * sin(minute / 30.0 * PI)), (int) round(9 - 8 * cos(minute / 30.0 * PI)));
        int second = Calendar.getInstance().get(Calendar.SECOND);
        g.setColor(getAWTColor(ChatColor.GOLD));
        g.drawLine(9, 9, (int) round(9 + 9 * sin(second / 30.0 * PI)), (int) round(9 - 9 * cos(second / 30.0 * PI)));
        // convert the image to chat lines
        for (int line = 0; line < 19; line++) {
            String text = "";
            for (int x = 0; x < 19; x++) {
                ChatColor chatColor = getSimilarChatColor(new Color(image.getRGB(x, line)));
                text += chatColor == null ? ' ' : chatColor.toString() + '█';
            }
            customTablist.setSlot(line, 0, icon, text, 0);
        }
    }
}
