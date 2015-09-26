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

package codecrafter47.bungeetablistplus.api.bungee.placeholder;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotBuilder;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * A PlaceholderProvider provides multiple placeholders
 */
public abstract class PlaceholderProvider {
    private PlaceholderRegistry registry = null;

    /**
     * In this method the subclass should register it's placeholders
     */
    public abstract void setup();

    /**
     * Use this to start creating a placeholder
     *
     * @param name the name of the placeholder without { }
     * @return a PlaceholderBuilder you can use to create your placeholder
     */
    protected PlaceholderBuilder bind(String name) {
        return new PlaceholderBuilder(name, OptionalDouble.empty());
    }

    /**
     * Create a placeholder using a regular expression
     *
     * @param regex the regular expression
     * @return a RegexPlaceholderBuilder you can use to create your placeholder
     */
    protected RegexPlaceholderBuilder bindRegex(String regex) {
        return new RegexPlaceholderBuilder(regex);
    }

    public class RegexPlaceholderBuilder {
        private final String regex;

        public RegexPlaceholderBuilder(String regex) {
            this.regex = regex;
        }

        /**
         * Finishes creating the placeholder
         * @param function a function that creates a SlotTemplate
         */
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
        private final OptionalDouble requiredUpdateInterval;

        private PlaceholderBuilder(String name, OptionalDouble requiredUpdateInterval) {
            this(Collections.singletonList(name), requiredUpdateInterval);
        }

        public PlaceholderBuilder(List<String> names, OptionalDouble requiredUpdateInterval) {
            this.names = names;
            this.requiredUpdateInterval = requiredUpdateInterval;
        }

        /**
         * Finished creating the placeholder
         * @param function a function that provides the replacement for the variable
         *                 the function can use the TabListContext to get contextual information
         */
        public void to(Function<TabListContext, String> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    requiredUpdateInterval.ifPresent(BungeeTabListPlusAPI::requireTabListUpdateInterval);

                    return new SlotTemplate() {
                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return builder.appendText(function.apply(context));
                        }
                    };
                }
            });
        }

        /**
         * Finished creating the placeholder
         * this function requires a SlotTemplate as replacement thus allowing variables to change skin and ping of a slot
         * @param function a function that provides the replacement for the variable
         *                 the function can use the TabListContext to get contextual information
         */
        public void toTemplate(Function<TabListContext, SlotTemplate> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    requiredUpdateInterval.ifPresent(BungeeTabListPlusAPI::requireTabListUpdateInterval);

                    return new SlotTemplate() {
                        @Override
                        public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                            return function.apply(context).buildSlot(builder, context);
                        }
                    };
                }
            });
        }

        /**
         * creates a placeholder which takes arguments {variable_name:args}
         * arguments are provided as string
         * @return an ArgsPlaceholderBuilder allowing you to create the placeholder
         */
        public ArgsPlaceholderBuilder withArgs() {
            return new ArgsPlaceholderBuilder(names, requiredUpdateInterval);
        }

        /**
         * creates a placeholder which takes arguments {variable_name:args}
         * arguments are provided as SlotTemplate, this is useful if you intend
         * to use the arguments as replacement for the placeholder
         * @return an TemplateArgsPlaceholderBuilder allowing you to create the placeholder
         */
        public TemplateArgsPlaceholderBuilder withTemplateArgs() {
            return new TemplateArgsPlaceholderBuilder(names, requiredUpdateInterval);
        }

        /**
         * adds an alias to the placeholder
         * @param alias the alias without { }
         * @return a PlaceholderBuilder
         */
        public PlaceholderBuilder alias(String alias) {
            ArrayList<String> names = new ArrayList<>();
            names.addAll(this.names);
            names.add(alias);
            return new PlaceholderBuilder(names, requiredUpdateInterval);
        }

        /**
         * set the minimum tablist update interval (in seconds) this variable requires
         *
         * @param interval the interval in seconds
         * @return a PlaceholderBuilder
         */
        public PlaceholderBuilder setRequiredUpdateInterval(double interval) {
            return new PlaceholderBuilder(names, OptionalDouble.of(interval));
        }
    }

    public class ArgsPlaceholderBuilder {
        private final List<String> names;
        private final OptionalDouble requiredUpdateInterval;

        private ArgsPlaceholderBuilder(List<String> names, OptionalDouble requiredUpdateInterval) {
            this.names = names;
            this.requiredUpdateInterval = requiredUpdateInterval;
        }

        /**
         * Registers the variable
         * @param function a function the provides the replacement text
         *                 the placeholder arguments as well as the TabListContext
         *                 are given to the function
         */
        public void to(BiFunction<TabListContext, String, String> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)(?::((?:(?:[^{}]*)\\{(?:[^{}]*)\\})*(?:[^{}]*)))?\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    requiredUpdateInterval.ifPresent(BungeeTabListPlusAPI::requireTabListUpdateInterval);

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

        /**
         * Registers the variable
         * @param function a function the provides the replacement
         *                 the replacement is a SlotTemplate which allows the variable to modify ping and skin
         *                 the placeholder arguments as well as the TabListContext are given to the function
         */
        public void toTemplate(BiFunction<TabListContext, String, SlotTemplate> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)(?::((?:(?:[^{}]*)\\{(?:[^{}]*)\\})*(?:[^{}]*)))?\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    requiredUpdateInterval.ifPresent(BungeeTabListPlusAPI::requireTabListUpdateInterval);

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
        private final OptionalDouble requiredUpdateInterval;

        private TemplateArgsPlaceholderBuilder(List<String> names, OptionalDouble requiredUpdateInterval) {
            this.names = names;
            this.requiredUpdateInterval = requiredUpdateInterval;
        }

        /**
         * Registers the variable
         * @param function a function the provides the replacement
         *                 the replacement is a SlotTemplate which allows the variable to modify ping and skin
         *                 the placeholder arguments as well as the TabListContext are given to the function
         */
        public void to(BiFunction<TabListContext, SlotTemplate, SlotTemplate> function) {
            registry.registerPlaceholder(new Placeholder(String.format("\\{(?:%s)(?::((?:(?:[^{}]*)\\{(?:[^{}]*)\\})*(?:[^{}]*)))?\\}", Joiner.on('|').join(names))) {
                @Override
                public SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher) {
                    requiredUpdateInterval.ifPresent(BungeeTabListPlusAPI::requireTabListUpdateInterval);

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

    public interface PlaceholderRegistry {

        void registerPlaceholder(Placeholder placeholder);
    }
}
