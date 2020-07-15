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
import de.codecrafter47.taboverlay.config.misc.ChatFormat;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TestRealWorldExamples extends AbstractTabListLogicTestBase {
    private static Gson gson = new Gson();

    private final String fileName;

    @Parameterized.Parameters(name = "{0}")
    public static Collection testData() {
        return Arrays.asList(
                "00ea3262-465a-42e8-a5e2-0893b2a5daa1.log",
                "014c4747-7354-4f48-88e8-12baa0e73ef8.log",
                "02405540-f3ab-4e39-897e-08349a216b63.log",
                "0927aced-5296-4f98-80af-3f3521f8f6c4.log",
                "0f81aaa3-dbf8-4aea-9170-86b0929499fe.log",
                "13ff9cfe-1ebc-470d-af25-e534e2355be5.log",
                "17369cb4-7ccc-49ed-b86e-7e615909977d.log",
                "1d04a25a-37c4-4b6d-8b16-086b6c2bcec9.log",
                "220fed38-90e0-43dd-a7f5-b4e7e33112a1.log",
                "333ece55-2cb3-4cef-9244-71f8f3214493.log",
                "388b12ef-d94d-4d7c-b76e-4e5ce76e4f1d.log",
                "39007308-9e4a-49d2-ac75-19310ee0fe8e.log",
                "3f241e3f-3eb3-420e-8737-c32786eca1be.log",
                "488fa788-fb13-4296-8991-bde38d30af3c.log",
                "49284790-eafc-4f31-8887-130fe1aed2dc.log",
                "4e383985-617a-467c-82cc-d90bc45974e3.log",
                "58bab797-b40e-4f95-a7d0-d8c319aa8dd1.log",
                "5d234b53-35e5-4904-8baa-d29df091896e.log",
                "5ef6819c-8eeb-474f-82d6-256d8ffb35aa.log",
                "60130686-a9c2-4b36-908f-5bef4b49b2e4.log",
                "60f9b50a-d30b-4d6d-a936-2c1128511ba6.log",
                "670587e1-b5b1-4f4c-aaac-61634b3ba676.log",
                "6aa57e7a-7afc-4c4b-b868-cc3817c33d8d.log",
                "6e743e29-998d-4e78-81ad-6104d3018548.log",
                "6f569108-3c1f-4fb3-aac4-890be8de27dd.log",
                "7623a40f-f889-4685-86bd-2751dedb5097.log",
                "7965c076-0f69-4b87-9a98-2269e5b08c9f.log",
                "7be3982d-c8a5-4ed6-851f-ffff31a432cf.log",
                "7d652a5c-364b-4ca5-b3b9-d0a3bcef58e8.log",
                "7f8fcf47-b4aa-48d3-ae81-58f1122641ac.log",
                "8548e7ec-d2bd-4b89-bc4b-c0f0b3855f4d.log",
                "862d75e9-d952-4c0a-bef0-15366e2a427b.log",
                "89aa9b9d-d5a7-4a72-83b4-9d6017656e3b.log",
                "89d93be0-1312-41a3-b2b3-1b4367450e51.log",
                "8a0fb5de-8355-440c-a4ba-e930e493ae0f.log",
                "91611096-ed15-40ad-8230-e772faf8fd67.log",
                "9414f86b-8dc5-43e5-abbb-a8772c9e026e.log",
                "95d26dfb-8bac-4ea5-acf9-706c4dae9cd5.log",
                "9deecc57-3690-4ddd-b497-4829b68f643f.log",
                "a0ea010a-fa94-4b39-a108-8d7b3a9861d3.log",
                "a532190f-0a4e-482a-834b-837f7013c891.log",
                "a7b58de1-0590-4729-8da2-9d77e1cfe044.log",
                "a909a2de-8498-460f-9773-88f8beb60898.log",
                "abd2821f-ad17-48e8-b95d-28fa2de2cc3c.log",
                "af22d61e-ffc4-4ee0-8419-be5e90e468bd.log",
                "b3c20ba0-c124-4527-9f31-1653c22dc3c1.log",
                "bc6f43c3-5fe1-414e-9e46-156fac74c322.log",
                "c1283d28-fce6-4f16-a833-cd5f618d0a77.log",
                "c3e746c6-63ce-466c-8d68-4471cbb8c8db.log",
                "cb06ca42-d1d0-43e7-bcc4-a32178d3cddc.log",
                "ce840188-f789-4516-8d92-894104fa678c.log",
                "d024c261-cf70-4eae-a23a-8744e0422e8e.log",
                "d61c9b4a-6c3d-4108-b519-ecab95028052.log",
                "d771a65a-d9cf-427f-9ad4-73a3d8a4b7bf.log",
                "d9edd8f0-3c1f-4742-9b85-290c662283eb.log",
                "e47fa7b1-73d2-4107-b5ab-d02b90e26a3b.log",
                "f4f9d8dc-6be1-4145-9ce9-97a794fba72b.log",
                "f56cb6b5-0443-4e1c-80b6-38c3181be619.log",
                "f5fd03c0-38ae-4e7f-812d-1d4817984fd2.log",
                "f7241cdb-b4a9-4f06-bf92-6461d57f4d2e.log",
                "ff7b9f66-6506-4815-9750-2f7284313d19.log",
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
            String fakeClientID = null;
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
                            fakeClientID = gson.fromJson(tokens[1], String.class);
                            connected = true;
                            break;
                        case "disconnect":
                            assertTrue(connected);
                            connected = false;
                            break;
                        case "pli":
                            assertTrue(connected);
                            Transformer.PlayerListPacketWrapper wrapper = gson.fromJson(tokens[1], Transformer.PlayerListPacketWrapper.class);
                            for (Transformer.TabListItemWrapper item : wrapper.items) {
                                if (fakeClientID.equals(item.uuid)) {
                                    item.uuid = clientUUID.toString();
                                }
                            }
                            tabListHandler.onPlayerListPacket(wrapper.unwrap());
                            break;
                        case "team":
                            assertTrue(connected);
                            tabListHandler.onTeamPacket(gson.fromJson(tokens[1], net.md_5.bungee.protocol.packet.Team.class));
                            break;
                        case "serverSwitch":
                            tabListHandler.onServerSwitch(false);
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
                            if (fakeClientID.equals(data.skin.owner)) {
                                data.skin.owner = clientUUID.toString();
                            }
                            tabListHandler.setSlot(data.index, data.skin.unwrap(), ChatColor.stripColor(data.text), data.ping);
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
