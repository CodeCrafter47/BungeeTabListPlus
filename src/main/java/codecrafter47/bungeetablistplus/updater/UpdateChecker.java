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
package codecrafter47.bungeetablistplus.updater;

import com.google.common.base.Charsets;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @author Florian Stober
 */
public class UpdateChecker implements Runnable {

    private final Plugin plugin;

    private boolean updateAvailable = false;

    private final long intervall = 120;

    private int pid = -1;

    private boolean error = false;

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
        enable();
    }

    private void enable() {
        plugin.getLogger().info("Starting UpdateChecker Task");
        pid = plugin.getProxy().getScheduler().schedule(plugin, this, 0,
                intervall, TimeUnit.MINUTES).getId();
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

            if (!newVersion.equalsIgnoreCase(plugin.getDescription().
                    getVersion())) {
                updateAvailable = true;

            }

            input.close();
            ir.close();
            inputStream.close();
            con.disconnect();
        } catch (Throwable t) {
            error = true;
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
}
