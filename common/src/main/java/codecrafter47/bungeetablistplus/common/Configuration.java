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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Configuration {
    private static ThreadLocal<Yaml> yaml = ThreadLocal.withInitial(() -> {
        DumperOptions yamlOptions = new DumperOptions();
        yamlOptions.setIndent(2);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer yamlRepresenter = new Representer();
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        return new Yaml(yamlRepresenter, yamlOptions);
    });

    private List<String> header = Collections.emptyList();
    private PrintWriter writer = null;

    @SuppressWarnings("unchecked")
    public final void read(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);
        readHeader(bufferedReader);
        Map<Object, Object> map = (Map<Object, Object>) yaml.get().load(bufferedReader);
        for (Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Object, Object> entry = iterator.next();
            if (entry.getKey() == null || entry.getValue() == null) {
                iterator.remove();
            }
        }
        read(map);
        bufferedReader.close();
    }

    private void readHeader(BufferedReader reader) throws IOException {
        reader.mark(8192);
        header = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("#")) {
                header.add(line.trim().substring(1).trim());
            } else {
                break;
            }
        }
        reader.reset();
    }

    protected abstract void read(Map<Object, Object> map);

    public final void write(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        writer = new PrintWriter(streamWriter);
        writeHeader();
        write();
        writer.close();
    }

    protected void writeHeader() {
        writeComments(header.toArray(new String[header.size()]));
        writer.print('\n');
    }

    protected abstract void write();

    protected final void writeComment(String comment) {
        writer.println("# " + comment);
    }

    protected final void writeComments(String... comments) {
        for (String comment : comments) {
            writer.println("# " + comment);
        }
    }

    protected final void write(Object key, Object value) {
        writer.println(yaml.get().dumpAsMap(Collections.singletonMap(key, value)));
    }

    public void setHeader(String... header) {
        this.header = Arrays.asList(header);
    }

    protected boolean parseBoolean(Object object) {
        if (object instanceof Boolean) {
            return (boolean) object;
        } else if (object instanceof String) {
            return Boolean.parseBoolean((String) object);
        } else {
            throw new IllegalArgumentException(Objects.toString(object) + " is not a boolean value");
        }
    }

    protected double parseDouble(Object object) {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else if (object instanceof String) {
            return Double.parseDouble((String) object);
        } else {
            throw new IllegalArgumentException(Objects.toString(object) + " is not a number");
        }
    }

    protected int parseInteger(Object object) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        } else if (object instanceof String) {
            return Integer.parseInt((String) object);
        } else {
            throw new IllegalArgumentException(Objects.toString(object) + " is not a number");
        }
    }
}
