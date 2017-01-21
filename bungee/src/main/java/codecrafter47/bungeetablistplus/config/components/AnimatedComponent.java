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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.tablist.component.ComponentTablistAccess;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AnimatedComponent extends Component implements Validate {

    private List<Component> components;
    private float interval;

    public void setComponents(List<Component> components) {
        Preconditions.checkArgument(components.stream().allMatch(Component::hasConstantSize)
                && components.stream().mapToInt(Component::getSize).distinct().count() == 1, "All elements of an animation are required to have the same size.");
        this.components = components;
    }

    public void setInterval(float interval) {
        Preconditions.checkArgument(interval >= 0.01, "Interval needs to be at least 0.01.");
        this.interval = interval;
    }

    @Override
    public boolean hasConstantSize() {
        return true;
    }

    @Override
    public int getSize() {
        return components.get(0).getSize();
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context, components, interval, getSize());
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull(components, "components is null");
        Preconditions.checkArgument(!components.isEmpty(), "components is empty");
        Preconditions.checkArgument(interval > 0, "interval is negative", interval);
    }

    public static class Instance extends Component.Instance implements Runnable {

        private List<Component.Instance> frames;
        private int activeComponent = -1;
        private int interval;
        private final int size;

        protected Instance(Context context, List<Component> components, float interval, int size) {
            super(context);
            frames = new ArrayList<>(components.size());
            this.size = size;
            for (Component component : components) {
                frames.add(component.toInstance(context));
            }
            this.interval = (int) (interval * 1000);
        }

        @Override
        public void activate() {
            super.activate();
            BungeeTabListPlus.getInstance().registerTask(interval / 1000f, this);
        }

        @Override
        public void deactivate() {
            super.deactivate();
            synchronized (context.get(Context.KEY_TAB_LIST)) {
                BungeeTabListPlus.getInstance().unregisterTask(interval / 1000f, this);
                activeComponent = -1;
            }
        }

        @Override
        public void update1stStep() {
            super.update1stStep();
        }

        @Override
        public void update2ndStep() {
            super.update2ndStep();
            synchronized (context.get(Context.KEY_TAB_LIST)) {
                ComponentTablistAccess cta = getTablistAccess();
                if (cta != null) {
                    if (activeComponent == -1) {
                        activeComponent = (int) ((System.currentTimeMillis() / interval) % frames.size());
                        Component.Instance component = frames.get(activeComponent);
                        component.activate();
                        component.update1stStep();
                        component.setPosition(cta);
                        component.update2ndStep();
                    } else {
                        Component.Instance component = frames.get(activeComponent);
                        component.update1stStep();
                        component.setPosition(cta);
                        component.update2ndStep();
                    }
                }
            }
        }

        @Override
        public void run() {
            if (activeComponent != -1) {
                synchronized (context.get(Context.KEY_TAB_LIST)) {
                    ComponentTablistAccess cta = getTablistAccess();
                    if (cta != null) {
                        if (activeComponent != -1) {
                            frames.get(activeComponent).deactivate();
                            activeComponent = (int) ((System.currentTimeMillis() / interval) % frames.size());
                            Component.Instance component = frames.get(activeComponent);
                            component.activate();
                            component.update1stStep();
                            component.setPosition(cta);
                            component.update2ndStep();
                        }
                    }
                }
            }
        }

        @Override
        public int getMinSize() {
            return size;
        }

        @Override
        public int getPreferredSize() {
            return size;
        }

        @Override
        public int getMaxSize() {
            return size;
        }

        @Override
        public boolean isBlockAligned() {
            return false;
        }
    }
}
