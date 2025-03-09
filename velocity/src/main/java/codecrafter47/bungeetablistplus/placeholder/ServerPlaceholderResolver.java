/*
 *     Copyright (C) 2025 proferabg
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

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.cache.Cache;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.TypeToken;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.taboverlay.config.placeholder.*;
import de.codecrafter47.taboverlay.config.template.TemplateCreationContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerPlaceholderResolver extends AbstractDataHolderPlaceholderResolver<DataHolder> {

    private final Map<String, DataKey<String>> bridgeCustomPlaceholderServerDataKeys = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, DataKey<String>> customPlaceholderServerDataKeys = Collections.synchronizedMap(new HashMap<>());

    private final Cache cache;

    public ServerPlaceholderResolver(Cache cache) {
        this.cache = cache;

        setDefaultPlaceholder(builder -> builder.acquireData(new DataHolderPlaceholderDataProviderSupplier<>(TypeToken.STRING, BTLPVelocityDataKeys.DATA_KEY_ServerName, (c, d) -> d), TypeToken.STRING));
        addPlaceholder("tps", create(MinecraftData.TPS));
        addPlaceholder("name", create(BTLPVelocityDataKeys.DATA_KEY_ServerName));
        addPlaceholder("online", create(BTLPVelocityDataKeys.DATA_KEY_SERVER_ONLINE));
    }

    @Nonnull
    @Override
    public PlaceholderBuilder<?, ?> resolve(PlaceholderBuilder<DataHolder, ?> builder, List<PlaceholderArg> args, TemplateCreationContext tcc) throws UnknownPlaceholderException, PlaceholderException {
        try {
            return super.resolve(builder, args, tcc);
        } catch (UnknownPlaceholderException e) {
            if (!args.isEmpty()) {
                String id = args.get(0).getText().toLowerCase();
                PlaceholderBuilder<?, ?> result = null;
                if (customPlaceholderServerDataKeys.containsKey(id)) {
                    DataKey<String> dataKey = customPlaceholderServerDataKeys.get(id);
                    result = builder.acquireData(new DataHolderPlaceholderDataProviderSupplier<>(TypeToken.STRING, dataKey, (server, replacement) -> replacement), TypeToken.STRING);
                } else if (bridgeCustomPlaceholderServerDataKeys.containsKey(id)) {
                    DataKey<String> dataKey = bridgeCustomPlaceholderServerDataKeys.get(id);
                    result = builder.acquireData(new DataHolderPlaceholderDataProviderSupplier<>(TypeToken.STRING, dataKey, (player, replacement) -> replacement), TypeToken.STRING);
                } else if (cache.getCustomServerPlaceholdersBridge().contains(id)) {
                    // prevent warnings because bridge data has not been synced yet
                    result = builder.acquireData(new DataHolderPlaceholderDataProviderSupplier<>(TypeToken.STRING, BTLPDataKeys.createThirdPartyServerVariableDataKey(id), (player, replacement) -> replacement), TypeToken.STRING);
                }
                if (result != null) {
                    args.remove(0);
                    return result;
                }
            }
            throw e;
        }
    }

    public void addCustomPlaceholderServerDataKey(String id, DataKey<String> dataKey) {
        customPlaceholderServerDataKeys.put(id.toLowerCase(), dataKey);
    }

    public void addBridgeCustomPlaceholderServerDataKey(String id, DataKey<String> dataKey) {
        bridgeCustomPlaceholderServerDataKeys.put(id.toLowerCase(), dataKey);
    }
}
