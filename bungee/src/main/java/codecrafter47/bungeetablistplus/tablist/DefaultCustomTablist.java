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

import codecrafter47.bungeetablistplus.util.IconUtil;
import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.TabOverlayProvider;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.handler.*;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import static java.lang.Integer.min;

public class DefaultCustomTablist extends AbstractCustomTablist {
    private final ReferenceSet<TabOverlayProviderImpl> handlers = new ReferenceOpenHashSet<>();

    public DefaultCustomTablist() {
    }

    public DefaultCustomTablist(int size) {
        super(size);
    }

    @Override
    protected void onSizeChanged() {
        for (TabOverlayProviderImpl handler : handlers) {
            handler.onSizeChanged();
        }
    }

    @Override
    protected void onSlotChanged(int index) {
        Icon icon = getIcon(index);
        String text = getText(index);
        int ping = getPing(index);
        for (TabOverlayProviderImpl handler : handlers) {
            handler.onSlotChanged(index, icon, text, ping);
        }
    }

    @Override
    protected void onHeaderOrFooterChanged() {
        String header = getHeader();
        String footer = getFooter();
        for (TabOverlayProviderImpl handler : handlers) {
            handler.setHeaderFooter(header, footer);
        }
    }

    public void addToPlayer(TabView tabView) {
        TabOverlayProviderImpl provider = new TabOverlayProviderImpl();
        tabView.getTabOverlayProviders().addProvider(provider);
    }

    public class TabOverlayProviderImpl extends TabOverlayProvider {

        private SimpleTabOverlay tabOverlay;
        private HeaderAndFooterHandle headerAndFooterHandle;

        TabOverlayProviderImpl() {
            super("custom-tab-overlay", 10001);
        }

        @Override
        protected void attach(TabView tabView) {
            handlers.add(this);
        }

        @Override
        protected void detach(TabView tabView) {
            handlers.remove(this);
        }

        @Override
        protected void activate(TabView tabView, TabOverlayHandler handler) {
            synchronized (DefaultCustomTablist.this) {
                tabOverlay = handler.enterContentOperationMode(ContentOperationMode.SIMPLE);
                headerAndFooterHandle = handler.enterHeaderAndFooterOperationMode(HeaderAndFooterOperationMode.CUSTOM);
                int size = min(80, getSize());
                tabOverlay.setSize(size);
                updateAllSlots();
                headerAndFooterHandle.setHeaderFooter(getHeader(), getFooter());
            }
        }

        @Override
        protected void deactivate(TabView tabView) {

        }

        @Override
        protected boolean shouldActivate(TabView tabView) {
            return true;
        }

        private void updateAllSlots() {
            for (int column = 0; column < getColumns(); column++) {
                for (int row = 0; row < getRows(); row++) {
                    Icon icon = IconUtil.convert(getIcon(row, column));
                    String text = getText(row, column);
                    int ping = getPing(row, column);

                    tabOverlay.setSlot(index(row, column), icon, text, ping);
                }
            }
        }

        void onSizeChanged() {
            synchronized (DefaultCustomTablist.this) {
                int size = getSize();
                tabOverlay.setSize(size);
                updateAllSlots();
            }
        }

        void onSlotChanged(int index, Icon icon, String text, int ping) {
            tabOverlay.setSlot(index, icon, text, ping);
        }

        void setHeaderFooter(String header, String footer) {
            headerAndFooterHandle.setHeaderFooter(header, footer);
        }
    }
}
