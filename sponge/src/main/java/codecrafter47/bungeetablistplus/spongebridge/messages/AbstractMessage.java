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

import codecrafter47.bungeetablistplus.spongebridge.util.ChannelBufInputStream;
import codecrafter47.bungeetablistplus.spongebridge.util.ChannelBufOutputStream;
import lombok.SneakyThrows;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.*;

public abstract class AbstractMessage implements Message {

    public void readFrom(DataInput input) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void writeTo(DataOutput output) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SneakyThrows
    public final void readFrom(ChannelBuf buf) {
        readFrom(new DataInputStream(new ChannelBufInputStream(buf)));
    }

    @Override
    @SneakyThrows
    public final void writeTo(ChannelBuf buf) {
        writeTo(new DataOutputStream(new ChannelBufOutputStream(buf)));
    }
}
