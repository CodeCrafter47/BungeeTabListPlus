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
package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.Variable;
import codecrafter47.bungeetablistplus.layout.TabListContext;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeVariable implements Variable {

    private final SimpleDateFormat format;

    public TimeVariable(String format) {
        this.format = new SimpleDateFormat(format);
        this.format.setTimeZone(BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().getTimeZone());
    }

    @Override
    public String getReplacement(ProxiedPlayer viewer, String args, TabListContext context) {
        if (args == null) {
            return format.format(Calendar.getInstance().getTime());
        } else {
            SimpleDateFormat format2 = new SimpleDateFormat(args);
            return format2.format(Calendar.getInstance().getTime());
        }
    }

}
