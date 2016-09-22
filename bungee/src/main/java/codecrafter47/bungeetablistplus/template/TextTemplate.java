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

package codecrafter47.bungeetablistplus.template;

import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.placeholder.Placeholder;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTemplate {
    private static Pattern placeholder = Pattern.compile("(?ms)\\$\\{([^}]+)\\}");
    private List<Part> parts;

    public static final TextTemplate EMPTY = new TextTemplate("");

    public TextTemplate(String text) {
        parts = new ArrayList<>();

        Matcher matcher = placeholder.matcher(text);
        while (matcher.find()) {
            StringBuffer sb = new StringBuffer();
            matcher.appendReplacement(sb, "");
            parts.add(new StringPart(sb.toString()));
            parts.add(new PlaceholderPart(matcher.group(1)));
        }
        StringBuffer sb = new StringBuffer();
        matcher.appendTail(sb);
        parts.add(new StringPart(sb.toString()));
    }

    public String evaluate(Context context) {
        if (parts.size() == 1) {
            return parts.get(0).evaluate(context);
        } else {
            String result = "";
            for (Part part : parts) {
                result += part.evaluate(context);
            }
            return result;
        }
    }

    private static abstract class Part {
        public abstract String evaluate(Context context);
    }

    @AllArgsConstructor
    private static class StringPart extends Part {
        private String text;

        @Override
        public String evaluate(Context context) {
            return text;
        }
    }

    private static class PlaceholderPart extends Part {
        private Placeholder placeholder;

        public PlaceholderPart(String s) {
            this.placeholder = Placeholder.of(s);
        }

        @Override
        public String evaluate(Context context) {
            return placeholder.evaluate(context);
        }
    }
}
