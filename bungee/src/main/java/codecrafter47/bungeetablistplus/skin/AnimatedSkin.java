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

package codecrafter47.bungeetablistplus.skin;

import codecrafter47.bungeetablistplus.api.bungee.Skin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AnimatedSkin implements Skin {
    private final List<Skin> skins;
    private final int interval;

    public AnimatedSkin(Skin... skins) {
        this(Arrays.asList(skins), 1000);
    }

    public AnimatedSkin(List<Skin> skins, int interval) {
        this.skins = skins;
        this.interval = interval;
    }

    private Skin getSkin() {
        return skins.get((int) ((System.currentTimeMillis() / interval) % skins.size()));
    }

    @Override
    public String[] toProperty() {
        return getSkin().toProperty();
    }

    @Override
    public UUID getOwner() {
        return getSkin().getOwner();
    }
}
