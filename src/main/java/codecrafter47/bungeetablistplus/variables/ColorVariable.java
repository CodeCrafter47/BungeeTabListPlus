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
package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.Variable;
import net.md_5.bungee.api.ChatColor;

/**
 *
 * @author Florian Stober
 */
public class ColorVariable implements Variable {

    private final ChatColor colors[] = new ChatColor[]{ChatColor.BLUE,
        ChatColor.AQUA, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GOLD,
        ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE};

    @Override
    public String getReplacement(String args) {
        long time = System.currentTimeMillis() / 1000;
        ChatColor all[];
        if (args != null) {
            // parse color args
            String array[] = args.split(",");
            all = new ChatColor[array.length];
            for (int i = 0; i < array.length; i++) {
                String color = array[i];
                try {
                    all[i] = ChatColor.valueOf(color);
                } catch (Exception ex) {
                    BungeeTabListPlus.getInstance().getLogger().warning(
                            color + " is no valid color");
                    all[i] = ChatColor.BLACK;
                }
            }
        } else {
            all = colors;
        }
        int index = (int) (time % all.length);
        return all[index].toString();
    }

}
