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

package codecrafter47.bungeetablistplus.common.network;

import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyRegistry;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

public class DataStreamUtils {

    public static void writeUUID(DataOutput output, UUID uuid) throws IOException {
        output.writeLong(uuid.getLeastSignificantBits());
        output.writeLong(uuid.getMostSignificantBits());
    }

    public static UUID readUUID(DataInput input) throws IOException {
        long lsb = input.readLong();
        long msb = input.readLong();
        return new UUID(msb, lsb);
    }

    public static void writeDataKey(DataOutput output, DataKey<?> key) throws IOException {
        output.writeUTF(key.getId());
        String parameter = key.getParameter();
        output.writeBoolean(parameter != null);
        if (parameter != null) {
            output.writeUTF(parameter);
        }
    }

    public static DataKey<?> readDataKey(DataInput input, DataKeyRegistry registry) throws IOException {
        return readDataKey(input, registry, null);
    }

    public static DataKey<?> readDataKey(DataInput input, DataKeyRegistry registry, Consumer<String> missingDataKeyHandler) throws IOException {
        String id = input.readUTF();
        boolean hasParameter = input.readBoolean();
        String parameter = hasParameter ? input.readUTF() : null;

        DataKey<?> key = registry.getKeyByIdentifier(id);
        if (key != null) {
            return hasParameter ? key.withParameter(parameter) : key;
        } else {
            if (missingDataKeyHandler != null) {
                missingDataKeyHandler.accept(id);
            }
            return null;
        }
    }
}
