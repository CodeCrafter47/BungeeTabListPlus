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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.Placeholder;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderManager;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotBuilder;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderManagerImpl implements PlaceholderManager {

    private final List<Placeholder> placeholders = new ArrayList<>();

    private Pattern pattern_all = null;
    private boolean needsUpdate = true;

    private final List<PlaceholderProvider> placeholderProviderList = new ArrayList<>();

    @Override
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

    @Override
    public void registerPlaceholderProvider(PlaceholderProvider placeholderProvider) {
        BungeeTabListPlus.getInstance().registerPlaceholderProvider0(placeholderProvider);
    }

    @SneakyThrows
    private void update() {
        synchronized (placeholders) {
            placeholders.clear();
            for (PlaceholderProvider placeholderProvider : placeholderProviderList) {
                boolean isExternal = !placeholderProvider.getClass().getClassLoader().equals(PlaceholderManagerImpl.class.getClassLoader());
                Field registry = PlaceholderProvider.class.getDeclaredField("registry");
                registry.setAccessible(true);
                registry.set(placeholderProvider, isExternal
                        ? (PlaceholderProvider.PlaceholderRegistry) placeholder -> placeholders.add(new ExceptionSafePlaceholder(placeholder))
                        : (PlaceholderProvider.PlaceholderRegistry) placeholders::add);
                placeholderProvider.setup();
            }
            pattern_all = Pattern.compile("(?ims)" + Joiner.on('|').join(Iterables.transform(placeholders, v -> "(?:" + v.getRegex() + ")")));
            needsUpdate = false;
        }
    }

    public void reload() {
        needsUpdate = true;
    }

    public void internalRegisterPlaceholderProvider(PlaceholderProvider placeholderProvider) {
        placeholderProviderList.add(placeholderProvider);
        needsUpdate = true;
    }

    private static class ExceptionSafePlaceholder extends Placeholder {
        private final Placeholder delegate;

        public ExceptionSafePlaceholder(Placeholder delegate) {
            super(delegate.getRegex());
            this.delegate = delegate;
        }

        @Override
        public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
            try {
                return new ExceptionSafeSlotTemplate(delegate.getReplacement(placeholderManager, matcher));
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "An error occurred while resolving an external placeholder.", th);
                return SlotTemplate.empty();
            }
        }

        private static class ExceptionSafeSlotTemplate extends SlotTemplate {
            private final SlotTemplate delegate;

            private ExceptionSafeSlotTemplate(SlotTemplate delegate) {
                this.delegate = delegate;
            }

            @Override
            public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                try {
                    return delegate.buildSlot(builder, context);
                } catch (Throwable th) {
                    BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "An error occurred while replacing an external placeholder.", th);
                    return builder;
                }
            }
        }
    }

}
