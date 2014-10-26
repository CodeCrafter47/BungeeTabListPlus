/*
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
package codecrafter47.bungeetablistplus.bukkitbridge.api;

import org.bukkit.plugin.Plugin;

/**
 *
 * @author Florian Stober
 */
public interface BungeeTabListPlusBukkitBridge {

    void registerInformationProvider(Plugin pl, GeneralInformationProvider ip);

    void registerPlayerInformationProvider(Plugin pl,
            PlayerInformationProvider ip);

}
