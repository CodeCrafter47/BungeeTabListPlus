/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.data;

import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyCatalogue;
import de.codecrafter47.data.api.TypeToken;
import de.codecrafter47.data.velocity.api.VelocityData;
import de.codecrafter47.taboverlay.Icon;

public class BTLPVelocityDataKeys implements DataKeyCatalogue {
    public static final DataKey<Integer> DATA_KEY_Server_Count = new DataKey<>("btlp:server_count", VelocityData.SCOPE_VELOCITY_PROXY, TypeToken.INTEGER);
    public static final DataKey<Integer> DATA_KEY_Server_Count_Online = new DataKey<>("btlp:server_count_online", VelocityData.SCOPE_VELOCITY_PROXY, TypeToken.INTEGER);

    public static final DataKey<String> ThirdPartyPlaceholderBungee = new DataKey<>("btlp:thirdPartyPlaceholderBungee", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.STRING);

    public static final DataKey<String> ThirdPartyServerPlaceholderBungee = new DataKey<>("btlp:thirdPartyServerPlaceholderBungee", VelocityData.SCOPE_VELOCITY_SERVER, TypeToken.STRING);

    public static final DataKey<Integer> DATA_KEY_GAMEMODE = new DataKey<>("btlp:gamemode", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.INTEGER);
    public static final DataKey<Icon> DATA_KEY_ICON = new DataKey<>("btlp:icon", VelocityData.SCOPE_VELOCITY_PLAYER, BTLPDataTypes.ICON);

    public static final DataKey<String> DATA_KEY_RedisBungee_ServerId = new DataKey<>("btlp:redisbungee:serverId", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.STRING);

    public static final DataKey<String> DATA_KEY_ServerName = new DataKey<>("btlp:serverName", VelocityData.SCOPE_VELOCITY_SERVER, TypeToken.STRING);

    public static final DataKey<Boolean> DATA_KEY_SERVER_ONLINE = new DataKey<>("btlp:server_online", VelocityData.SCOPE_VELOCITY_SERVER, TypeToken.BOOLEAN);

    public static final DataKey<Boolean> DATA_KEY_IS_HIDDEN = new DataKey<>("btlp:is_hidden", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.BOOLEAN);
    public static final DataKey<String> DATA_KEY_CLIENT_VERSION = new DataKey<>("btlp:client_version", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.STRING);
    public static final DataKey<Boolean> DATA_KEY_CLIENT_VERSION_BELOW_1_8 = new DataKey<>("btlp:client_version_below_1_8", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.BOOLEAN);

    public static final DataKey<Boolean> DATA_KEY_IS_HIDDEN_PLAYER_CONFIG = new DataKey<>("btlp:is_hidden_player_config", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.BOOLEAN);
    public static final DataKey<Boolean> DATA_KEY_IS_HIDDEN_PLAYER_COMMAND = new DataKey<>("btlp:is_hidden_player_command", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.BOOLEAN);
    public static final DataKey<Boolean> DATA_KEY_IS_HIDDEN_SERVER_CONFIG = new DataKey<>("btlp:is_hidden_server_config", VelocityData.SCOPE_VELOCITY_SERVER, TypeToken.BOOLEAN);
    public static final DataKey<Boolean> Permission = new DataKey<>("btlp:permission", VelocityData.SCOPE_VELOCITY_PLAYER, TypeToken.BOOLEAN);

    public static DataKey<String> createBungeeThirdPartyVariableDataKey(String name) {
        return ThirdPartyPlaceholderBungee.withParameter(name);
    }

    public static DataKey<String> createBungeeThirdPartyServerVariableDataKey(String name) {
        return ThirdPartyServerPlaceholderBungee.withParameter(name);
    }

    public static DataKey<Boolean> permission(String permission) {
        return Permission.withParameter(permission);
    }
}
