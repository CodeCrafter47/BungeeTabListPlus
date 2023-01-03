/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.cache;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.util.ProxyServer;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cache implements Serializable {

    private transient File file;

    private Map<String, List<String>> cachedPAPIPrefixes = new HashMap<>();
    private Map<String, List<String>> cachedCustomPlaceholdersBridge = new HashMap<>();
    private Map<String, List<String>> cachedCustomServerPlaceholdersBridge = new HashMap<>();

    public synchronized void updatePAPIPrefixes(String server, List<String> prefixes) {
        cachedPAPIPrefixes.put(server, new ArrayList<>(prefixes));
    }

    public synchronized Set<String> getPAPIPrefixes() {
        return getServerNames()
                .map(cachedPAPIPrefixes::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private Stream<String> getServerNames() {
        for (int i = 0; i < 3; i++) {
            try {
                return ProxyServer.getInstance().getAllServers()
                        .stream()
                        .filter(Objects::nonNull).map(registeredServer -> registeredServer.getServerInfo().getName());
            } catch (Throwable ignored) {
            }
        }
        return Stream.empty();
    }

    public synchronized void updateCustomPlaceholdersBridge(String server, List<String> prefixes) {
        cachedCustomPlaceholdersBridge.put(server, new ArrayList<>(prefixes));
    }

    public synchronized Set<String> getCustomPlaceholdersBridge() {
        return getServerNames()
                .map(cachedCustomPlaceholdersBridge::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    public synchronized void updateCustomServerPlaceholdersBridge(String server, List<String> prefixes) {
        cachedCustomServerPlaceholdersBridge.put(server, new ArrayList<>(prefixes));
    }

    public synchronized Set<String> getCustomServerPlaceholdersBridge() {
        return getServerNames()
                .map(cachedCustomServerPlaceholdersBridge::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private Cache(File file) {
        this.file = file;
    }

    public static Cache load(File file) {
        try (FileInputStream is = new FileInputStream(file)) {
            ObjectInputStream ois = new ObjectInputStream(is);
            Cache cache = (Cache) ois.readObject();
            cache.file = file;
            return cache;
        } catch (Throwable th) {
            return new Cache(file);
        }
    }

    public synchronized void save() {
        try (FileOutputStream os = new FileOutputStream(file)) {
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(this);
            oos.flush();
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to write file: " + th.getMessage(), th);
        }
    }
}
