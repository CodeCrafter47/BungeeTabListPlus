package codecrafter47.bungeetablistplus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing a chat formatting syntax similar to bbcode.
 *
 * Details:
 * For example [b]this is bold[/b], [i]this is italic[/i], [u]this is underlined[/u] and [s]this is crossed out[/s].
 * The difference between the above and making something &amp;lbold&amp;r the vanilla way is, that the above makes all the
 * enclosed text bold, while &amp;b makes bold everything until reaching the next color code.
 * Same for [color=...]
 *
 * How links will work is easy to guess, e.g. it's just [url]spigotmc.org[/url] or [url=spigotmc.org]click here[/url].
 * Executing commands works similar [command=/tp CodeCrafter47]click here[/command].
 *
 * Suggesting commands works with [suggest=/tp ]...[/suggest]
 * To create tooltips do [hover=Text magically appears when moving the mouse over]this[/hover].
 *
 * It is possible to use [nocolor][/nocolor] to prevent the use of legacy color codes in a block;
 * [nobbcode][/nobbcode] will prevent the use of bbcode in a block;
 *
 * Vanilla color codes still work and can be mixed with the [color=..] and other formatting tags without problems.
 */
public class ChatUtil {

    public static final Map<Character, TextFormat> BY_CHAR = new HashMap<Character, TextFormat>() {{
        put('0', NamedTextColor.BLACK);
        put('1', NamedTextColor.DARK_BLUE);
        put('2', NamedTextColor.DARK_GREEN);
        put('3', NamedTextColor.DARK_AQUA);
        put('4', NamedTextColor.DARK_RED);
        put('5', NamedTextColor.DARK_PURPLE);
        put('6', NamedTextColor.GOLD);
        put('7', NamedTextColor.GRAY);
        put('8', NamedTextColor.DARK_GRAY);
        put('9', NamedTextColor.BLUE);
        put('a', NamedTextColor.GREEN);
        put('b', NamedTextColor.AQUA);
        put('c', NamedTextColor.RED);
        put('d', NamedTextColor.LIGHT_PURPLE);
        put('e', NamedTextColor.YELLOW);
        put('f', NamedTextColor.WHITE);
        put('k', TextDecoration.OBFUSCATED);
        put('l', TextDecoration.BOLD);
        put('m', TextDecoration.STRIKETHROUGH);
        put('n', TextDecoration.UNDERLINED);
        put('o', TextDecoration.ITALIC);
        put('r', CustomFormat.RESET);
    }};

    private static final String NON_UNICODE_CHARS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

    private static final int[] NON_UNICODE_CHAR_WIDTHS = new int[]{6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 4, 4, 6, 7, 6, 6, 6, 6, 6, 6, 1, 1, 1, 1, 1, 1, 1, 4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6, 7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 2, 6, 6, 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5, 9, 9, 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7, 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1};

    private static final byte[] UNICODE_CHAR_WIDTHS = new byte[65536];

