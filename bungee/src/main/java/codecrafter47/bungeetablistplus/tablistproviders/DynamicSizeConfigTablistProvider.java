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
import codecrafter47.bungeetablistplus.config.DynamicSizeConfig;
import codecrafter47.bungeetablistplus.config.components.Component;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.tablist.component.ComponentTablistAccess;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.template.IconTemplate;
import codecrafter47.bungeetablistplus.template.PingTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.min;

public class DynamicSizeConfigTablistProvider extends ConfigTablistProvider<DynamicSizeConfig> {

    private List<Component.Instance> activeComponents = new ArrayList<>();

    public DynamicSizeConfigTablistProvider(DynamicSizeConfig config, Context context) {
        super(config, context);
        this.context.put(Context.KEY_COLUMNS, 1);
    }

    @Override
    public synchronized void onActivated(PlayerTablistHandler handler) {
        super.onActivated(handler);
        handler.runInEventLoop(() -> {
            synchronized (DynamicSizeConfigTablistProvider.this) {
                handler.setResizePolicy(PlayerTablistHandler.ResizePolicy.DYNAMIC);
            }
        });
    }

    @Override
    public synchronized void update() {
        if (!active) {
            return;
        }

        super.update();

        // get players
        List<Player> players = context.get(Context.KEY_PLAYER_SETS).get(config.getPlayerSet());
        if (players == null) {
            players = Collections.emptyList();
            BungeeTabListPlus.getInstance().getLogger().warning("Missing player set " + config.getPlayerSet());
        }
        config.getPlayerOrder().sort(context, players);

        // deactivate old components
        activeComponents.forEach(Component.Instance::deactivate);
        activeComponents.clear();

        int size = min(80, players.size() * config.getPlayerComponent().getSize());
        setSize(1, size);

        // create & update components
        ComponentTablistAccess cta = ComponentTablistAccess.of(this);
        int pos = 0;
        int i;
        for (i = 0; pos < size; i++) {
            Player player = players.get(i);
            Component.Instance component = config.getPlayerComponent().toInstance(context.derived().put(Context.KEY_PLAYER, player).put(Context.KEY_DEFAULT_ICON, IconTemplate.PLAYER_ICON).put(Context.KEY_DEFAULT_PING, PingTemplate.PLAYER_PING));
            component.activate();
            component.update1stStep();
            component.setPosition(ComponentTablistAccess.createChild(cta, config.getPlayerComponent().getSize(), pos));
            component.update2ndStep();
            activeComponents.add(component);
            pos += config.getPlayerComponent().getSize();
        }
    }

    @Override
    public synchronized void onDeactivated(PlayerTablistHandler handler) {
        activeComponents.forEach(Component.Instance::deactivate);
        activeComponents.clear();
        super.onDeactivated(handler);
    }
}
