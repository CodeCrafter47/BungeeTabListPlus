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

package codecrafter47.bungeetablistplus.data;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyCatalogue;
import de.codecrafter47.data.api.TypeToken;
import de.codecrafter47.data.bungee.api.BungeeData;

public class BTLPBungeeDataKeys implements DataKeyCatalogue {

    public static final DataKey<String> ThirdPartyPlaceholderBungee = new DataKey<>("btlp:thirdPartyPlaceholderBungee", BungeeData.SCOPE_BUNGEE_PLAYER, TypeToken.STRING);

    public static final DataKey<String> ThirdPartyServerPlaceholderBungee = new DataKey<>("btlp:thirdPartyServerPlaceholderBungee", BungeeData.SCOPE_BUNGEE_PLAYER, TypeToken.STRING);

    public static final DataKey<Integer> DATA_KEY_GAMEMODE = new DataKey<>("btlp:gamemode", BungeeData.SCOPE_BUNGEE_PLAYER, TypeToken.INTEGER);
    public static final DataKey<Icon> DATA_KEY_ICON = new DataKey<>("btlp:icon", BungeeData.SCOPE_BUNGEE_PLAYER, BTLPDataTypes.ICON);

    public static final DataKey<String> DATA_KEY_RedisBungee_ServerId = new DataKey<>("btlp:redisbungee:serverId", BungeeData.SCOPE_BUNGEE_PLAYER, TypeToken.STRING);

    public static DataKey<String> createBungeeThirdPartyVariableDataKey(String name) {
        return ThirdPartyPlaceholderBungee.withParameter(name);
    }

    public static DataKey<String> createBungeeThirdPartyServerVariableDataKey(String name) {
        return ThirdPartyServerPlaceholderBungee.withParameter(name);
    }
}
