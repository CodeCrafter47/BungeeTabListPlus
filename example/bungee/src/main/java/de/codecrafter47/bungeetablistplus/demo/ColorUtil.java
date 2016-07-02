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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;

public class ColorUtil {
    private static BiMap<Color, ChatColor> colors = ImmutableBiMap.<Color, ChatColor>builder()
            .put(new Color(0x000000), ChatColor.BLACK)
            .put(new Color(0x0000aa), ChatColor.DARK_BLUE)
            .put(new Color(0x00aa00), ChatColor.DARK_GREEN)
            .put(new Color(0x00aaaa), ChatColor.DARK_AQUA)
            .put(new Color(0xaa0000), ChatColor.DARK_RED)
            .put(new Color(0xaa00aa), ChatColor.DARK_PURPLE)
            .put(new Color(0xffaa00), ChatColor.GOLD)
            .put(new Color(0xaaaaaa), ChatColor.GRAY)
            .put(new Color(0x555555), ChatColor.DARK_GRAY)
            .put(new Color(0x5555ff), ChatColor.BLUE)
            .put(new Color(0x55ff55), ChatColor.GREEN)
            .put(new Color(0x55ffff), ChatColor.AQUA)
            .put(new Color(0xff5555), ChatColor.RED)
            .put(new Color(0xff55ff), ChatColor.LIGHT_PURPLE)
            .put(new Color(0xffff55), ChatColor.YELLOW)
            .put(new Color(0xffffff), ChatColor.WHITE)
            .build();

    public static Color getAWTColor(ChatColor chatColor) {
        return colors.inverse().get(chatColor);
    }

    public static ChatColor getSimilarChatColor(Color color) {
        if (color.getAlpha() < 128) {
            return null;
        }
        Color bestColor = null;
        double bestDist = Double.POSITIVE_INFINITY;
        for (Color c : colors.keySet()) {
            double dist;
            if ((dist = ColourDistance(c, color)) < bestDist) {
                bestColor = c;
                bestDist = dist;
            }
        }
        return colors.get(bestColor);
    }

    // from http://stackoverflow.com/questions/2103368/color-logic-algorithm
    public static double ColourDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2;
        int r = c1.getRed() - c2.getRed();
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2 + rmean / 256;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256;
        return Math.sqrt(weightR * r * r + weightG * g * g + weightB * b * b);
    }
}
