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

import codecrafter47.bungeetablistplus.common.network.TypeAdapter;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

@Getter
@Setter
public class AbstractMessageServerUpdateData extends AbstractMessage {

    private List<Item> items;

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeInt(items.size());
        for (Item item : items) {
            output.writeInt(item.netId);
            output.writeBoolean(item.value == null);
            if (item.value != null) {
                item.typeAdapter.write(output, item.value);
            }
        }

    }

    @Value
    public static class Item {
        private int netId;
        private TypeAdapter<Object> typeAdapter;
        private Object value;
    }
}
