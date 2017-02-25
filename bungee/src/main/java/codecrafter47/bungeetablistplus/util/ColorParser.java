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
package codecrafter47.bungeetablistplus.util;

import net.md_5.bungee.api.ChatColor;

public class ColorParser {

    public static String extractColorCodes(String s) {
        boolean bold = false;
        boolean underlined = false;
        boolean magic = false;
        boolean italic = false;
        boolean strikethrough = false;
        ChatColor color = ChatColor.WHITE;

        boolean escaped = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (escaped) {
                ChatColor code = ChatColor.getByChar(ch);
                if (code == null) {
                    // ignore
                } else if (code.equals(ChatColor.BOLD)) {
                    bold = true;
                } else if (code.equals(ChatColor.ITALIC)) {
                    italic = true;
                } else if (code.equals(ChatColor.UNDERLINE)) {
                    underlined = true;
                } else if (code.equals(ChatColor.STRIKETHROUGH)) {
                    strikethrough = true;
                } else if (code.equals(ChatColor.MAGIC)) {
                    magic = true;
                } else if (code.equals(ChatColor.RESET)) {
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                    magic = false;
                    color = ChatColor.WHITE;
                } else {
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                    magic = false;
                    color = code;
                }
                escaped = false;
            }
            if (ch == ChatColor.COLOR_CHAR) {
                escaped = true;
            }
        }

        StringBuilder string = new StringBuilder();
        if (!color.equals(ChatColor.WHITE)) {
            string.append(color);
        }
        if (bold) {
            string.append(ChatColor.BOLD);
        }
        if (italic) {
            string.append(ChatColor.ITALIC);
        }
        if (underlined) {
            string.append(ChatColor.UNDERLINE);
        }
        if (strikethrough) {
            string.append(ChatColor.STRIKETHROUGH);
        }
        if (magic) {
            string.append(ChatColor.MAGIC);
        }

        return string.toString();
    }

    public static int endofColor(String s, int start) {
        boolean escaped = false;
        for (int i = start - 1; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (ch == ChatColor.COLOR_CHAR) {
                escaped = true;
            } else if (i < start) {

            } else {
                return i;
            }
        }
        return s.length() - 1;
    }
}
