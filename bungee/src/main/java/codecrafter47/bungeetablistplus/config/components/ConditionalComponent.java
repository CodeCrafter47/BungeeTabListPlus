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

package codecrafter47.bungeetablistplus.config.components;

import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.expression.Expression;
import codecrafter47.bungeetablistplus.expression.ExpressionResult;
import codecrafter47.bungeetablistplus.tablist.component.ComponentTablistAccess;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;

public class ConditionalComponent extends Component implements Validate {
    private Expression condition;
    private Component trueReplacement;
    private Component falseReplacement;

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public Component getTrue() {
        return trueReplacement;
    }

    public void setTrue(Component trueReplacement) {
        this.trueReplacement = trueReplacement;
    }

    public Component getFalse() {
        return falseReplacement;
    }

    public void setFalse(Component falseReplacement) {
        this.falseReplacement = falseReplacement;
    }

    @Override
    public boolean hasConstantSize() {
        return trueReplacement.hasConstantSize() && falseReplacement.hasConstantSize() && trueReplacement.getSize() == falseReplacement.getSize();
    }

    @Override
    public int getSize() {
        return trueReplacement.getSize();
    }

    @Override
    public Component.Instance toInstance(Context context) {
        return new Instance(context);
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull(condition, "condition is null");
        Preconditions.checkNotNull(trueReplacement, "true replacement is null");
        Preconditions.checkNotNull(falseReplacement, "false replacement is null");
    }

    public class Instance extends Component.Instance {

        private Component.Instance component;

        protected Instance(Context context) {
            super(context);
        }

        @Override
        public void deactivate() {
            super.deactivate();
            if (component != null) {
                component.deactivate();
            }
        }

        @Override
        public void update1stStep() {
            super.update1stStep();
            if (component != null) {
                component.deactivate();
            }
            if (condition.evaluate(context, ExpressionResult.BOOLEAN)) {
                component = trueReplacement.toInstance(context);
            } else {
                component = falseReplacement.toInstance(context);
            }
            component.activate();
            component.update1stStep();
        }

        @Override
        public void update2ndStep() {
            super.update2ndStep();
            ComponentTablistAccess cta = getTablistAccess();
            if (cta != null) {
                component.setPosition(cta);
                component.update2ndStep();
            }
        }

        @Override
        public int getMinSize() {
            return component.getMinSize();
        }

        @Override
        public int getPreferredSize() {
            return component.getPreferredSize();
        }

        @Override
        public int getMaxSize() {
            return component.getMaxSize();
        }

        @Override
        public boolean isBlockAligned() {
            return component.isBlockAligned();
        }
    }
}
