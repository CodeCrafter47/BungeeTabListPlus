/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.util;

import net.md_5.bungee.api.ChatColor;

/**
 *
 * @author florian
 */
public class ColorParser {

    public static String extractColorCodes(String s) {
        boolean bold = false;
        boolean underlined = false;
        boolean magic = false;
        boolean italic = false;
        boolean durchgestrichen = false;
        ChatColor color = ChatColor.WHITE;

        boolean escaped = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (escaped) {
                ChatColor code = ChatColor.getByChar(ch);
                if (code.equals(ChatColor.BOLD)) {
                    bold = true;
                } else if (code.equals(ChatColor.ITALIC)) {
                    italic = true;
                } else if (code.equals(ChatColor.UNDERLINE)) {
                    underlined = true;
                } else if (code.equals(ChatColor.STRIKETHROUGH)) {
                    durchgestrichen = true;
                } else if (code.equals(ChatColor.MAGIC)) {
                    magic = true;
                } else if (code.equals(ChatColor.RESET)) {
                    bold = false;
                    italic = false;
                    underlined = false;
                    durchgestrichen = false;
                    magic = false;
                    color = ChatColor.WHITE;
                } else {
                    bold = false;
                    italic = false;
                    underlined = false;
                    durchgestrichen = false;
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
        if (durchgestrichen) {
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

    public static String substringIgnoreColors(String s, int num) {
        StringBuilder ret = new StringBuilder();
        boolean escaped = false;
        int size = 0;
        for (char ch : s.toCharArray()) {
            ret.append(ch);
            if (escaped) {
                escaped = false;
            } else if (ch == ChatColor.COLOR_CHAR) {
                escaped = true;
            } else {
                size++;
                if (size >= num) {
                    return ret.toString();
                }
            }
        }
        return ret.toString();
    }
}
