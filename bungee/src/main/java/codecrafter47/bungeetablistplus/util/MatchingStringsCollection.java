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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class MatchingStringsCollection {

    private final List<String> patterns;
    private final Object2BooleanMap<String> cache = new Object2BooleanOpenHashMap<>();

    public MatchingStringsCollection(List<String> patterns) {
        this.patterns = patterns.stream()
                .filter(regex -> {
                    try {
                        Pattern.compile(regex);
                        return true;
                    } catch (PatternSyntaxException e) {
                        BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Illegal regex", e);
                        return false;
                    }
                }).collect(Collectors.toList());
    }

    public boolean contains(String s) {
        if (cache.containsKey(s)) {
            return cache.getBoolean(s);
        } else {
            boolean r = compute(s);
            cache.put(s, r);
            return r;
        }
    }

    private boolean compute(String s) {
        for (String pattern : patterns) {
            try {
                if (s.matches(pattern)) {
                    return true;
                }
            } catch (PatternSyntaxException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
