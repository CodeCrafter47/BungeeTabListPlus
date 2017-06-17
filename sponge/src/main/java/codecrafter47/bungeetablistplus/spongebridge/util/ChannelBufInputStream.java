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

package codecrafter47.bungeetablistplus.spongebridge.util;

import org.spongepowered.api.network.ChannelBuf;

import java.io.IOException;
import java.io.InputStream;

public class ChannelBufInputStream extends InputStream {

    private final ChannelBuf channelBuf;

    public ChannelBufInputStream(ChannelBuf channelBuf) {
        this.channelBuf = channelBuf;
    }

    @Override
    public int read() throws IOException {
        return channelBuf.available() > 0 ? Byte.toUnsignedInt(channelBuf.readByte()) : -1;
    }
}
