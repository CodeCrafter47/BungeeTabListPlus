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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.api.Placeholder;
import codecrafter47.bungeetablistplus.api.PlaceholderRegistry;
import codecrafter47.bungeetablistplus.api.PlaceholderProvider;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderManager {

    private final List<Placeholder> placeholders = new ArrayList<>();

    private Pattern pattern_all = null;
    private boolean needsUpdate = true;

    private final List<PlaceholderProvider> placeholderProviderList = new ArrayList<>();

    public SlotTemplate parseSlot(String text) {
        Preconditions.checkNotNull(text, "text");
        if (needsUpdate) {
            update();
        }

        SlotTemplate.SlotTemplateBuilder templateBuilder = SlotTemplate.builder();

        Matcher matcher = pattern_all.matcher(text);
        while (matcher.find()) {
            String group0 = matcher.group();

            StringBuffer buffer = new StringBuffer();
            matcher.appendReplacement(buffer, "");
            templateBuilder.append(buffer.toString());

            boolean resolved = false;
            for (Placeholder placeholder : placeholders) {
                Matcher placeholderMatcher = Pattern.compile("(?ims)" + placeholder.getRegex()).matcher(group0);
                if (placeholderMatcher.matches()) {
                    templateBuilder.append(placeholder.getReplacement(this, placeholderMatcher));
                    resolved = true;
                    break;
                }
            }

            if (!resolved) {
                throw new RuntimeException("Failed to resolve placeholder '" + group0 + "'");
            }
        }

        StringBuffer buffer = new StringBuffer();
        matcher.appendTail(buffer);
        templateBuilder.append(buffer.toString());

        return templateBuilder.build();
    }

    @SneakyThrows
    private void update() {
        placeholders.clear();
        for (PlaceholderProvider placeholderProvider : placeholderProviderList) {
            Field registry = PlaceholderProvider.class.getDeclaredField("registry");
            registry.setAccessible(true);
            registry.set(placeholderProvider, (PlaceholderRegistry) placeholders::add);
            placeholderProvider.setup();
        }
        pattern_all = Pattern.compile("(?ims)" + Joiner.on('|').join(Iterables.transform(placeholders, v -> "(?:" + v.getRegex() + ")")));
        needsUpdate = false;
    }

    public void reload() {
        needsUpdate = true;
    }

    public void registerPlaceholderProvider(PlaceholderProvider placeholderProvider) {
        placeholderProviderList.add(placeholderProvider);
        needsUpdate = true;
    }
}
