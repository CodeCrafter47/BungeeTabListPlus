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

package codecrafter47.bungeetablistplus.api.bungee.tablist;

import codecrafter47.bungeetablistplus.api.bungee.Skin;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Template for creating a Slot
 */
public abstract class SlotTemplate {
    private static SlotTemplate emptyTemplate = new SlotTemplate() {
        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            return builder;
        }
    };

    /**
     * Creates a SlotTemplate that sets the ping of the Slot to the give value
     *
     * @param ping the ping
     * @return the created SlotTemplate
     */
    public static SlotTemplate ping(int ping) {
        return new SlotTemplatePing(ping);
    }

    /**
     * Creates a SlotTemplate that appends the given text to the Slot
     *
     * @param text the text
     * @return the created SlotTemplate
     */
    public static SlotTemplate text(String text) {
        return new SlotTemplateText(text);
    }

    /**
     * Creates a SlotTemplate that sets the skin of the Slot
     *
     * @param skin the Skin
     * @return the created SlotTemplate
     */
    public static SlotTemplate skin(Skin skin) {
        return new SlotTemplateSkin(skin);
    }

    /**
     * Creates a SlotTemplate by combining all given SlotTemplates
     *
     * @param templates the SlotTemplates to combine
     * @return the combines SlotTemplate
     */
    public static SlotTemplate of(SlotTemplate... templates) {
        return of(Arrays.asList(templates));
    }

    /**
     * Creates a SlotTemplate by combining all given SlotTemplates
     *
     * @param templates the SlotTemplates to combine
     * @return the combines SlotTemplate
     */
    public static SlotTemplate of(Iterable<SlotTemplate> templates) {
        return new SlotTemplateCompound(templates);
    }

    /**
     * Creates an animated SlotTemplate
     * <p>
     * The animation is done by cycling through the given templates at
     * the given interval
     *
     * @param templates the templates
     * @param interval  the interval
     * @return the created SlotTemplate
     */
    public static SlotTemplate animate(List<SlotTemplate> templates, double interval) {
        Preconditions.checkArgument(!templates.isEmpty(), "List of templates is empty");
        Preconditions.checkArgument(interval > 0, "Interval must be greater than zero");
        return new SlotTemplateAnimated(templates, interval);
    }

    /**
     * Creates a new SlotTemplateBuilder
     *
     * @return the new SlotTemplateBuilder
     */
    public static SlotTemplateBuilder builder() {
        return new SlotTemplateBuilder();
    }

    /**
     * Get an empty SlotTemplate
     *
     * @return an empty SlotTemplate
     */
    public static SlotTemplate empty() {
        return emptyTemplate;
    }

    private Slot cached = null;

    /**
     * Build a Slot using the given TabListContext
     *
     * @param context the TabListContext
     * @return the created Slot
     */
    public Slot buildSlot(TabListContext context) {
        if (cached != null) {
            return cached;
        }
        Slot slot = buildSlot(new SlotBuilder(), context).build();
        if (isConstant()) {
            cached = slot;
        }
        return slot;
    }

    /**
     * Build a Slot using the given TabListContext
     *
     * @param builder the builder which should be used
     * @param context the TabListContext
     * @return the builder
     */
    public abstract SlotBuilder buildSlot(SlotBuilder builder, TabListContext context);

    /**
     * Whether this SlotTemplate is immutable
     *
     * @return whether this SlotTemplate is immutable
     */
    protected boolean isConstant() {
        return false;
    }

    private static class SlotTemplateText extends SlotTemplate {
        private final String text;

        private SlotTemplateText(String text) {
            this.text = text;
        }

        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            return builder.appendText(text);
        }

        @Override
        protected boolean isConstant() {
            return true;
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

        @Override
        protected boolean isConstant() {
            return true;
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

        @Override
        protected boolean isConstant() {
            return true;
        }
    }

    private static class SlotTemplateAnimated extends SlotTemplate {
        private final List<SlotTemplate> templates;
        private final long intervalMillis;

        private SlotTemplateAnimated(List<SlotTemplate> templates, double interval) {
            this.templates = templates;
            this.intervalMillis = (long) (interval * 1000);
        }

        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            int index = (int) ((System.currentTimeMillis() / intervalMillis) % templates.size());
            return templates.get(index).buildSlot(builder, context);
        }
    }

    private static class SlotTemplateCompound extends SlotTemplate {
        private final Iterable<SlotTemplate> templates;
        private final boolean constant;

        private SlotTemplateCompound(Iterable<SlotTemplate> templates) {
            this.templates = templates;
            boolean b = true;
            for (SlotTemplate template : templates) {
                b = b && template.isConstant();
            }
            constant = b;
        }

        @Override
        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
            for (SlotTemplate template : templates) {
                builder = template.buildSlot(builder, context);
            }
            return builder;
        }

        @Override
        protected boolean isConstant() {
            return constant;
        }
    }

    /**
     * Utility class to simplify the creation of an SlotTemplate
     */
    public static class SlotTemplateBuilder {
        private final List<SlotTemplate> templates;

        private SlotTemplateBuilder() {
            templates = new ArrayList<>();
        }

        /**
         * Appends a SlotTemplate
         *
         * @param template the SlotTemplate
         * @return itself
         */
        public SlotTemplateBuilder append(SlotTemplate template) {
            templates.add(template);
            return this;
        }

        /**
         * Appends text to the SlotTemplate
         *
         * @param text the text
         * @return itself
         */
        public SlotTemplateBuilder append(String text) {
            return append(new SlotTemplateText(text));
        }

        /**
         * Set the ping
         *
         * @param ping the ping
         * @return itself
         */
        public SlotTemplateBuilder setPing(int ping) {
            return append(new SlotTemplatePing(ping));
        }

        /**
         * Set the Skin
         *
         * @param skin the skin
         * @return itself
         */
        public SlotTemplateBuilder setSkin(Skin skin) {
            return append(new SlotTemplateSkin(skin));
        }

        /**
         * Builds a SlotTemplate from this SlotTemplateBuilder
         * <p>
         * The SlotTemplateBuilder shouldn't be used anymore after invoking this method
         *
         * @return the created SlotTemplate
         */
        public SlotTemplate build() {
            return new SlotTemplateCompound(ImmutableList.copyOf(templates));
        }
    }
}
