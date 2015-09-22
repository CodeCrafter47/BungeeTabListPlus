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

import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.skin.Skin;

public class SlotBuilder {
    private StringBuilder textBuilder = new StringBuilder();
    private int ping = 0;
    private Skin skin = SkinManager.defaultSkin;

    public SlotBuilder appendText(String text) {
        textBuilder.append(text);
        return this;
    }

    public SlotBuilder setPing(int ping) {
        this.ping = ping;
        return this;
    }

    public SlotBuilder setSkin(Skin skin) {
        this.skin = skin;
        return this;
    }

    public Slot build() {
        return new Slot(textBuilder.toString(), ping, skin);
    }
}
