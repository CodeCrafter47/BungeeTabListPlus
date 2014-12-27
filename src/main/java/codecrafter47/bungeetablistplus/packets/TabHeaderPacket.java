/*
 * Copyright (C) 2014 florian
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
package codecrafter47.bungeetablistplus.packets;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

/**
 * @author florian
 */
public class TabHeaderPacket extends DefinedPacket {

    private String header, footer;

    public TabHeaderPacket() {
        header = ComponentSerializer.toString(TextComponent.fromLegacyText(""));
        footer = ComponentSerializer.toString(TextComponent.fromLegacyText(""));
    }

    public TabHeaderPacket(String header, String footer) {
        this.header = header;
        this.footer = footer;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    @Override
    public void handle(AbstractPacketHandler aph) throws Exception {
        // I don't think this gets ever called...
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TabHeaderPacket && ((TabHeaderPacket) o).footer.
                equals(this.footer) && ((TabHeaderPacket) o).header.equals(
                this.header);
    }

    @Override
    public int hashCode() {
        return footer.hashCode() + header.hashCode();
    }

    @Override
    public String toString() {
        return "{H: " + header + ", F: " + footer + "}";
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction,
                     int protocolVersion) {
        // I don't think we receive that packet
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction,
                      int protocolVersion) {
        writeString(header, buf);
        writeString(footer, buf);
    }

}
