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

package codecrafter47.bungeetablistplus.api;

import codecrafter47.bungeetablistplus.managers.PlaceholderManager;
import codecrafter47.bungeetablistplus.tablist.SlotBuilder;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;

public abstract class PlaceholderProvider {
    private PlaceholderRegistry registry = null;

    public abstract void setup();

    protected PlaceholderBuilder bind(String name) {
        return new PlaceholderBuilder(name);
    }

    protected RegexVariablePlaceholderBuilder bindRegex(String regex) {
        return new RegexVariablePlaceholderBuilder(regex);
    }

    public class RegexVariablePlaceholderBuilder {
        private final String regex;

        public RegexVariablePlaceholderBuilder(String regex) {
            this.regex = regex;
        }

        public void to(BiFunction<PlaceholderManager, Matcher, SlotTemplate> function) {
            registry.registerPlaceholder(new Placeholder(regex) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    return function.apply(placeholderManager, matcher);
                }
            });
        }
    }

    public class PlaceholderBuilder {
        private final List<String> names;

        private PlaceholderBuilder(String name) {
            this(Collections.singletonList(name));
        }

        public PlaceholderBuilder(List<String> names) {
            this.names = names;
        }

        public void to(Function<TabListContext, String> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    return new SlotTemplate() {
                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return builder.appendText(function.apply(context));
                        }
                    };
                }
            });
        }

        public void toTemplate(Function<TabListContext, SlotTemplate> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    return new SlotTemplate() {
                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return function.apply(context).buildSlot(builder, context);
                        }
                    };
                }
            });
        }

        public ArgsPlaceholderBuilder withArgs() {
            return new ArgsPlaceholderBuilder(names);
        }

        public TemplateArgsPlaceholderBuilder withTemplateArgs() {
            return new TemplateArgsPlaceholderBuilder(names);
        }

        public PlaceholderBuilder alias(String alias) {
            ArrayList<String> names = new ArrayList<>();
            names.addAll(this.names);
            names.add(alias);
            return new PlaceholderBuilder(names);
        }
    }

    public class ArgsPlaceholderBuilder {
        private final List<String> names;

        private ArgsPlaceholderBuilder(List<String> names) {
            this.names = names;
        }

        public void to(BiFunction<TabListContext, String, String> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)(?::((?:(?:[^{}]*)\\{(?:[^{}]*)\\})*(?:[^{}]*)))?\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    String group = matcher.group(1);
                    return new SlotTemplate() {
                        private SlotTemplate args = group != null ? placeholderManager.parseSlot(group) : null;

                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return builder.appendText(function.apply(context, args != null ? args.buildSlot(context).getText() : null));
                        }
                    };
                }
            });
        }

        public void toTemplate(BiFunction<TabListContext, String, SlotTemplate> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)(?::((?:(?:[^{}]*)\\{(?:[^{}]*)\\})*(?:[^{}]*)))?\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    String group = matcher.group(1);
                    return new SlotTemplate() {
                        private SlotTemplate args = group != null ? placeholderManager.parseSlot(group) : null;

                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return function.apply(context, args != null ? args.buildSlot(context).getText() : null).buildSlot(builder, context);
                        }
                    };
                }
            });
        }
    }

    public class TemplateArgsPlaceholderBuilder {
        private final List<String> names;

        private TemplateArgsPlaceholderBuilder(List<String> names) {
            this.names = names;
        }

        public void to(BiFunction<TabListContext, SlotTemplate, SlotTemplate> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)(?::((?:(?:[^{}]*)\\{(?:[^{}]*)\\})*(?:[^{}]*)))?\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    String group = matcher.group(1);
                    return new SlotTemplate() {
                        private SlotTemplate args = group != null ? placeholderManager.parseSlot(group) : null;

                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return function.apply(context, args != null ? args : SlotTemplate.empty()).buildSlot(builder, context);
                        }
                    };
                }
            });
        }
    }

}
