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

package codecrafter47.bungeetablistplus.api.bungee;

import java.util.UUID;

/**
 * Represents a skin of a player
 * <p>
 * You can obtain a skin using {@link BungeeTabListPlusAPI#getSkinForPlayer(String)}
 */
public interface Skin {

    /**
     * The property associated with the skin.
     * See http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape
     * This is only the "textures" property.
     * <p>
     * If this returns null {@link Skin#getOwner()} must also return null. In that case this is a
     * random Alex/ Steve skin.
     *
     * @return the properties associated with this skin
     * can be null
     */
    String[] toProperty();

    /**
     * The UUID of the player who's skin is represented by this object
     * <p>
     * If this returns null {@link Skin#toProperty()} must also return null. In that case this is a
     * random Alex/ Steve skin.
     *
     * @return the uuid or null
     */
    UUID getOwner();
}
