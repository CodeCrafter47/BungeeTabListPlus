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

package codecrafter47.bungeetablistplus.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.Executor;
import java.util.logging.*;

public class BugReportingService {
    private final Level level;
    private final String pluginName;
    private final String pluginVersion;
    private final Executor executor;
    private final BugReportFormatter formatter = new BugReportFormatter();

    public BugReportingService(Level level, String pluginName, String pluginVersion, Executor executor) {
        this.level = level;
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
        this.executor = executor;
    }

    public void registerLogger(Logger logger) {
        UploadBugReportHandler handler = new UploadBugReportHandler();
        handler.setLevel(level);
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        handler.setLevel(level);
        handler.setFormatter(formatter);
    }

    private class UploadBugReportHandler extends Handler {
        int reportedBugs = 0;
        @Override
        public void publish(LogRecord record) {
            if(record.getLevel().intValue() >= level.intValue()) {
                if (reportedBugs++ < 10) {
                    executor.execute(() -> {
                        try {
                            String url = String.format(
                                    "http://bugs.codecrafter47.dyndns.eu/?plugin=%s&version=%s&message=%s",
                                    URLEncoder.encode(pluginName, "UTF-8"),
                                    URLEncoder.encode(pluginVersion, "UTF-8"),
                                    URLEncoder.encode(getFormatter().format(record), "UTF-8"));
                            URLConnection connection = new URL(url).openConnection();
                            connection.connect();
                            connection.getInputStream().close();
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }

    private static class BugReportFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder formatted = new StringBuilder();

            formatted.append(formatMessage(record));
            formatted.append('\n');
            if (record.getThrown() != null) {
                StringWriter writer = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(writer));
                formatted.append(writer);
            }

            return formatted.toString();
        }
    }
}
