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

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;

import java.text.SimpleDateFormat;

public class TimePlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        SimpleDateFormat format_time = getFormat("HH:mm:ss");
        bind("time").setRequiredUpdateInterval(1.0).withArgs().to((context, args) -> {
            SimpleDateFormat format = args != null && !args.isEmpty() ? new SimpleDateFormat(args) : format_time;
            return format.format(System.currentTimeMillis());
        });

        addTimePlaceholder("date", "dd.MM.yyyy", 300);
        addTimePlaceholder("second", "ss", 1);
        addTimePlaceholder("seconds", "ss", 1);
        addTimePlaceholder("sec", "ss", 1);
        addTimePlaceholder("minute", "mm", 60);
        addTimePlaceholder("minutes", "mm", 60);
        addTimePlaceholder("min", "mm", 60);
        addTimePlaceholder("hour", "HH", 300);
        addTimePlaceholder("hours", "HH", 300);
        addTimePlaceholder("day", "dd", 300);
        addTimePlaceholder("days", "dd", 300);
        addTimePlaceholder("month", "MM", 300);
        addTimePlaceholder("months", "MM", 300);
        addTimePlaceholder("year", "yyyy", 300);
        addTimePlaceholder("years", "yyyy", 300);
    }

    private void addTimePlaceholder(String name, String pattern, double requiredUpdateInterval) {
        SimpleDateFormat format = getFormat(pattern);
        bind(name).setRequiredUpdateInterval(requiredUpdateInterval).to(context -> format.format(System.currentTimeMillis()));
    }

    private SimpleDateFormat getFormat(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().getTimeZone());
        return format;
    }
}
