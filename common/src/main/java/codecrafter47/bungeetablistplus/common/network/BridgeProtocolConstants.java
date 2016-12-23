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

/**
 * This class holds some constants required to implement the bridge protocol.
 */
public class BridgeProtocolConstants {

    public static final String CHANNEL = "BTLP";

    public static final int VERSION = 3;

    public static final int MESSAGE_ID_PROXY_HANDSHAKE = 0x80;
    public static final int MESSAGE_ID_PROXY_REQUEST_DATA = 0x81;
    public static final int MESSAGE_ID_PROXY_REQUEST_SERVER_DATA = 0x82;
    public static final int MESSAGE_ID_PROXY_REQUEST_RESET_SERVER_DATA = 0x83;
    public static final int MESSAGE_ID_PROXY_OUTDATED = 0xAC;

    public static final int MESSAGE_ID_SERVER_HANDSHAKE = 0x00;
    public static final int MESSAGE_ID_SERVER_UPDATE_DATA = 0x01;
    public static final int MESSAGE_ID_SERVER_UPDATE_SERVER_DATA = 0x02;
    public static final int MESSAGE_ID_SERVER_DISABLE_CONNECTION = 0x20;
    public static final int MESSAGE_ID_SERVER_ENABLE_CONNECTION = 0x21;
    public static final int MESSAGE_ID_SERVER_OUTDATED = 0xAC;
}
