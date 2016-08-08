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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.config.Config;
import codecrafter47.bungeetablistplus.config.PlayerSet;
import codecrafter47.bungeetablistplus.config.components.Component;
import codecrafter47.bungeetablistplus.config.components.ListComponent;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.context.PlayerSets;
import codecrafter47.bungeetablistplus.expression.ExpressionResult;
import codecrafter47.bungeetablistplus.player.IPlayerProvider;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.tablist.DefaultCustomTablist;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigTablistProvider extends DefaultCustomTablist {
    public final Config config;
    private final Context context;

    private int headerIndex;
    private int footerIndex;

    private Component.Instance content;

    private Runnable headerUpdater;
    private Runnable footerUpdater;

    private boolean[] marks;

    private PlayerSets playerSets;

    private boolean active = false;

    public ConfigTablistProvider(Config config, Context context) {
        this.config = config;

        // Set custom tab list size
        setSize(config.getSize());
        marks = new boolean[config.getSize()];

        // Create context
        this.context = context;
        this.context.setColumns(getColumns());
        this.context.setPlayerSets(playerSets = new PlayerSets());
        this.context.setTablist(this);
        init();
    }

    private void init() {
        // Header and footer
        if (config.isShowHeaderFooter()) {
            headerIndex = 0;
            footerIndex = 0;
        }

        // Tab overlay
        content = new ListComponent(config.getComponents()).toInstance(context);
    }

    @Override
    public synchronized void onActivated(PlayerTablistHandler handler) {
        super.onActivated(handler);
        content.activate();
        active = true;

        // register a header/ footer update task here
        if (config.isShowHeaderFooter()) {
            headerUpdater = new HeaderUpdater();
            BungeeTabListPlus.getInstance().registerTask(config.getHeaderAnimationUpdateInterval(), headerUpdater);
            footerUpdater = new FooterUpdater();
            BungeeTabListPlus.getInstance().registerTask(config.getFooterAnimationUpdateInterval(), footerUpdater);
        }
    }

    @Override
    public synchronized void onDeactivated(PlayerTablistHandler handler) {
        content.deactivate();
        active = false;
        if (config.isShowHeaderFooter()) {
            BungeeTabListPlus.getInstance().unregisterTask(config.getHeaderAnimationUpdateInterval(), headerUpdater);
            BungeeTabListPlus.getInstance().unregisterTask(config.getFooterAnimationUpdateInterval(), footerUpdater);
        }
        super.onDeactivated(handler);
    }

    @Override
    public synchronized void setSlot(int row, int column, @Nonnull @NonNull Icon icon, @Nonnull @NonNull String text, int ping) {
        super.setSlot(row, column, icon, text, ping);
        marks[row * getColumns() + column] = true;
    }

    public synchronized void update() {
        if (!active) {
            return;
        }

        // PlayerSets
        ImmutableList<? extends IPlayer> all = ImmutableList.copyOf(Iterables.concat(Collections2.transform(BungeeTabListPlus.getInstance().playerProviders, IPlayerProvider::getPlayers)));
        for (Map.Entry<String, PlayerSet> entry : config.getPlayerSets().entrySet()) {
            playerSets.put(entry.getKey(), all.stream().map(p -> ((Player) p)).filter(player -> entry.getValue().getFilter().evaluate(context.derived().setPlayer(player), ExpressionResult.BOOLEAN)).collect(Collectors.toList()));
        }

        // Tab overlay
        Arrays.fill(marks, false);
        content.setPosition(0, 0, getSize());
        content.update1stStep();
        content.update2ndStep();
        for (int i = 0; i < marks.length; i++) {
            boolean mark = marks[i];
            if (!mark) {
                setSlot(i / getColumns(), i % getColumns(), config.getDefaultIcon().evaluate(context), "", config.getDefaultPing());
            }
        }

        // Header & Footer
        if (config.isShowHeaderFooter()) {
            setHeader(config.getHeader().get(headerIndex).evaluate(context));
            setFooter(config.getFooter().get(footerIndex).evaluate(context));
        }
    }

    private class HeaderUpdater implements Runnable {

        @Override
        public void run() {
            synchronized (ConfigTablistProvider.this) {
                if (active) {
                    headerIndex++;
                    if (headerIndex >= config.getHeader().size()) {
                        headerIndex = 0;
                    }
                    setHeader(config.getHeader().get(headerIndex).evaluate(context));
                }
            }
        }
    }

    private class FooterUpdater implements Runnable {

        @Override
        public void run() {
            synchronized (ConfigTablistProvider.this) {
                if (active) {
                    footerIndex++;
                    if (footerIndex >= config.getFooter().size()) {
                        footerIndex = 0;
                    }
                    setFooter(config.getFooter().get(footerIndex).evaluate(context));
                }
            }
        }
    }
}
