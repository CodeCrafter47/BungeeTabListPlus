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
import codecrafter47.bungeetablistplus.expression.ExpressionResult;
import codecrafter47.bungeetablistplus.template.TextTemplate;
import codecrafter47.bungeetablistplus.yamlconfig.Subtype;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Subtype(type = CustomPlaceholder.Conditional.class, tag = "!conditional")
@Subtype(type = CustomPlaceholder.Switch.class, tag = "!switch")
public abstract class CustomPlaceholder {

    public abstract String evaluate(Context context);

    public static class Conditional extends CustomPlaceholder {
        private Expression condition;
        private TextTemplate trueReplacement;
        private TextTemplate falseReplacement;

        public Expression getCondition() {
            return condition;
        }

        public void setCondition(Expression condition) {
            this.condition = condition;
        }

        public TextTemplate getTrue() {
            return trueReplacement;
        }

        public void setTrue(TextTemplate trueReplacement) {
            this.trueReplacement = trueReplacement;
        }

        public TextTemplate getFalse() {
            return falseReplacement;
        }

        public void setFalse(TextTemplate falseReplacement) {
            this.falseReplacement = falseReplacement;
        }

        @Override
        public String evaluate(Context context) {
            return condition.evaluate(context, ExpressionResult.BOOLEAN) ? trueReplacement.evaluate(context) : falseReplacement.evaluate(context);
        }
    }

    @Getter
    @Setter
    public static class Switch extends CustomPlaceholder {
        private Expression expression;
        private Map<String, TextTemplate> replacements;
        private TextTemplate defaultReplacement;

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
