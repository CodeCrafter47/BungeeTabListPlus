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
package codecrafter47.bungeetablistplus.updater;

import com.google.common.base.Charsets;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class UpdateChecker implements Runnable {

    private final Plugin plugin;

    private boolean updateAvailable = false;
    private boolean newDevBuildAvailable = false;

    public static final long interval = 120;

    private boolean error = false;

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (updateAvailable) {
            return;
        }
        if (error) {
            return;
        }
        try {
            InputStreamReader ir;
            URL url = new URL(
                    "http://updates.codecrafter47.dyndns.eu/" + plugin.
                            getDescription().getName() + "/version.txt");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            int res = con.getResponseCode();
            if (res != 200) {
                con.disconnect();
                return;
            }
            InputStream inputStream = con.getInputStream();
            ir = new InputStreamReader(inputStream, Charsets.UTF_8);

            BufferedReader input = new BufferedReader(ir);

            String newVersion = "";
            for (int i = 0; i < 10; i++) {
                String s = input.readLine();
                if (s == null) {
                    break;
                }
                if (!s.isEmpty() && s.length() > 2) {
                    newVersion = s;
                }
            }

            // compare versions
            String runningVersion = plugin.getDescription().getVersion();
            updateAvailable = compareVersions(newVersion, runningVersion);

            input.close();
            ir.close();
            inputStream.close();
            con.disconnect();

            if (updateAvailable) {
                plugin.getLogger().info("A new version of BungeeTabListPlus (" + newVersion + ") is available. Download from http://www.spigotmc.org/resources/bungeetablistplus.313/");
            }

            if (!updateAvailable && !newDevBuildAvailable && runningVersion.endsWith("-SNAPSHOT")) {
                // Check whether there is a new dev-build available
                try {
                    Properties current = new Properties();
                    current.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
                    String currentVersion = current.getProperty("build", "unknown");
                    if (!currentVersion.equals("unknown")) {
                        int buildNumber = Integer.valueOf(currentVersion);
                        Properties latest = new Properties();
                        latest.load(new URL("http://ci.codecrafter47.dyndns.eu/job/BungeeTabListPlus/lastSuccessfulBuild/artifact/bungee/target/classes/version.properties").openStream());
                        String latestVersion = latest.getProperty("build", "unknown");
                        if (!latestVersion.isEmpty() && !latestVersion.equals("unknown")) {
                            int latestBuildNumber = Integer.valueOf(latestVersion);
                            if (latestBuildNumber > buildNumber) {
                                newDevBuildAvailable = true;
                                plugin.getLogger().info("A new dev-build is available at http://ci.codecrafter47.dyndns.eu/job/BungeeTabListPlus/");
                            }
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable t) {
            error = true;
        }
    }

    static boolean compareVersions(String newVersion, String runningVersion) {
        boolean usesDevBuild = false;
        if (runningVersion.endsWith("-SNAPSHOT")) {
            usesDevBuild = true;
            runningVersion = runningVersion.replace("-SNAPSHOT", "");
        }

        String[] current = runningVersion.split("\\.");
        String[] latest = newVersion.split("\\.");

        int i = 0;
        boolean higher = false;
        boolean equal = true;
        for (; i < current.length && i < latest.length; i++) {
            if (Integer.valueOf(current[i]) < Integer.valueOf(latest[i])) {
                higher = true;
                break;
            } else if (Objects.equals(Integer.valueOf(current[i]), Integer.valueOf(latest[i]))) {
                equal = true;
            } else {
                equal = false;
                break;
            }
        }

        boolean updateAvailable = false;
        if (higher) {
            updateAvailable = true;
        }

        if (equal) {
            if (current.length < latest.length) {
                updateAvailable = true;
            } else if (current.length == latest.length && usesDevBuild) {
                updateAvailable = true;
            }
        }
        return updateAvailable;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public boolean isNewDevBuildAvailable() {
        return newDevBuildAvailable;
    }
}