    static {
        InputStream resourceAsStream = ChatUtil.class.getResourceAsStream("unicode.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        try {
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                UNICODE_CHAR_WIDTHS[i++] = Byte.valueOf(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final int DEFAULT_CHAT_LINE_WIDTH = 320;

    private static final Pattern pattern = Pattern.compile("(?is)(?=\\n)|(?:[&\u00A7](?<color>[0-9A-FK-OR]))|" +
            "(?:\\[(?<tag>/?(?:b|i|u|s|nocolor|nobbcode)|(?:url|command|hover|suggest|color)=(?<value>(?:(?:[^]\\[]*)\\[(?:[^]\\[]*)\\])*(?:[^]\\[]*))|/(?:url|command|hover|suggest|color))\\])|" +
            "(?:\\[(?<implicitTag>url|command|suggest)\\](?=(?<implicitValue>.*?)\\[/\\k<implicitTag>\\]))");

    private static final Pattern strip_bbcode_pattern = Pattern.compile("(?is)(?:\\[(?<tag>/?(?:b|i|u|s|nocolor|nobbcode)|(?:url|command|hover|suggest|color)=(?<value>(?:(?:[^]\\[]*)\\[(?:[^]\\[]*)\\])*(?:[^]\\[]*))|/(?:url|command|hover|suggest|color))\\])|" +
            "(?:\\[(?<implicitTag>url|command|suggest)\\](?=(?<implicitValue>.*?)\\[/\\k<implicitTag>\\]))");

    private static final Logger logger = Logger.getLogger("Minecraft");

    public static Component parseBBCode(String text) {
        Matcher matcher = pattern.matcher(text);
        Component current = Component.text("");
        List<Component> components = new LinkedList<>();
        int forceBold = 0;
        int forceItalic = 0;
        int forceUnderlined = 0;
        int forceStrikethrough = 0;
        int nocolorLevel = 0;
        int nobbcodeLevel = 0;
        Deque<TextColor> colorDeque = new LinkedList<>();
        Deque<ClickEvent> clickEventDeque = new LinkedList<>();
        Deque<HoverEvent<Component>> hoverEventDeque = new LinkedList<>();
        while (matcher.find()) {
            boolean parsed = false;
            {
                StringBuffer stringBuffer = new StringBuffer();
                matcher.appendReplacement(stringBuffer, "");
                Component component = Component.text(stringBuffer.toString());
                components.add(component.mergeStyle(current));
            }
            String group_color = matcher.group("color");
            String group_tag = matcher.group("tag");
            String group_value = matcher.group("value");
            String group_implicitTag = matcher.group("implicitTag");
            String group_implicitValue = matcher.group("implicitValue");
            if (group_color != null && nocolorLevel <= 0) {
                TextFormat color = BY_CHAR.get(group_color.charAt(0));
                if (color != null) {
                    if (TextDecoration.OBFUSCATED.equals(color)) {
                        current = current.decorate(TextDecoration.OBFUSCATED);
                    } else if (TextDecoration.BOLD.equals(color)) {
                        current = current.decorate(TextDecoration.BOLD);
                    } else if (TextDecoration.STRIKETHROUGH.equals(color)) {
                        current = current.decorate(TextDecoration.STRIKETHROUGH);
                    } else if (TextDecoration.UNDERLINED.equals(color)) {
                        current = current.decorate(TextDecoration.UNDERLINED);
                    } else if (TextDecoration.ITALIC.equals(color)) {
                        current = current.decorate(TextDecoration.ITALIC);
                    } else {
                        if (CustomFormat.RESET.equals(color))
                            color = NamedTextColor.WHITE;

                        current = Component.text("");
                        current = current.color((TextColor) color);
                        current = current.decoration(TextDecoration.BOLD, forceBold > 0);
                        current = current.decoration(TextDecoration.ITALIC, forceItalic > 0);
                        current = current.decoration(TextDecoration.UNDERLINED, forceUnderlined > 0);
                        current = current.decoration(TextDecoration.STRIKETHROUGH, forceStrikethrough > 0);
                        if (!colorDeque.isEmpty()) {
                            current = current.color(colorDeque.peek());
                        }
                        if (!clickEventDeque.isEmpty()) {
                            current = current.clickEvent(clickEventDeque.peek());
                        }
                        if (!hoverEventDeque.isEmpty()) {
                            current = current.hoverEvent(hoverEventDeque.peek());
                        }
                    }
                    parsed = true;
                }
            }
            if (group_tag != null && nobbcodeLevel <= 0) {
                // [b]this is bold[/b]
                if (group_tag.matches("(?is)^b$")) {
                    forceBold++;
                    current = current.decoration(TextDecoration.BOLD, forceBold > 0);
                    parsed = true;
                } else if (group_tag.matches("(?is)^/b$")) {
                    forceBold--;
                    current = current.decoration(TextDecoration.BOLD, forceBold > 0);
                    parsed = true;
                }
                // [i]this is italic[/i]
                if (group_tag.matches("(?is)^i$")) {
                    forceItalic++;
                    current = current.decoration(TextDecoration.ITALIC, forceItalic > 0);
                    parsed = true;
                } else if (group_tag.matches("(?is)^/i$")) {
                    forceItalic--;
                    current = current.decoration(TextDecoration.ITALIC, forceItalic > 0);
                    parsed = true;
                }
                // [u]this is underlined[/u]
                if (group_tag.matches("(?is)^u$")) {
                    forceUnderlined++;
                    current = current.decoration(TextDecoration.UNDERLINED, forceUnderlined > 0);
                    parsed = true;
                } else if (group_tag.matches("(?is)^/u$")) {
                    forceUnderlined--;
                    current = current.decoration(TextDecoration.UNDERLINED, forceUnderlined > 0);
                    parsed = true;
                }
                // [s]this is crossed out[/s]
                if (group_tag.matches("(?is)^s$")) {
                    forceStrikethrough++;
                    current = current.decoration(TextDecoration.STRIKETHROUGH, forceStrikethrough > 0);
                    parsed = true;
                } else if (group_tag.matches("(?is)^/s$")) {
                    forceStrikethrough--;
                    current = current.decoration(TextDecoration.STRIKETHROUGH, forceStrikethrough > 0);
                    parsed = true;
                }
                // [color=red]huh this is red...[/color]
                if (group_tag.matches("(?is)^color=.*$")) {
                    NamedTextColor color = null;
                    for (TextFormat color1 : BY_CHAR.values()) {
                        if (color1 instanceof NamedTextColor && ((NamedTextColor)color1).toString().equalsIgnoreCase(group_value)) {
                            color = (NamedTextColor) color1;
                        }
                    }
                    colorDeque.push(current.color());
                    if (color != null) {
                        colorDeque.push(color);
                        current = current.color(color);
                    } else {
                        logger.warning("Invalid color tag: [" + group_tag + "] UNKNOWN COLOR '" + group_value + "'");
                        colorDeque.push(NamedTextColor.WHITE);
                        current = current.color(NamedTextColor.WHITE);
                    }
                    parsed = true;
                } else if (group_tag.matches("(?is)^/color$")) {
                    if (!colorDeque.isEmpty()) {
                        colorDeque.pop();
                        current = current.color(colorDeque.pop());
                    }
                    parsed = true;
                }
                // [url=....]
                if (group_tag.matches("(?is)^url=.*$")) {
                    String url = group_value;
                    url = url.replaceAll("(?is)\\[/?nobbcode\\]", "");
                    if (!url.startsWith("http")) {
                        url = "http://" + url;
                    }
                    ClickEvent clickEvent = ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url);
                    clickEventDeque.push(clickEvent);
                    current = current.clickEvent(clickEvent);
                    parsed = true;
                }
                // [/url], [/command], [/suggest]
                if (group_tag.matches("(?is)^/(?:url|command|suggest)$")) {
                    if (!clickEventDeque.isEmpty()) clickEventDeque.pop();
                    current = current.clickEvent(clickEventDeque.isEmpty() ? null : clickEventDeque.peek());
                    parsed = true;
                }
                // [command=....]
                if (group_tag.matches("(?is)^command=.*")) {
                    group_value = group_value.replaceAll("(?is)\\[/?nobbcode\\]", "");
                    ClickEvent clickEvent = ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, group_value);
                    clickEventDeque.push(clickEvent);
                    current = current.clickEvent(clickEvent);
                    parsed = true;
                }
                // [suggest=....]
                if (group_tag.matches("(?is)^suggest=.*")) {
                    group_value = group_value.replaceAll("(?is)\\[/?nobbcode\\]", "");
                    ClickEvent clickEvent = ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, group_value);
                    clickEventDeque.push(clickEvent);
                    current = current.clickEvent(clickEvent);
                    parsed = true;
                }
                // [hover=....]...[/hover]
                if (group_tag.matches("(?is)^hover=.*$")) {
                    Component components1 = parseBBCode(group_value);
                    if (!hoverEventDeque.isEmpty()) {
                        components1 = Component.text().append(hoverEventDeque.getLast().value()).append(Component.text("\n")).append(components).build();
                    }
                    HoverEvent<Component> hoverEvent = HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, components1);
                    hoverEventDeque.push(hoverEvent);
                    current = current.hoverEvent(hoverEvent);
                    parsed = true;
                } else if (group_tag.matches("(?is)^/hover$")) {
                    if (!hoverEventDeque.isEmpty()) hoverEventDeque.pop();
                    current = current.hoverEvent(hoverEventDeque.isEmpty() ? null : hoverEventDeque.peek());
                    parsed = true;
                }
            }
            if (group_implicitTag != null && nobbcodeLevel <= 0) {
                // [url]spigotmc.org[/url]
                if (group_implicitTag.matches("(?is)^url$")) {
                    String url = group_implicitValue;
                    if (!url.startsWith("http")) {
                        url = "http://" + url;
                    }
                    ClickEvent clickEvent = ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, url);
                    clickEventDeque.push(clickEvent);
                    current = current.clickEvent(clickEvent);
                    parsed = true;
                }
                // [command]/spawn[/command]
                if (group_implicitTag.matches("(?is)^command$")) {
                    ClickEvent clickEvent = ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, group_implicitValue);
                    clickEventDeque.push(clickEvent);
                    current = current.clickEvent(clickEvent);
                    parsed = true;
                }
                // [suggest]/friend add [/suggest]
                if (group_implicitTag.matches("(?is)^suggest$")) {
                    ClickEvent clickEvent = ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, group_implicitValue);
                    clickEventDeque.push(clickEvent);
                    current = current.clickEvent(clickEvent);
                    parsed = true;
                }
            }
            if (group_tag != null) {
                if (group_tag.matches("(?is)^nocolor$")) {
                    nocolorLevel++;
                    parsed = true;
                }
                if (group_tag.matches("(?is)^/nocolor$")) {
                    nocolorLevel--;
                    parsed = true;
                }
                if (group_tag.matches("(?is)^nobbcode$")) {
                    nobbcodeLevel++;
                    parsed = true;
                }
                if (group_tag.matches("(?is)^/nobbcode$")) {
                    nobbcodeLevel--;
                    parsed = true;
                }
            }
            if (!parsed) {
                Component component = Component.text(matcher.group(0));
                components.add(component.mergeStyle(current));
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        matcher.appendTail(stringBuffer);
        Component component = Component.text(stringBuffer.toString());
        components.add(component.mergeStyle(current));
        Component output = Component.text("");
        for(Component comp : components){
            output = output.append(comp);
        }
        return output;
    }

    public static String stripBBCode(String string){
        return strip_bbcode_pattern.matcher(string).replaceAll("");
    }

    public static double getCharWidth(int codePoint, boolean isBold) {
        int nonUnicodeIdx = NON_UNICODE_CHARS.indexOf(codePoint);
        double width;
        if (nonUnicodeIdx != -1) {
            width = NON_UNICODE_CHAR_WIDTHS[nonUnicodeIdx];
            if (isBold) {
                width += 1;
            }
        } else {
            // MC unicode -- what does this even do? but it's client-only so we can't use it directly :/
            int j = UNICODE_CHAR_WIDTHS[codePoint] >>> 4;
            int k = UNICODE_CHAR_WIDTHS[codePoint] & 15;

            if (k > 7) {
                k = 15;
                j = 0;
            }
            width = ((k + 1) - j) / 2 + 1;
            if (isBold) {
                width += 0.5;
            }
        }
        return width;
    }

    public static int getLength(Component text) {
        double length = 0;
        for (Component child : text.children()) {
            final String txt;
            if (child instanceof TextComponent) {
                txt = ((TextComponent) child).content();
            } else { // TODO translatable components
                continue;
            }
            boolean isBold = child.hasDecoration(TextDecoration.BOLD);
            for (int i = 0; i < txt.length(); ++i) {
                length += getCharWidth(txt.codePointAt(i), isBold);
            }
        }
        return (int) Math.ceil(length);
    }

    public enum CustomFormat implements TextFormat {
        RESET("reset");
        private final String name;
        CustomFormat(String name){
            this.name = name;
        }
    }
}
