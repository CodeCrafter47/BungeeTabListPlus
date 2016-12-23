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

package codecrafter47.bungeetablistplus.spongebridge.messages;

import codecrafter47.bungeetablistplus.common.network.DataStreamUtils;
import codecrafter47.bungeetablistplus.spongebridge.SpongePlugin;
import de.codecrafter47.data.api.DataKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class AbstractMessageProxyRequestData extends AbstractMessage {

    public static Consumer<String> missingDataKeyLogger = null;

    private List<Item> items;

    @Override
    public void readFrom(DataInput input) throws IOException {
        int size = input.readInt();
        items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            DataKey<?> key = DataStreamUtils.readDataKey(input, SpongePlugin.keyRegistry, missingDataKeyLogger);
            int netId = input.readInt();

            if (key != null) {
                items.add(new Item(key, netId));
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class Item {
        private final DataKey<?> key;
        private final int netId;
    }
}
