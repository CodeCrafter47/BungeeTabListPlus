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
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class BugReportingService {
    private final Level level;
    private final String pluginName;
    private final String pluginVersion;
    private final Executor executor;
    private final String systemInfo;
    private final BugReportFormatter formatter = new BugReportFormatter();

    public BugReportingService(Level level, String pluginName, String pluginVersion, Executor executor, String systemInfo) {
        this.level = level;
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
        this.executor = executor;
        this.systemInfo = systemInfo;
    }

    public void registerLogger(Logger logger) {
        UploadBugReportHandler handler = new UploadBugReportHandler();
        handler.setLevel(level);
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        handler.setLevel(level);
        handler.setFormatter(formatter);
    }

    public void unregisterLogger(Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof UploadBugReportHandler) {
                logger.removeHandler(handler);
            }
        }
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
                                    URLEncoder.encode(getFormatter().format(record) + "\n\n" + systemInfo, "UTF-8"));
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
