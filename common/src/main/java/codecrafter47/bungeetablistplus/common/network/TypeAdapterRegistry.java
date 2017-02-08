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

import com.google.common.collect.ImmutableMap;
import de.codecrafter47.data.api.TypeToken;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TypeAdapterRegistry {

    public static final TypeAdapterRegistry DEFAULT_TYPE_ADAPTERS = new TypeAdapterRegistry(ImmutableMap.<TypeToken<?>, TypeAdapter<?>>builder()
            .put(TypeToken.BOOLEAN, new TypeAdapter<Boolean>() {
                @Override
                public Boolean read(DataInput input) throws IOException {
                    return input.readBoolean();
                }

                @Override
                public void write(DataOutput output, Boolean object) throws IOException {
                    output.writeBoolean(object);
                }
            })
            .put(TypeToken.INTEGER, new TypeAdapter<Integer>() {
                @Override
                public Integer read(DataInput input) throws IOException {
                    return input.readInt();
                }

                @Override
                public void write(DataOutput output, Integer object) throws IOException {
                    output.writeInt(object);
                }
            })
            .put(TypeToken.FLOAT, new TypeAdapter<Float>() {
                @Override
                public Float read(DataInput input) throws IOException {
                    return input.readFloat();
                }

                @Override
                public void write(DataOutput output, Float object) throws IOException {
                    output.writeFloat(object);
                }
            })
            .put(TypeToken.DOUBLE, new TypeAdapter<Double>() {
                @Override
                public Double read(DataInput input) throws IOException {
                    return input.readDouble();
                }

                @Override
                public void write(DataOutput output, Double object) throws IOException {
                    output.writeDouble(object);
                }
            })
            .put(TypeToken.STRING, new TypeAdapter<String>() {
                @Override
                public String read(DataInput input) throws IOException {
                    return input.readUTF();
                }

                @Override
                public void write(DataOutput output, String object) throws IOException {
                    output.writeUTF(object);
                }
            })
            .put(TypeToken.DURATION, new TypeAdapter<Duration>() {
                @Override
                public Duration read(DataInput input) throws IOException {
                    long seconds = input.readLong();
                    int nanos = input.readInt();
                    return Duration.ofSeconds(seconds, nanos);
                }

                @Override
                public void write(DataOutput output, Duration object) throws IOException {
                    output.writeLong(object.getSeconds());
                    output.writeInt(object.getNano());
                }
            })
            .put(TypeToken.STRING_LIST, new TypeAdapter<List<String>>() {
                @Override
                public List<String> read(DataInput input) throws IOException {
                    int size = input.readInt();
                    ArrayList<String> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        list.add(input.readUTF());
                    }
                    return list;
                }

                @Override
                public void write(DataOutput output, List<String> object) throws IOException {
                    int size = object.size();
                    output.writeInt(size);
                    for (int i = 0; i < size; i++) {
                        String element = object.get(i);
                        output.writeUTF(element);
                    }

                }
            }).build());

    public static TypeAdapterRegistry of(TypeAdapterRegistry... registries) {
        ImmutableMap.Builder<TypeToken<?>, TypeAdapter<?>> builder = ImmutableMap.builder();
        for (TypeAdapterRegistry registry : registries) {
            builder.putAll(registry.map);
        }
        return new TypeAdapterRegistry(builder.build());
    }

    private final ImmutableMap<TypeToken<?>, TypeAdapter<?>> map;

    public TypeAdapterRegistry(ImmutableMap<TypeToken<?>, TypeAdapter<?>> map) {
        this.map = map;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> getTypeAdapter(TypeToken<T> type) {
        return (TypeAdapter<T>) map.get(type);
    }
}
