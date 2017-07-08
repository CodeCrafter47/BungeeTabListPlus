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

import codecrafter47.util.chat.ChatUtil;
import net.md_5.bungee.api.ChatColor;

public final class FastChat {
    private final static String emptyJsonText = "{\"text\":\"\"}";

    public static int legacyTextLength(String legacyText, char alternateColorChar) {
        double length = 0;
        boolean bold = false;
        for (int i = 0; i < legacyText.length(); ++i) {
            char c = legacyText.charAt(i);
            if (i + 1 < legacyText.length() && (c == ChatColor.COLOR_CHAR || (c == alternateColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(legacyText.charAt(i + 1)) > -1))) {
                c = legacyText.charAt(++i);
                if ("0123456789AaBbCcDdEeFf".indexOf(c) > -1) {
                    bold = false;
                } else if ("Ll".indexOf(c) > -1) {
                    bold = true;
                }
            } else {
                length += ChatUtil.getCharWidth(c, bold);
            }
        }
        return (int) Math.ceil(length);
    }

    public static String cropLegacyText(String legacyText, char alternateColorChar, int maxLength) {
        StringBuilder result = new StringBuilder(legacyText.length());
        double length = 0;
        boolean bold = false;
        for (int i = 0; i < legacyText.length(); ++i) {
            char c = legacyText.charAt(i);
            if (i + 1 < legacyText.length() && (c == ChatColor.COLOR_CHAR || (c == alternateColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(legacyText.charAt(i + 1)) > -1))) {
                result.append(c);
                c = legacyText.charAt(++i);
                result.append(c);
                if ("0123456789AaBbCcDdEeFf".indexOf(c) > -1) {
                    bold = false;
                } else if ("Ll".indexOf(c) > -1) {
                    bold = true;
                }
            } else {
                length += ChatUtil.getCharWidth(c, bold);
                if (length <= maxLength) {
                    result.append(c);
                } else {
                    break;
                }
            }
        }
        return result.toString();
    }

    public static String legacyTextToJson(String legacyText, char alternateColorChar) {
        if (legacyText == null) {
            return null;
        }
        if (legacyText.isEmpty()) {
            return emptyJsonText;
        }
        // evil optimizations
        StringBuilder builder = new StringBuilder(legacyText.length() + 10);
        builder.append("{\"text\":\"");
        for (int i = 0; i < legacyText.length(); ++i) {
            char c = legacyText.charAt(i);
            if (i + 1 < legacyText.length() && (c == ChatColor.COLOR_CHAR || (c == alternateColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(legacyText.charAt(i + 1)) > -1))) {
                builder.append(ChatColor.COLOR_CHAR);
                c = legacyText.charAt(++i);
            }
            if (c == '"') {
                builder.append("\\\"");
            } else if (c == '\\') {
                builder.append("\\\\");
            } else {
                builder.append(c);
            }
        }
        builder.append("\"}");
        return new String(builder);
        /*
        StringBuilder jsonBuilder = new StringBuilder("{\"text\":\"\",\"extra\":[");
        StringBuilder builder = new StringBuilder();
        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;
        boolean first = true;
        ChatColor color = ChatColor.WHITE;

        for (int i = 0; i < legacyText.length(); ++i) {
            char c = legacyText.charAt(i);
            if (i + 1 < legacyText.length() && (c == ChatColor.COLOR_CHAR || (c == alternateColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(legacyText.charAt(i + 1)) > -1))) {
                ++i;
                c = legacyText.charAt(i);
                if (c >= 65 && c <= 90) {
                    c = (char) (c + 32);
                }

                if (builder.length() > 0) {
                    if (first) {
                        first = false;
                    } else {
                        jsonBuilder.append(",");
                    }
                    jsonBuilder.append("{\"text\":\"").append(builder.toString()).append("\"");
                    jsonBuilder.append(",\"color\":\"").append(color.getName()).append("\"");
                    if (bold) {
                        jsonBuilder.append(",\"bold\":\"true\"");
                    }
                    if (italic) {
                        jsonBuilder.append(",\"italic\":\"true\"");
                    }
                    if (underlined) {
                        jsonBuilder.append(",\"underlined\":\"true\"");
                    }
                    if (strikethrough) {
                        jsonBuilder.append(",\"strikethrough\":\"true\"");
                    }
                    if (obfuscated) {
                        jsonBuilder.append(",\"obfuscated\":\"true\"");
                    }
                    jsonBuilder.append("}");
                    builder = new StringBuilder();
                }

                ChatColor var10 = ChatColor.getByChar(c);
                if (var10 != null) {
                    switch (var10) {
                        case BOLD:
                            bold = true;
                            break;
                        case ITALIC:
                            italic = true;
                            break;
                        case UNDERLINE:
                            underlined = true;
                            break;
                        case STRIKETHROUGH:
                            strikethrough = true;
                            break;
                        case MAGIC:
                            obfuscated = true;
                            break;
                        case RESET:
                            var10 = ChatColor.WHITE;
                        default:
                            bold = false;
                            italic = false;
                            underlined = false;
                            strikethrough = false;
                            obfuscated = false;
                            color = var10;
                    }
                }
            } else {
                if (c == '"') {
                    builder.append("\\\"");
                } else if (c == '\\') {
                    builder.append("\\\\");
                } else {
                    builder.append(c);
                }
            }
        }

        if (builder.length() > 0) {
            if (first) {
                first = false;
            } else {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("{\"text\":\"").append(builder.toString()).append("\"");
            jsonBuilder.append(",\"color\":\"").append(color.getName()).append("\"");
            if (bold) {
                jsonBuilder.append(",\"bold\":\"true\"");
            }
            if (italic) {
                jsonBuilder.append(",\"italic\":\"true\"");
            }
            if (underlined) {
                jsonBuilder.append(",\"underlined\":\"true\"");
            }
            if (strikethrough) {
                jsonBuilder.append(",\"strikethrough\":\"true\"");
            }
            if (obfuscated) {
                jsonBuilder.append(",\"obfuscated\":\"true\"");
            }
            jsonBuilder.append("}");
        }

        if (first) {
            return emptyJsonText;
        }

        jsonBuilder.append("]}");
        return jsonBuilder.toString();
        */
    }
}
