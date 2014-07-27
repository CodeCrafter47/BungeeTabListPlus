/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;
import net.md_5.bungee.api.plugin.Plugin;

/**
 *
 * @author florian
 */
public class UpdateChecker implements Runnable {

    private final Plugin plugin;

    private boolean updateAvailable = false;

    private final long intervall = 15;

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
                return;
            }
            ir = new InputStreamReader(con.getInputStream());

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
        } catch (Throwable t) {
            error = true;
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
}
