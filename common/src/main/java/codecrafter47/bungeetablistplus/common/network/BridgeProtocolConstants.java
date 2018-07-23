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

    public static final String CHANNEL = "btlp:bridge";

    public static final int VERSION = 5;

    public static final int MESSAGE_ID_INTRODUCE = 0x00;

    public static final int MESSAGE_ID_ACK = 0x01;
    public static final int MESSAGE_ID_REQUEST_DATA = 0x02;
    public static final int MESSAGE_ID_UPDATE_DATA = 0x03;

    public static final int MESSAGE_ID_ACK_SERVER = 0x81;
    public static final int MESSAGE_ID_REQUEST_DATA_SERVER = 0x82;
    public static final int MESSAGE_ID_UPDATE_DATA_SERVER = 0x83;
}
