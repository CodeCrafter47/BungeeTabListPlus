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

package codecrafter47.bungeetablistplus.bukkitbridge;

import codecrafter47.bungeetablistplus.common.Configuration;

import java.util.Map;

public class MainConfig extends Configuration {

    public boolean automaticallySendBugReports = true;

    @Override
    protected void read(Map<Object, Object> map) {
        automaticallySendBugReports = (boolean) map.getOrDefault("automaticallySendBugReports", true);
    }

    @Override
    protected void write() {
        writeComments("If this is set to true and the plugin encounters an issue a bugreport is sent automatically",
                "Bug reports do not contain any sensitive or identifying information",
                "Bug reports contain the plugin name, plugin version and the error message that also appears in the server log");
        write("automaticallySendBugReports", automaticallySendBugReports);
    }
}
