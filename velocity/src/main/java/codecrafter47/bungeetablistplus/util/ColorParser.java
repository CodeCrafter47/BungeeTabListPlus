/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.bungeetablistplus.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorParser {

    public static String extractColorCodes(String s) {
        boolean bold = false;
        boolean underlined = false;
        boolean magic = false;
        boolean italic = false;
        boolean strikethrough = false;
        NamedTextColor color = NamedTextColor.WHITE;

        boolean escaped = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (escaped) {
                TextFormat code = ChatUtil.BY_CHAR.get(ch);
                if (code == null) {
                    // ignore
                } else if (code.equals(TextDecoration.BOLD)) {
                    bold = true;
                } else if (code.equals(TextDecoration.ITALIC)) {
                    italic = true;
                } else if (code.equals(TextDecoration.UNDERLINED)) {
                    underlined = true;
                } else if (code.equals(TextDecoration.STRIKETHROUGH)) {
                    strikethrough = true;
                } else if (code.equals(TextDecoration.OBFUSCATED)) {
                    magic = true;
                } else if (code.equals(ChatUtil.CustomFormat.RESET)) {
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                    magic = false;
                    color = NamedTextColor.WHITE;
                } else {
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                    magic = false;
                    color = (NamedTextColor) code;
                }
                escaped = false;
            }
            if (ch == LegacyComponentSerializer.SECTION_CHAR) {
                escaped = true;
            }
        }

        StringBuilder string = new StringBuilder();
        if (!color.equals(NamedTextColor.WHITE)) {
            string.append(color);
        }
        if (bold) {
            string.append(TextDecoration.BOLD);
        }
        if (italic) {
            string.append(TextDecoration.ITALIC);
        }
        if (underlined) {
            string.append(TextDecoration.UNDERLINED);
        }
        if (strikethrough) {
            string.append(TextDecoration.STRIKETHROUGH);
        }
        if (magic) {
            string.append(TextDecoration.OBFUSCATED);
        }

        return string.toString();
    }

    public static int endofColor(String s, int start) {
        boolean escaped = false;
        for (int i = start - 1; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (ch == LegacyComponentSerializer.SECTION_CHAR) {
                escaped = true;
            } else if (i < start) {

            } else {
                return i;
            }
        }
        return s.length() - 1;
    }
}
