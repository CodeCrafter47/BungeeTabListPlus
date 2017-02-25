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
import codecrafter47.bungeetablistplus.api.bungee.Icon;
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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class SkinManagerImpl implements SkinManager {

    private final Plugin plugin;
    private static final Gson gson = new Gson();

    private final Cache<String, Icon> cache = CacheBuilder.newBuilder().expireAfterAccess(35, TimeUnit.MINUTES).build();
    private final Map<File, Icon> fileSkinCache = new ConcurrentHashMap<>();
    private final Map<Head, Icon> headCache = new ConcurrentHashMap<>();

    private final Set<String> fetchingSkins = Sets.newConcurrentHashSet();

    private final File headsFolder;

    private final static Pattern PATTERN_VALID_USERNAME = Pattern.compile("(?:\\p{Alnum}|_){1,16}");
    private final static Pattern PATTERN_VALID_UUID = Pattern.compile("(?i)[a-f0-9]{8}-?[a-f0-9]{4}-?4[a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}");
    private final static Pattern PATTERN_OFFLINE_UUID = Pattern.compile("(?i)[a-f0-9]{8}-?[a-f0-9]{4}-?3[a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}");

    public SkinManagerImpl(Plugin plugin, File headsFolder) {
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
                                new Icon(null, new String[][]{{"textures", entry[1], entry[2]}})
                        ));
            } catch (Throwable th) {
                plugin.getLogger().log(Level.WARNING, "Failed to load heads/cache.txt", th);
            }
        }
    }

    @Override
    public Icon getIcon(String nameOrUUID) {
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

        Icon skin = cache.getIfPresent(nameOrUUID);
        if (skin != null) return skin;
        if (!fetchingSkins.contains(nameOrUUID)) {
            fetchingSkins.add(nameOrUUID);
            ProxyServer.getInstance().getScheduler().schedule(plugin, new SkinFetchTask(nameOrUUID), 0, TimeUnit.MILLISECONDS);
        }
        return null;
    }

    @SneakyThrows
    public Icon getSkin(File file) {
        Preconditions.checkNotNull(file, "file");

        Icon skin = fileSkinCache.get(file);
        if (skin != null) {
            return skin;
        }

        if (!file.getAbsolutePath().startsWith(headsFolder.getAbsolutePath())) {
            plugin.getLogger().warning("Requested head outside the heads folder: " + file.getAbsolutePath());
            fileSkinCache.put(file, missingSkinTexture);
            return missingSkinTexture;
        }

        // todo fileSkinCache.put(file, loadingSkinTexture);
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
                    Icon skin1 = headCache.get(Head.of(headArray));
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

        // todo
        return null;
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
                Icon skin = new Icon(null, new String[][]{{"textures", (String) map.get("skin"), (String) map.get("signature")}});
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
                writer.write(skin.getProperties()[0][1]);
                writer.write(' ');
                writer.write(skin.getProperties()[0][2]);
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
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> fileSkinCache.remove(file, missingSkinTexture), 30, TimeUnit.SECONDS);
        }
    }

    @Override
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

    private Icon fetchSkin(String uuid) {
        try {
            uuid = uuid.replace("-", "");
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").
                    openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), Charsets.UTF_8));
            SkinProfile skin = gson.fromJson(reader, SkinProfile.class);
            if (skin != null && skin.properties != null && !skin.properties.isEmpty()) {
                return new Icon(UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32)), new String[][]{{"textures", skin.properties.get(0).value, skin.properties.
                        get(0).signature}});
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

    @Override
    public void createIcon(BufferedImage image, Consumer<Icon> callback) {
        if (image.getWidth() != 8 || image.getHeight() != 8) {
            throw new IllegalArgumentException("Image must be 8x8 px.");
        }

        int[] rgb = image.getRGB(0, 0, 8, 8, null, 0, 8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(rgb.length * 4);
        byteBuffer.asIntBuffer().put(rgb);
        byte[] headArray = byteBuffer.array();

        if (headCache.containsKey(Head.of(headArray))) {
            Icon skin1 = headCache.get(Head.of(headArray));
            callback.accept(new Icon(skin1.getPlayer(), skin1.getProperties()));
            return;
        }

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
            if (map.get("state").equals("SUCCESS")) {
                Icon skin = new Icon(null, new String[][]{{"textures", (String) map.get("skin"), (String) map.get("signature")}});
                headCache.put(Head.of(headArray), skin);

                // save to cache
                File cacheFile = new File(headsFolder, "cache.txt");
                if (!cacheFile.exists()) {
                    cacheFile.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile, true));
                writer.write(Base64.getEncoder().encodeToString(headArray));
                writer.write(' ');
                writer.write(skin.getProperties()[0][1]);
                writer.write(' ');
                writer.write(skin.getProperties()[0][2]);
                writer.newLine();
                writer.close();

                callback.accept(new Icon(skin.getPlayer(), skin.getProperties()));
            } else {
                ProxyServer.getInstance().getScheduler().schedule(plugin, () -> createIcon(image, callback), 5, TimeUnit.SECONDS);
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "An error occurred while trying to contact skinservice.codecrafter47.dyndns.eu", ex);
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> createIcon(image, callback), 1, TimeUnit.MINUTES);
        }
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
                Icon skin = fetchSkin(uuid);
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
