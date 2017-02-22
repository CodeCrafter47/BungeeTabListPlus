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

package codecrafter47.bungeetablistplus.tablistproviders;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.config.FixedColumnsConfig;
import codecrafter47.bungeetablistplus.config.components.Component;
import codecrafter47.bungeetablistplus.config.components.ListComponent;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.tablist.component.ComponentTablistAccess;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.template.PingTemplate;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class FixedColumnsConfigTablistProvider extends ConfigTablistProvider<FixedColumnsConfig> {

    private Component.Instance content;

    private final int minSize;
    private final int maxSize;

    private boolean[] marks;

    public FixedColumnsConfigTablistProvider(FixedColumnsConfig config, Context context) {
        super(config, context);

        // Set custom tab list size
        if (config.getColumns() == 1) minSize = 0;
        else if (config.getColumns() == 2) minSize = 22;
        else if (config.getColumns() == 3) minSize = 42;
        else minSize = 64;
        maxSize = config.getColumns() * 20;
        setSize(minSize);
        marks = new boolean[maxSize];

        // update context
        context.put(Context.KEY_COLUMNS, getColumns());
        context.put(Context.KEY_DEFAULT_ICON, config.getDefaultIcon());
        context.put(Context.KEY_DEFAULT_PING, PingTemplate.constValue(config.getDefaultPing()));

        // Tab overlay
        content = new ListComponent(config.getComponents()).toInstance(context);
    }

    @Override
    public synchronized void onActivated(PlayerTablistHandler handler) {
        super.onActivated(handler);
        content.activate();
    }

    @Override
    public synchronized void onDeactivated(PlayerTablistHandler handler) {
        content.deactivate();
        super.onDeactivated(handler);
    }

    @Override
    public synchronized void setSlot(int row, int column, @Nonnull @NonNull Icon icon, @Nonnull @NonNull String text, int ping) {
        super.setSlot(row, column, icon, text, ping);
        marks[row * getColumns() + column] = true;
    }

    @Override
    public synchronized void update() {
        if (!active) {
            return;
        }

        super.update();

        // Tab overlay
        Arrays.fill(marks, false);
        content.update1stStep();
        int size = content.getPreferredSize();
        size = ((size + getColumns() - 1) / getColumns()) * getColumns();
        if (size < minSize)
            size = minSize;
        if (size > maxSize)
            size = maxSize;
        setSize(size);
        content.setPosition(ComponentTablistAccess.of(this));
        content.update2ndStep();
        for (int i = 0; i < size; i++) {
            boolean mark = marks[i];
            if (!mark) {
                setSlot(i / getColumns(), i % getColumns(), config.getDefaultIcon().evaluate(context), "", config.getDefaultPing());
            }
        }
    }
}
