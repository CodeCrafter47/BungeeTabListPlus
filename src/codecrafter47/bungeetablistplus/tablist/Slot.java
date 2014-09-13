/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;

public class Slot {

    public String text;
    public int ping;
    private String[] textures = null;
    private String skin = null;

    public Slot(String text, int ping) {
        super();
        this.text = text;
        //this.ping = ping;//>1000?999:ping;
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
    }

    public Slot(String text) {
        super();
        this.text = text;
        this.ping = 0;
    }

    public Slot(Slot s) {
        this.ping = s.ping;
        this.skin = s.skin;
        this.text = s.text;
        this.textures = s.textures;
    }

    public String[] getTextures() {
        if (textures != null) {
            return textures;
        }
        if (skin != null) {
            return BungeeTabListPlus.getInstance().getSkinManager().
                    getSkin(skin);
        }
        return null;
    }

    public void setTextures(String[] textures) {
        this.textures = textures;
    }

    public void setSkin(String name) {
        this.skin = name;
    }
}
