/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
import codecrafter47.bungeetablistplus.skin.Skin;
import codecrafter47.bungeetablistplus.skin.PlayerSkin;
import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SkinManager {

    private final BungeeTabListPlus plugin;
    private static final Gson gson = new Gson();

    private final Cache<String, Skin> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
    public static final Skin defaultSkin = new PlayerSkin(UUID.randomUUID(), null);

    private final Set<String> fetchingSkins = Sets.newConcurrentHashSet();

    public SkinManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    @SneakyThrows
    public Skin getSkin(String nameOrUUID) {
        Skin skin = cache.getIfPresent(nameOrUUID);
        if (skin != null) return skin;
        if (!fetchingSkins.contains(nameOrUUID)) {
            fetchingSkins.add(nameOrUUID);
            plugin.getProxy().getScheduler().schedule(plugin, new SkinFetchTask(nameOrUUID), 0, TimeUnit.MILLISECONDS);
        }
        return defaultSkin;
    }

    private String fetchUUID(String player) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
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
            return null;
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            plugin.reportError(e);
        }
        return null;

    }

    private Skin fetchSkin(final String uuid) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").
                    openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), Charsets.UTF_8));
            SkinProfile skin = gson.fromJson(reader, SkinProfile.class);
            return new PlayerSkin(UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32)), new String[]{"textures", skin.properties.get(0).value, skin.properties.
                    get(0).signature});
        } catch (Exception ignored) {
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

        List<Property> properties = new ArrayList<>();

        private static class Property {

            private String name, value, signature;
        }
    }

    private class SkinFetchTask implements Runnable {

        String nameOrUUID;

        public SkinFetchTask(String nameOrUUID) {
            this.nameOrUUID = nameOrUUID;
        }

        @Override
        public void run() {
            String uuid;
            if (nameOrUUID.length() < 17) {
                uuid = fetchUUID(nameOrUUID);
            } else {
                uuid = nameOrUUID;
            }

            if (uuid != null) {
                Skin skin = fetchSkin(uuid);
                if (skin != null) {
                    cache.put(nameOrUUID, skin);
                    fetchingSkins.remove(nameOrUUID);
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
