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

package codecrafter47.bungeetablistplus.tablisthandler.logic;

import codecrafter47.bungeetablistplus.eventlog.EventLogger;
import codecrafter47.bungeetablistplus.eventlog.Transformer;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TestRealWorldExamples extends AbstractTabListLogicTestBase {
    private static Gson gson = new Gson();

    private final String fileName;

    @Parameterized.Parameters(name = "{0}")
    public static Collection testData() {
        return Arrays.asList(
                "325c6cf5-52d9-4ccb-9347-49c570b78f47.log",
                "6f3126f7-245e-416f-801b-495dd1c16ad5.log",
                "8ae7c38b-9a6e-4e6b-82f3-1175ed63632d.log",
                "174b2e9c-1ecb-47cb-a7bb-aef46be4ea99.log",
                "f6ae3390-552a-4f70-996b-480592be5f20.log",
                "6b4dfedf-1895-4742-b376-5801ba9ea00d.log",
                "e54d2444-c5dc-499b-8ad9-bbbe84dca8dc.log",
                "faad43b7-ff83-464f-a86f-624ac0ff3609.log",
                "5e6084b9-fe50-44aa-8f61-d49a6a27a230.log",
                "523a5290-6e47-4c89-abc3-0a44d039f263.log",
                "69b13863-76c9-4e34-bf5b-ec294302bdb4.log",
                "7fef47b5-0f44-4391-95d2-1a663b00f54d.log",
                "fe22c818-806e-4b85-8d8b-763a6edea708.log",
                "242b57fd-5c5f-4aca-bc9f-2b8f2dab3a02.log",
                "f40d13fd-b6c5-4eb5-8e5d-a47d141e87ba.log",
                "413e71cd-6181-4ffa-bea6-ab9144c0cd23.log",
                "1fa34e67-e41b-40f5-9f57-ddcc396f83b8.log");
    }

    public TestRealWorldExamples(String fileName) {
        this.fileName = fileName;
    }


    @Test
    @SneakyThrows
    public void testRealWorldData() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            boolean connected = false;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    String[] tokens = line.split(" ", 2);
                    assertEquals(2, tokens.length);

                    switch (tokens[0]) {
                        case "connect":
                            assertFalse(connected);
                            clientUUID = UUID.fromString(gson.fromJson(tokens[1], String.class));
                            connected = true;
                            break;
                        case "disconnect":
                            assertTrue(connected);
                            connected = false;
                            break;
                        case "pli":
                            assertTrue(connected);
                            Transformer.PlayerListPacketWrapper wrapper = gson.fromJson(tokens[1], Transformer.PlayerListPacketWrapper.class);
                            tabListHandler.onPlayerListPacket(wrapper.unwrap());
                            break;
                        case "team":
                            assertTrue(connected);
                            tabListHandler.onTeamPacket(gson.fromJson(tokens[1], net.md_5.bungee.protocol.packet.Team.class));
                            break;
                        case "serverSwitch":
                            assertTrue(connected);
                            tabListHandler.onServerSwitch();
                            break;
                        case "passThrough":
                            assertTrue(connected);
                            tabListHandler.setPassThrough(gson.fromJson(tokens[1], Boolean.class));
                            break;
                        case "size":
                            assertTrue(connected);
                            tabListHandler.setSize(gson.fromJson(tokens[1], Integer.class));
                            break;
                        case "set":
                            assertTrue(connected);
                            EventLogger.SetData data = gson.fromJson(tokens[1], EventLogger.SetData.class);
                            tabListHandler.setSlot(data.index, data.skin.unwrap(), data.text, data.ping);
                            break;
                        default:
                            fail("Unknown token " + tokens[0]);
                    }
                } catch (Throwable th) {
                    throw new AssertionError("Error processing line " + lineNumber, th);
                }
            }
            assertFalse(connected);
        }
    }
}
