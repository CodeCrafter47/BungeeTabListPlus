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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.Skin;
import codecrafter47.bungeetablistplus.skin.AnimatedSkin;
import codecrafter47.bungeetablistplus.skin.PlayerSkin;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedHashTreeMap;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class SkinManager {

    private final Plugin plugin;
    private static final Gson gson = new Gson();

    private final Cache<String, Skin> cache = CacheBuilder.newBuilder().expireAfterAccess(35, TimeUnit.MINUTES).build();
    private final Map<File, Skin> fileSkinCache = new ConcurrentHashMap<>();
    private final Map<Head, Skin> headCache = new ConcurrentHashMap<>();

    public static final Skin defaultSkin = new PlayerSkin(null, null);
    public static final Skin missingSkinTexture = new PlayerSkin(null, new String[]{"textures", "eyJ0aW1lc3RhbXAiOjE0NTU1MzU4NTE1NzIsInByb2ZpbGVJZCI6ImIzYjE4MzQ1MzViZjRiNzU4ZTBjZGJmMGY4MjA2NTZlIiwicHJvZmlsZU5hbWUiOiIxMDExMTEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgwNzg2M2FiY2JmNWFhMjFhNDQxYzhiZjJkMzIzOTUyZjQ2OTU5ZTc1ODZjYjgzMTg5MWI5MGM3ZTg3ZTUwIn19fQ==", "n1MBwf8EQC2CJdmHjpDj0/O1339eeE0uPxnlowyWa+q9bsmHYYFxv6Cwg7ROxsV5YFGN08/kqIYaQFoYSBh5piihDs0zZpDTeDvpO3OOWNQ2Q9TU3ixdD+25ZkVW5CwIZKkuoa5r31zAmP894WeMXbf25M4OEkK7c2YBphksuWDWhikaK8x9p2C8CZetidqa4kzSBCSYT/rI+7whQ8v00Dk4coWaFzL8+C2f6jLLJZo7JCMt4I5uTuhEaqb8snuq5cmYGozrbhjQ+VbX8g/zy8M0JCZk2iAxWKva0HpPQ0TtQ7oWtfebi0e4QmgnImgbVM79b7cW22m0nvl4xcuZ5tbQS17xKIOndP5fI/xGtf2FWZ/8TSLXauNlJi1JrLrP7uVDdtalhzHL+RcsnXuxOoifFNJwTDjJSczOsR2eeqtZKEWg6E9ZZVk6Pyyi237+l06wEEftYDC2xDo7wrPqyjiXKhPGxtcydNQVM4HyU/25nr7y0QDEX+AFUWtpiUhZXKXxtgNF8bSKIFSFcpQvG78VsSdbFWTmzTc0k0Rqmh25iMGiVUgdL1h9/E+2kL6j+PEoQDs5GLfpKGrJfolptzcQbLS2Jabir9JcI/geF2qMyv8Q/nrZsODZgkYV2sDG6lwsXFaQ+ndSH5AV1oN2Mj2CiqvcMw3ymufcfwoP38E="});
    public static final Skin loadingSkinTexture = new AnimatedSkin(
            new PlayerSkin(null, new String[]{"textures", "eyJ0aW1lc3RhbXAiOjE0NTU1MzgwOTYxMTUsInByb2ZpbGVJZCI6ImIzYjE4MzQ1MzViZjRiNzU4ZTBjZGJmMGY4MjA2NTZlIiwicHJvZmlsZU5hbWUiOiIxMDExMTEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJiNjE4OWIzOTA0NzQ2ZjFjZTU3NmM3YWViNWM1MTQ1ZDNkNzgxNTZjMTRkOTc2YTU3ZmVlYzFjYzdhYjUxIn19fQ==", "eS+r00BVAXb5Z8FUzrPbDxpZ+ZxaZUgidWPPBXH8LPO58Kwikqf5yaw5G7TOrVR4yyIil6MjumpC3bmrsgChl+8ObS6s3uafV4p0EXSwD2XNSN2QJaldXYmPLtvpMSmdoXJmpR+o4EmJwvY7c4MeghDsPjXmiJts/ygNf9rnF1W/Iqw9z/CTRsiCouEuf3BnBjo1ID1/XJfZWrXsOS/Go4WMCm0DRVuAXKxfI7lcm04RRQx8HHARUjPslMEBBS+FWa0bSFEhSK+PTETN1lFOl2dCgmuKcyS570TCHeLRbedpQ5geJ3i6ka4DQCz2jf3zT++CSlZpfJDsnq3DQoQ2nJ5InrPI79+3o+QkL04+4WsT59tYDaYEjai9TxtEKyp7eYEz31z5Xd91Mh79dphqQeFBz1wgH7F7bCU2AdZKm1VUsmFXf3kI1JHpUyvcvtPAIVqjFEqwcW4sGSbm2u4Wl43UadMl5VwbdohA+mrPLcqCOTJ6IaFNzYZ43Gi/rX7Xd2mQ1kdBw9GfDIndr9sRge8rchfJx8Axiqn/SPT749IhU+9MXY9yGv9kFUCJqOOxb587Qkb7Tn4LZEa7eXOuni+CiT6gc9a415lXkco+MzwTXVIiw9BDxmOvqXCPqssdvAzxq8YDi7frdP3WWfbjvoABhcfv5cU3vQVvDxto1NE="}),
            new PlayerSkin(null, new String[]{"textures", "eyJ0aW1lc3RhbXAiOjE0NTU1MzgzNDcxMTAsInByb2ZpbGVJZCI6ImIzYjE4MzQ1MzViZjRiNzU4ZTBjZGJmMGY4MjA2NTZlIiwicHJvZmlsZU5hbWUiOiIxMDExMTEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU3ODgyY2Q4NjdlNTg4ODQzYzMwYWRkMWU4NjMyNjI4MDViZGQ5NDU3Y2RiNWIzNzczZDc1ZjNmOTFhY2UyIn19fQ==", "OyYeEsKx38jP/XqSdeSbDtgBIzL7DwmlqgzMkbkz4QsYa+SRoEHulOrSettDneWkHgKndape0kZglUsG1R27OdK/px3zem8Wfrgc/7toBghlbW12wbbaKYDcUf6PcLhZ2te7vBGn+vR/oHW8vETIAu1xyLL4EXT3E2IGlv54JZHPsmAQ4dC7raUiVOTr8sh04JBA3okljYyqeWbuAj/vyQxoDufm1l+Z5Ny5kq5kM3UblMcdor4uU/8Qm2ECJbqJj3O2Pqy04VYvpha0TcVyTo4WuNCTGtSz3gYzPhXXURFIZUV7KYVaWHgm+mmLD9wJDAxG/w8XN2LtEOMZEdE7d9zK23ankboISOK3+cCQkxfvSPm4t2iec2HeQim3w4G/WqYfwpqf/+vWR/dSdaCAL5oTG300WDWdvuHqOuwFbMXMw86vcNzR4rI20hdvnTaL0Mko6cArsy1Tr+szceU+u/gpwnWrMgIjybFYIAhYn4HuHBKRU0KxlnSrtAIqtKWiVVjaelQ8yc+np3P2XAx43aTA9C8kQDKauzxUDMTvk8kiBeRBqtlgxUxCTKSdp423V+HTZotYZ+p+Vzo8somYKu4fbeHWgc2WEVYlpcZJLw41tyrZTRL158XD937kbj0PO2EYtaKyl8aSi/m1uacETe5jMCXUTL/Od82pS8GKCso="}),
            new PlayerSkin(null, new String[]{"textures", "eyJ0aW1lc3RhbXAiOjE0NTU1MzgzODQ4NTUsInByb2ZpbGVJZCI6ImIzYjE4MzQ1MzViZjRiNzU4ZTBjZGJmMGY4MjA2NTZlIiwicHJvZmlsZU5hbWUiOiIxMDExMTEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQxOTE1M2ZiYjRkNDg2OWRlYzRhMTU3ZGJmMmJiZmZlMWU4YjkxYzFiZDdjOGEyN2VmZjdlZjIyMjhmYzNjIn19fQ==", "yRebjNXkshOG+WpOh591UqQBQzt3m3LAaJbTUz6RVWIo7iLvfqgvCJqxhNXR0TM3glWoIAVRx5xmnVLbxMnYfjeuKHMxMtweYSCoqPvPewa2z6JgzH88p16UeO+kuRIeDqxc5OvFKYRjeBjnLJvi8voEh1pkPmUAdm6UHrBsgqBrPxj/w+MHmHXfAj8n3BsbOc1cdVDSSOYYWLI5GZJaUcBMPea0Yy+OGNfR+SGH1DnX9c4nYEPgZ9WaXcGEDZjAHf36DQFoYceV/e2GPFLa7uBNV99GL7hQWJ4xaYgRxATuRAANDFAHTtNEkwb4NVb1LMiFyiJD24Ik4LfQs552a4cNCvq+MkneI+JFtqR4UATHlu/xM1GilGx7QZCi2fZW9DN7tpXbm2o+Q6g0ZXLdVQkqK/vv9p2+qyNktzaZewx9IjLfOaMUyeiD5IbMT2YJFjrLIExoqaciPV8edwjOOmICfJdnbaCYWYfRGTasPplsv2Dr/EqY2acDlbfBAeCQFnQa+7e0PmhmFQT34vouMdcw4Qdg5/LDMu8GJgZeEU1VBMMbX1YOr4dPuhOkt5iGQBFp0dp84Y8f7i7MImciAxM0Gi/mH43FfR56knEf54V0hMh+Jz1HAKpAUK4mwpVVGG8+tqR1lmnDvcHVU4asimCHExskq6eotPxrAGCM27Y="}),
            new PlayerSkin(null, new String[]{"textures", "eyJ0aW1lc3RhbXAiOjE0NTU1MzgzMDkyNTAsInByb2ZpbGVJZCI6ImIzYjE4MzQ1MzViZjRiNzU4ZTBjZGJmMGY4MjA2NTZlIiwicHJvZmlsZU5hbWUiOiIxMDExMTEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU0M2NkNmQ5NjcxZDlkZmIyNzNmMzQxODFmNzE2NDg3ODEwNjRlMTdjNDNmNWI2MzQ3MzVhNjVlZTFiZWE3In19fQ==", "kgGitINyQS5Xz0Dn7jn059m/sEyaDrROYsGZL6n80e97O3745nNxKXmfrWJ2Q12gvlmw0AiIQfLG8VV3Id+WMnRTqDmyZRkW94yJLiVes1mlNwa0HAXtygAXDpchV713zx9VgAj56SsGJN3disrCCEL0Swca7YD6ciPkZzYE8gL5tUQQwaoHv7Gsd79IiM3B5OF/egSQ4UBbnX72Bxdb+8vJVQUwmeGUm+7//z+g7w2DSlYv1ov54zn1Tlo9an67M6eNZVPy1RrzYlFIaHpht8zwZUsKL13wQyEkthr0QLKT1OsWLHmTRy6jE9JL4Q9IoKblasERVTGPzUi63D2DajeCVrVqVebqy3KWgMJjDz8WklrCU98eLV99jZt7aZ7mu5l9I75KdyymMtl7WkwHovAftaJTOK/d7TEhDnbNPGbIETqAUiGZJBHLS2yrsREQlkxzYkC+0+1Z3Yt3Itkuuu+WMRInf5GMZ2UPGu+dHyDmg1M6LsgU4uJm2i9ehvTZ+zQCLAvWjrOlWO9kCFSryz3cpRUO2dqTlpCzx0StiLXLrwUE3b+3F/XK1FbsABE4n8RKXGczyb0KKb29iktKuxJ8M9qPVOGO2xH1C7uG0JO80yqxnVk4+P21tbVvS7qikNu8dmWkC1YTffhZHHop1aBGcDUISOZMYF8ie2rom54="})
    );

    private final Set<String> fetchingSkins = Sets.newConcurrentHashSet();

    private final File headsFolder;

    private final static Pattern PATTERN_VALID_USERNAME = Pattern.compile("(?:\\p{Alnum}|_){1,16}");
    private final static Pattern PATTERN_VALID_UUID = Pattern.compile("(?i)[a-f0-9]{8}-?[a-f0-9]{4}-?4[a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}");
    private final static Pattern PATTERN_OFFLINE_UUID = Pattern.compile("(?i)[a-f0-9]{8}-?[a-f0-9]{4}-?3[a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}");

    public SkinManager(Plugin plugin, File headsFolder) {
        this.plugin = plugin;
        this.headsFolder = headsFolder;

        // load head cache
        File file = new File(headsFolder, "cache.txt");
        if (file.exists()) {
            try {
                new BufferedReader(new FileReader(file)).lines()
                        .filter(line -> !line.isEmpty())
                        .map(line -> line.split(" "))
                        .forEach(entry -> headCache.put(
                                Head.of(Base64.getDecoder().decode(entry[0])),
                                new PlayerSkin(null, new String[]{"textures", entry[1], entry[2]})
                        ));
            } catch (Throwable th) {
                plugin.getLogger().log(Level.WARNING, "Failed to load heads/cache.txt", th);
            }
        }
    }

    @SneakyThrows
    public Skin getSkin(String nameOrUUID) {
        Preconditions.checkNotNull(nameOrUUID, "nameOrUuid");

        if (nameOrUUID.endsWith(".png")) {
            return getSkin(new File(headsFolder, nameOrUUID));
        }

        if (PATTERN_OFFLINE_UUID.matcher(nameOrUUID).matches()) {
            nameOrUUID = nameOrUUID.replace("-", "");
            UUID uuid = UUID.fromString(nameOrUUID.substring(0, 8) + "-" + nameOrUUID.substring(8, 12) + "-" + nameOrUUID.substring(12, 16) + "-" + nameOrUUID.substring(16, 20) + "-" + nameOrUUID.substring(20, 32));
            if ((uuid.hashCode() & 1) == 1) {
                // todo return alex skin;
            } else {
                //todo return steve skin;
            }
        }

        Skin skin = cache.getIfPresent(nameOrUUID);
        if (skin != null) return skin;
        if (!fetchingSkins.contains(nameOrUUID)) {
            fetchingSkins.add(nameOrUUID);
            ProxyServer.getInstance().getScheduler().schedule(plugin, new SkinFetchTask(nameOrUUID), 0, TimeUnit.MILLISECONDS);
        }
        return defaultSkin;
    }

    @SneakyThrows
    public Skin getSkin(File file) {
        Preconditions.checkNotNull(file, "file");

        Skin skin = fileSkinCache.get(file);
        if (skin != null) {
            return skin;
        }

        if (!file.getAbsolutePath().startsWith(headsFolder.getAbsolutePath())) {
            plugin.getLogger().warning("Requested head outside the heads folder: " + file.getAbsolutePath());
            fileSkinCache.put(file, missingSkinTexture);
            return missingSkinTexture;
        }

        fileSkinCache.put(file, loadingSkinTexture);
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            if (!file.exists()) {
                plugin.getLogger().warning("Requested file does not exists: " + file.getAbsolutePath());
                fileSkinCache.put(file, missingSkinTexture);
                return;
            }

            try {
                BufferedImage head = ImageIO.read(file);
                if (head.getWidth() != 8 || head.getHeight() != 8) {
                    plugin.getLogger().warning("Image " + file + " has the wrong size. Required 8x8 actual " + head.getWidth() + "x" + head.getHeight());
                    fileSkinCache.put(file, missingSkinTexture);
                    return;
                }

                int[] rgb = head.getRGB(0, 0, 8, 8, null, 0, 8);
                ByteBuffer byteBuffer = ByteBuffer.allocate(rgb.length * 4);
                byteBuffer.asIntBuffer().put(rgb);
                byte[] headArray = byteBuffer.array();

                if (headCache.containsKey(Head.of(headArray))) {
                    Skin skin1 = headCache.get(Head.of(headArray));
                    fileSkinCache.put(file, skin1);

                    // a new skin is available -> update tab to all players
                    BungeeTabListPlus.getInstance().resendTabLists();

                    return;
                }

                fetchHeadSkin(file, headArray);
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to load file " + file.getName());
                fileSkinCache.put(file, missingSkinTexture);
            }
        });

        return loadingSkinTexture;
    }

    private void fetchHeadSkin(File file, byte[] headArray) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("http://skinservice.codecrafter47.dyndns.eu/api/customhead").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            try (DataOutputStream out = new DataOutputStream(connection.
                    getOutputStream())) {
                out.write((Base64.getEncoder().encodeToString(headArray)).getBytes(Charsets.UTF_8));
                out.flush();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
            LinkedHashTreeMap map = gson.fromJson(reader, LinkedHashTreeMap.class);
            if (map.get("state").equals("ERROR")) {
                plugin.getLogger().warning("An server side error occurred while preparing head " + file.getName());
                fileSkinCache.put(file, missingSkinTexture);
            } else if (map.get("state").equals("QUEUED")) {
                plugin.getLogger().info("Preparing head " + file.getName() + " approx. " + map.get("timeLeft") + " minutes remaining.");
                ProxyServer.getInstance().getScheduler().schedule(plugin, () -> fetchHeadSkin(file, headArray), 30, TimeUnit.SECONDS);
            } else if (map.get("state").equals("SUCCESS")) {
                PlayerSkin skin = new PlayerSkin(null, new String[]{"textures", (String) map.get("skin"), (String) map.get("signature")});
                fileSkinCache.put(file, skin);
                headCache.put(Head.of(headArray), skin);
                plugin.getLogger().info("Head " + file.getName() + " is now ready for use.");

                // we received a new skin -> update tab to all players
                BungeeTabListPlus.getInstance().resendTabLists();

                // save to cache
                File cacheFile = new File(headsFolder, "cache.txt");
                if (!cacheFile.exists()) {
                    cacheFile.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile, true));
                writer.write(Base64.getEncoder().encodeToString(headArray));
                writer.write(' ');
                writer.write(skin.toProperty()[1]);
                writer.write(' ');
                writer.write(skin.toProperty()[2]);
                writer.newLine();
                writer.close();
            } else {
                plugin.getLogger().severe("Unexpected response from server: " + map.get("state"));
                fileSkinCache.put(file, missingSkinTexture);
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "An error occurred while trying to contact skinservice.codecrafter47.dyndns.eu", ex);
            plugin.getLogger().warning("Unable to prepare head " + file.getName());
            fileSkinCache.put(file, missingSkinTexture);
        }
    }

    public void onReload() {
        fileSkinCache.clear();
    }

    private String fetchUUID(final String player) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(
                    "https://api.mojang.com/profiles/minecraft").
                    openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            try (DataOutputStream out = new DataOutputStream(connection.
                    getOutputStream())) {
                out.write(("[\"" + player + "\"]").getBytes(Charsets.UTF_8));
                out.flush();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), Charsets.UTF_8));
            Profile[] profiles = gson.fromJson(reader, Profile[].class);
            if (profiles != null && profiles.length >= 1) {
                return profiles[0].id;
            }
            BungeeTabListPlus.getInstance().getLogger().warning("can't fetch uuid for '" + player + "', is not valid name");
        } catch (Throwable e) {
            if (e instanceof IOException && e.getMessage().contains("429")) {
                // mojang rate limit; try again later
                plugin.getLogger().warning("Hit mojang rate limits while fetching uuid for " + player + ".");
                String headerField = connection.getHeaderField("Retry-After");
                plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                    @Override
                    public void run() {
                        fetchingSkins.remove(player);
                    }
                }, headerField == null ? 300 : Integer.valueOf(headerField), TimeUnit.SECONDS);
            } else {
                // generic connection error, retry in 30 seconds
                plugin.getLogger().warning("An error occurred while connecting to mojang servers: " + e.getMessage() + ". Will retry in 1 minute");
                plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                    @Override
                    public void run() {
                        fetchingSkins.remove(player);
                    }
                }, 60, TimeUnit.SECONDS);
            }
        }
        return null;

    }

    private Skin fetchSkin(String uuid) {
        try {
            uuid = uuid.replace("-", "");
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").
                    openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), Charsets.UTF_8));
            SkinProfile skin = gson.fromJson(reader, SkinProfile.class);
            if (skin != null && skin.properties != null && !skin.properties.isEmpty()) {
                return new PlayerSkin(UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32)), new String[]{"textures", skin.properties.get(0).value, skin.properties.
                        get(0).signature});
            }
        } catch (Throwable e) {
            if (e instanceof IOException && e.getMessage().contains("429")) {
                // mojang rate limit; try again later
                plugin.getLogger().info("Hit mojang rate limits while fetching skin for " + uuid + ". Will retry in 1 minute. (This is not an error)");
            } else {
                // generic connection error
                plugin.getLogger().log(Level.WARNING, "An error occurred while connecting to mojang servers. Couldn't fetch skin for " + uuid + ". Will retry in 1 minute.", e);
            }
        }
        return null;

    }

    private static class Profile {

        private String id;
        private String name;
    }

    private static class SkinProfile {

        private String id;
        private String name;

        final List<Property> properties = new ArrayList<>();

        private static class Property {

            private String name, value, signature;
        }
    }

    private static final class Head {
        private final byte[] bytes;

        private Head(byte[] bytes) {
            this.bytes = bytes;
        }

        static Head of(byte[] bytes) {
            return new Head(bytes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Head && Arrays.equals(bytes, ((Head) obj).bytes);
        }
    }

    private class SkinFetchTask implements Runnable {

        final String nameOrUUID;

        public SkinFetchTask(String nameOrUUID) {
            this.nameOrUUID = nameOrUUID;
        }

        @Override
        public void run() {
            String uuid;
            if (PATTERN_VALID_USERNAME.matcher(nameOrUUID).matches()) {
                uuid = fetchUUID(nameOrUUID);
            } else if (PATTERN_VALID_UUID.matcher(nameOrUUID).matches()) {
                uuid = nameOrUUID;
            } else {
                plugin.getLogger().warning("Invalid skin requested: " + nameOrUUID);
                return;
            }

            if (uuid != null) {
                Skin skin = fetchSkin(uuid);
                if (skin != null) {
                    cache.put(nameOrUUID, skin);
                    fetchingSkins.remove(nameOrUUID);

                    // we received a new skin -> update tab to all players
                    BungeeTabListPlus.getInstance().resendTabLists();
                } else {
                    plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                        @Override
                        public void run() {
                            fetchingSkins.remove(nameOrUUID);
                        }
                    }, 1, TimeUnit.MINUTES);
                }
            }
        }
    }
}
