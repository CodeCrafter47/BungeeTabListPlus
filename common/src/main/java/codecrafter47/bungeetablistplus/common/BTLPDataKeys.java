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

package codecrafter47.bungeetablistplus.common;


import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyCatalogue;
import de.codecrafter47.data.api.TypeToken;
import de.codecrafter47.data.minecraft.api.MinecraftData;

import java.util.List;

public final class BTLPDataKeys implements DataKeyCatalogue {

    public static final DataKey<String> PAPIPlaceholder = new DataKey<>("btlp:placeholderAPI", MinecraftData.SCOPE_PLAYER, TypeToken.STRING);
    public static final DataKey<List<String>> PAPI_REGISTERED_PLACEHOLDER_PLUGINS = new DataKey<>("btlp:placeholderAPI:plugins", MinecraftData.SCOPE_SERVER, TypeToken.STRING_LIST);

    public static DataKey<String> createPlaceholderAPIDataKey(String placeholder) {
        return PAPIPlaceholder.withParameter(placeholder);
    }

    public static final DataKey<String> ThirdPartyPlaceholder = new DataKey<>("btlp:thirdPartyPlaceholder", MinecraftData.SCOPE_PLAYER, TypeToken.STRING);

    public static DataKey<String> createThirdPartyVariableDataKey(String name) {
        return ThirdPartyPlaceholder.withParameter(name);
    }

    public static final DataKey<String> ThirdPartyServerPlaceholder = new DataKey<>("btlp:thirdPartyServerPlaceholder", MinecraftData.SCOPE_SERVER, TypeToken.STRING);

    public static DataKey<String> createThirdPartyServerVariableDataKey(String name) {
        return ThirdPartyServerPlaceholder.withParameter(name);
    }

    public final static DataKey<List<String>> REGISTERED_THIRD_PARTY_VARIABLES = new DataKey<>("thirdparty-variables", MinecraftData.SCOPE_SERVER, TypeToken.STRING_LIST);

    public final static DataKey<List<String>> REGISTERED_THIRD_PARTY_SERVER_VARIABLES = new DataKey<>("thirdparty-server-variables", MinecraftData.SCOPE_SERVER, TypeToken.STRING_LIST);

    public final static DataKey<Boolean> PLACEHOLDERAPI_PRESENT = new DataKey<>("placeholderapi-present", MinecraftData.SCOPE_SERVER, TypeToken.BOOLEAN);
}
