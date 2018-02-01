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

package codecrafter47.bungeetablistplus.spongebridge.placeholderapi;

import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import de.codecrafter47.data.sponge.AbstractSpongeDataAccess;
import me.rojo8399.placeholderapi.PlaceholderService;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;


public class PlaceholderAPIDataAccess extends AbstractSpongeDataAccess<Player> {

    public PlaceholderAPIDataAccess(Logger logger) {
        super(logger);

        addProvider(BTLPDataKeys.PAPIPlaceholder, (player, key) -> {
            try {
                return Sponge.getServiceManager().getRegistration(PlaceholderService.class).map(registration -> {
                    PlaceholderService service = registration.getProvider();
                    Text text = service.replaceSourcePlaceholders(key.getParameter(), player);
                    return TextSerializers.FORMATTING_CODE.serialize(text);
                }).orElse(null);
            } catch (Throwable th) {
                logger.warn("Failed to query value for placeholder \"" + key.getParameter() + "\" from PlaceholderAPI", th);
                return null;
            }
        });
    }
}
