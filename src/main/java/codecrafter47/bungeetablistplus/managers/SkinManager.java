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
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SkinManager {

    private final BungeeTabListPlus plugin;
    private static final Gson gson = new Gson();

    private final Map<String, String[]> cache = new ConcurrentHashMap<>();

    private final List<String> skinsToFetch = new ArrayList<>();

    private final List<String> badNames = new ArrayList<>();

    public SkinManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
        plugin.getProxy().getScheduler().schedule(plugin, new SkinFetchTask(),
                1, TimeUnit.SECONDS);
    }

    public String[] getSkin(String nameOrUUID) {
        if (cache.containsKey(nameOrUUID)) {
            return cache.get(nameOrUUID);
        }
        synchronized (skinsToFetch) {
            if (!skinsToFetch.contains(nameOrUUID) && !badNames.contains(
                    nameOrUUID)) {
                skinsToFetch.add(nameOrUUID);
            }
        }
        return null;
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
                out.write(("[\"" + player + "\"]").getBytes());
                out.flush();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
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

    private String[] fetchSkin(final String uuid) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").
                    openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            SkinProfile skin = gson.fromJson(reader, SkinProfile.class);
            return new String[]{skin.properties.get(0).value, skin.properties.
                get(0).signature};
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            if(e.getMessage().contains("429")){
                // mojang rate limit
                // try again in 1 min
                plugin.getProxy().getScheduler().schedule(plugin, new Runnable(){

                    @Override
                    public void run() {
                        badNames.remove(uuid);
                        skinsToFetch.add(uuid);
                    }
                    
                }, 1, TimeUnit.MINUTES);
                return null;
            }
            plugin.reportError(e);
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

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    // ignore that
                }
                String next = null;
                synchronized (skinsToFetch) {
                    if (!skinsToFetch.isEmpty()) {
                        next = skinsToFetch.get(0);
                    }
                }
                if (next == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        // ignore that
                    }
                    continue;
                }
                String uuid;
                if (next.length() < 17) {
                    uuid = fetchUUID(next);
                } else {
                    uuid = next;
                }

                if (uuid != null) {
                    String[] skin = fetchSkin(uuid);
                    if (skin != null) {
                        cache.put(next, skin);
                    } else {
                        synchronized (skinsToFetch) {
                            badNames.add(next);
                        }
                    }
                } else {
                    synchronized (skinsToFetch) {
                        badNames.add(next);
                    }
                }

                synchronized (skinsToFetch) {
                    skinsToFetch.remove(next);
                }
            }

        }
    }
}
