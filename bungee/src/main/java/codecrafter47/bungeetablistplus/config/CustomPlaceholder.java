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

package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.expression.Expression;
import codecrafter47.bungeetablistplus.expression.ExpressionResult;
import codecrafter47.bungeetablistplus.placeholder.Placeholder;
import codecrafter47.bungeetablistplus.template.TextTemplate;
import codecrafter47.bungeetablistplus.yamlconfig.Subtype;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Subtype(type = CustomPlaceholder.Conditional.class, tag = "!conditional")
@Subtype(type = CustomPlaceholder.Switch.class, tag = "!switch")
@Getter
@Setter
public abstract class CustomPlaceholder {

    private int parameters = 0;

    protected String replaceParameters(String template, String[] args) {
        for (int i = 0; i < parameters; i++) {
            String replacement;
            if (i < args.length) {
                replacement = args[i];
                if (i == parameters - 1) {
                    for (int j = i + 1; j < args.length; j++) {
                        replacement += " " + args[j];
                    }
                }
            } else {
                replacement = "";
            }
            template = template.replace("%" + i, replacement);
        }
        return template;
    }

    public abstract Placeholder instantiate(String[] args);

    public static class Conditional extends CustomPlaceholder implements Validate {
        private String condition;
        private String trueReplacement;
        private String falseReplacement;

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getTrue() {
            return trueReplacement;
        }

        public void setTrue(String trueReplacement) {
            this.trueReplacement = trueReplacement;
        }

        public String getFalse() {
            return falseReplacement;
        }

        public void setFalse(String falseReplacement) {
            this.falseReplacement = falseReplacement;
        }

        @Override
        public Placeholder instantiate(String[] args) {
            Expression condition = new Expression(replaceParameters(this.condition, args));
            TextTemplate trueReplacement = new TextTemplate(replaceParameters(this.trueReplacement, args));
            TextTemplate falseReplacement = new TextTemplate(replaceParameters(this.falseReplacement, args));
            return new Instance(condition, trueReplacement, falseReplacement);
        }

        @Override
        public void validate() {
            Preconditions.checkNotNull(condition, "condition is null");
            Preconditions.checkNotNull(trueReplacement, "true replacement is null");
            Preconditions.checkNotNull(falseReplacement, "false replacement is null");
        }

        @AllArgsConstructor
        private static class Instance extends Placeholder {
            private final Expression condition;
            private final TextTemplate trueReplacement;
            private final TextTemplate falseReplacement;

            @Override
            public String evaluate(Context context) {
                return condition.evaluate(context, ExpressionResult.BOOLEAN) ? trueReplacement.evaluate(context) : falseReplacement.evaluate(context);
            }
        }
    }

    @Getter
    @Setter
    public static class Switch extends CustomPlaceholder implements Validate {
        private String expression;
        private Map<String, String> replacements;
        private String defaultReplacement = "";

        @Override
        public Placeholder instantiate(String[] args) {
            Expression expression = new Expression(replaceParameters(this.expression, args));
            Map<String, TextTemplate> replacements = new HashMap<>(Maps.transformValues(this.replacements, template -> new TextTemplate(replaceParameters(template, args))));
            TextTemplate defaultReplacement = new TextTemplate(replaceParameters(this.defaultReplacement, args));
            return new Instance(expression, replacements, defaultReplacement);
        }

        @Override
        public void validate() {
            Preconditions.checkNotNull(expression, "expression is null");
            Preconditions.checkNotNull(replacements, "replacements is null");
        }

        @AllArgsConstructor
        private static class Instance extends Placeholder {
            private final Expression expression;
            private final Map<String, TextTemplate> replacements;
            private final TextTemplate defaultReplacement;

            @Override
            public String evaluate(Context context) {
                TextTemplate replacement = replacements.get(expression.evaluate(context, ExpressionResult.STRING));
                if (replacement != null) {
                    return replacement.evaluate(context);
                } else if (defaultReplacement != null) {
                    return defaultReplacement.evaluate(context);
                } else {
                    return "";
                }
            }
        }
    }
}
