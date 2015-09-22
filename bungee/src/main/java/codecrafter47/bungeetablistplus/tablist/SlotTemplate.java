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

package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.skin.Skin;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SlotTemplate {
    private static SlotTemplate emptyTemplate = new SlotTemplate() {
        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            return builder;
        }
    };

    public static SlotTemplate ping(int ping) {
        return new SlotTemplatePing(ping);
    }

    public static SlotTemplate text(String text) {
        return new SlotTemplateText(text);
    }

    public static SlotTemplate skin(Skin skin) {
        return new SlotTemplateSkin(skin);
    }

    public static SlotTemplate of(SlotTemplate... templates) {
        return of(Arrays.asList(templates));
    }

    public static SlotTemplate of(Iterable<SlotTemplate> templates) {
        return new SlotTemplateCompound(templates);
    }

    public static SlotTemplateBuilder builder() {
        return new SlotTemplateBuilder();
    }

    public static SlotTemplate empty() {
        return emptyTemplate;
    }

    public Slot buildSlot(TabListContext context) {
        return buildSlot(new SlotBuilder(), context).build();
    }

    public abstract SlotBuilder buildSlot(SlotBuilder builder, TabListContext context);

    private static class SlotTemplateText extends SlotTemplate {
        private final String text;

        private SlotTemplateText(String text) {
            this.text = text;
        }

        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            return builder.appendText(text);
        }
    }

    private static class SlotTemplatePing extends SlotTemplate {
        private final int ping;

        private SlotTemplatePing(int ping) {
            this.ping = ping;
        }

        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            return builder.setPing(ping);
        }
    }

    private static class SlotTemplateSkin extends SlotTemplate {
        private final Skin skin;

        private SlotTemplateSkin(Skin skin) {
            this.skin = skin;
        }

        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            return builder.setSkin(skin);
        }
    }

    private static class SlotTemplateCompound extends SlotTemplate {
        private final Iterable<SlotTemplate> templates;

        private SlotTemplateCompound(Iterable<SlotTemplate> templates) {
            this.templates = templates;
        }

        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            for (SlotTemplate template : templates) {
                builder = template.buildSlot(builder, context);
            }
            return builder;
        }
    }

    public static class SlotTemplateBuilder {
        private final List<SlotTemplate> templates;

        private SlotTemplateBuilder() {
            templates = new ArrayList<>();
        }

        public SlotTemplateBuilder append(SlotTemplate template) {
            templates.add(template);
            return this;
        }

        public SlotTemplateBuilder append(String text) {
            return append(new SlotTemplateText(text));
        }

        public SlotTemplateBuilder setPing(int ping) {
            return append(new SlotTemplatePing(ping));
        }

        public SlotTemplateBuilder setSkin(Skin skin) {
            return append(new SlotTemplateSkin(skin));
        }

        public SlotTemplate build() {
            return new SlotTemplateCompound(ImmutableList.copyOf(templates));
        }
    }
}
