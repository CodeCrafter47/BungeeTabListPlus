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

package codecrafter47.bungeetablistplus.api.bungee.tablist;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.bungee.Skin;

/**
 * Represents a the contents of a slot in the tab list.
 * Slot objects are immutable.
 */
public final class Slot {
    private final String text;
    private final int ping;
    private final Skin skin;

    /**
     * Create a Slot with the given text, ping = 0 and a random Alex/ Steve skin
     *
     * @param text the text
     */
    public Slot(String text) {
        this(text, 0);
    }

    /**
     * Create a Slot with the given text and ping, and a random Alex/ Steve skin
     *
     * @param text the text
     * @param ping the ping
     */
    public Slot(String text, int ping) {
        this(text, ping, BungeeTabListPlusAPI.getDefaultSkin());
    }

    /**
     * Create a Slot with the given text, ping and skin
     *
     * @param text the text
     * @param ping the ping
     * @param skin the skin
     */
    public Slot(String text, int ping, Skin skin) {
        this.text = text;
        if (ping < 0) {
            this.ping = -1;
        } else if (ping < 150) {
            this.ping = 0;
        } else if (ping < 300) {
            this.ping = 150;
        } else if (ping < 600) {
            this.ping = 300;
        } else if (ping < 1000) {
            this.ping = 600;
        } else {
            this.ping = 1000;
        }
        this.skin = skin;
    }

    /**
     * The text this slot should be filled with
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * The ping of this slot.
     * This method can return a value different from the one this slot has been constructed with
     * if the new value results in the same ping bar image on the client
     *
     * @return the ping of this slot
     */
    public int getPing() {
        return ping;
    }

    /**
     * The skin this slot should have
     * @return the skin
     */
    public Skin getSkin() {
        return skin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Slot slot = (Slot) o;

        if (ping != slot.ping) return false;
        if (!text.equals(slot.text)) return false;
        return skin.equals(slot.skin);

    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + ping;
        result = 31 * result + skin.hashCode();
        return result;
    }
}
