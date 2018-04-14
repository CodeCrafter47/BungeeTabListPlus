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

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.tablistproviders.TablistProvider;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import static java.lang.Integer.min;

public class DefaultCustomTablist extends AbstractCustomTablist implements TablistProvider {
    private ReferenceSet<PlayerTablistHandler> handlers = new ReferenceOpenHashSet<>();

    @Override
    public synchronized void onActivated(PlayerTablistHandler handler) {
        Preconditions.checkState(!handlers.contains(handler));
        handlers.add(handler);
        handler.runInEventLoop(() -> {
            synchronized (DefaultCustomTablist.this) {
                handler.setResizePolicy(PlayerTablistHandler.ResizePolicy.DEFAULT);
                int size = min(80, getSize());
                handler.setSize(size);
                handler.setPassThrough(false);

                updateAllSlots(handler);

                handler.setHeaderFooter(getHeader(), getFooter());
            }
        });
    }

    private void updateAllSlots(PlayerTablistHandler handler) {
        for (int column = 0; column < getColumns(); column++) {
            for (int row = 0; row < getRows(); row++) {
                Icon icon = getIcon(row, column);
                String text = getText(row, column);
                int ping = getPing(row, column);

                handler.setSlot(row, column, icon, text, ping);
            }
        }
    }

    @Override
    public synchronized void onDeactivated(PlayerTablistHandler handler) {
        if (!handlers.remove(handler)) {
            throw new IllegalStateException();
        }
    }

    @Override
    protected void onSizeChanged() {
        int size = getSize();
        for (PlayerTablistHandler handler : handlers) {
            handler.runInEventLoop(() -> {
                handler.setSize(size);

                synchronized (DefaultCustomTablist.this) {
                    updateAllSlots(handler);
                }
            });
        }
    }

    @Override
    protected void onSlotChanged(int row, int column) {
        Icon icon = getIcon(row, column);
        String text = getText(row, column);
        int ping = getPing(row, column);
        for (PlayerTablistHandler handler : handlers) {
            handler.runInEventLoop(() -> handler.setSlot(row, column, icon, text, ping));
        }
    }

    @Override
    protected void onHeaderOrFooterChanged() {
        String header = getHeader();
        String footer = getFooter();
        for (PlayerTablistHandler handler : handlers) {
            handler.runInEventLoop(() -> handler.setHeaderFooter(header, footer));
        }
    }
}
