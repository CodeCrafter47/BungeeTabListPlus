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

package codecrafter47.bungeetablistplus.yamlconfig;

import lombok.SneakyThrows;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class YamlConfig {
    private static final Set<Tag> noClass = new HashSet<Tag>() {{
        add(Tag.YAML);
        add(Tag.MERGE);
        add(Tag.SET);
        add(Tag.PAIRS);
        add(Tag.OMAP);
        add(Tag.BINARY);
        add(Tag.INT);
        add(Tag.FLOAT);
        add(Tag.TIMESTAMP);
        add(Tag.BOOL);
        add(Tag.NULL);
        add(Tag.STR);
        add(Tag.SEQ);
        add(Tag.MAP);
    }};

    private static final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>() {
        @Override
        protected Yaml initialValue() {
            MyConstructor constructor = new MyConstructor();
            constructor.setPropertyUtils(new MyPropertyUtils());
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            return new Yaml(constructor, new Representer(), dumperOptions);
        }
    };

    public static <T> T read(InputStream is, Class<T> type) {
        return read(new UnicodeReader(is), type);
    }

    public static <T> T read(Reader reader, Class<T> type) {
        return yaml.get().loadAs(reader, type);
    }

    public static void writeWithComments(Writer writer, Object object, String... header) throws IOException {
        for (String line : header) {
            writeCommentLine(writer, line);
        }

        Yaml yaml = YamlConfig.yaml.get();

        String ser = yaml.dumpAs(object, Tag.MAP, null);

        Map<String, String[]> comments = new HashMap<>();
        for (Class<?> c = object.getClass(); c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Comment comment = field.getAnnotation(Comment.class);
                if (comment != null) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                        if (Modifier.isPublic(modifiers)) {
                            Path path = field.getAnnotation(Path.class);
                            comments.put(path != null ? path.value() : field.getName(), comment.value());
                        }
                    }
                }
            }
        }

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(ser.split("\n")));

        ListIterator<String> iterator = lines.listIterator();

        while (iterator.hasNext()) {
            String line = iterator.next();
            for (Map.Entry<String, String[]> entry : comments.entrySet()) {
                if (line.startsWith(entry.getKey())) {
                    String[] value = entry.getValue();
                    iterator.previous();
                    iterator.add("");
                    for (String comment : value) {
                        iterator.add("# " + comment);
                    }
                    iterator.next();
                }
            }
        }

        for (String line : lines) {
            writer.write(line);
            writer.write("\n");
        }

        writer.close();
    }

    private static void writeCommentLine(Writer writer, String comment) throws IOException {
        writer.write("# " + comment + "\n");
    }

    private static class MyConstructor extends CustomClassLoaderConstructor {

        public MyConstructor() {
            super(YamlConfig.class.getClassLoader());
            yamlClassConstructors.put(NodeId.mapping, new ConstructMapping());
        }

        @Override
        @SneakyThrows
        protected Object constructObject(Node node) {
            Class<?> target = node.getType();
            if (target == Object.class && node.getTag().startsWith(Tag.PREFIX) && !noClass.contains(node.getTag())) {
                try {
                    target = Class.forName(node.getTag().getClassName());
                } catch (ClassNotFoundException ignored) {
                }
            }

            List<Method> methods = Arrays.stream(target.getMethods())
                    .filter(method -> Modifier.isStatic(method.getModifiers()))
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .filter(method -> method.getAnnotation(Factory.class) != null)
                    .collect(Collectors.toList());

            for (Method method : methods) {
                if (method.getParameterCount() == 1) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (node.getNodeId() == NodeId.sequence && Collection.class.isAssignableFrom(parameterTypes[0])) {
                        Type containedElementType = method.getGenericParameterTypes()[0];
                        node.setTag(Tag.SEQ);
                        node.setType(parameterTypes[0]);
                        ((SequenceNode) node).setListType(((Class<?>) ((ParameterizedType) containedElementType).getActualTypeArguments()[0]));
                        Object object = super.constructObject(node);
                        return method.invoke(null, object);
                    }
                }
            }


            Subtype[] subtypes = target.getAnnotationsByType(Subtype.class);

            Class<?> defaultType = null;

            TypeSelection:
            for (Subtype subtype : subtypes) {
                if (!subtype.tag().isEmpty()) {
                    if (subtype.tag().equals(node.getTag().getValue())) {
                        node.setType(subtype.type());
                        node.setTag(new Tag(subtype.type()));
                        defaultType = null;
                        break TypeSelection;
                    }
                } else if (!subtype.property().isEmpty()) {
                    for (NodeTuple tuple : ((MappingNode) node).getValue()) {
                        if (tuple.getKeyNode() instanceof ScalarNode) {
                            tuple.getKeyNode().setType(String.class);
                            String key = (String) constructObject(tuple.getKeyNode());
                            if (subtype.property().equals(key)) {
                                if (tuple.getValueNode() instanceof ScalarNode) {
                                    if (((ScalarNode) tuple.getValueNode()).getValue().equals(subtype.value())) {
                                        node.setType(subtype.type());
                                        node.setTag(new Tag(subtype.type()));
                                        defaultType = null;
                                        break TypeSelection;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    defaultType = subtype.type();
                }
            }
            if (defaultType != null) {
                node.setType(defaultType);
                node.setTag(new Tag(defaultType));
            }
            if (node.getNodeId() == NodeId.mapping && node.getType() != null) {
                ensureTypeDefinitionPresent(node.getType());
            }
            Object object = super.constructObject(node);
            if (object instanceof Validate) {
                try {
                    ((Validate) object).validate();
                } catch (Throwable th) {
                    throw new YamlValidationException(node, th);
                }
            }
            return object;
        }

        @Override
        protected void flattenMapping(MappingNode node) {
            if (node.isMerged()) {
                throw new IllegalStateException("Merging nodes is not supported in BTLP config files.");
            }
        }

        private void ensureTypeDefinitionPresent(Class<?> type) {
            if (!typeDefinitions.containsKey(type)) {
                addTypeDescription(computeTypeDescription(type));
            }
        }

        private TypeDescription computeTypeDescription(Class<?> clazz) {
            TypeDescription typeDescription = new TypeDescription(clazz, new Tag(clazz));
            Set<Property> properties = null;
            try {
                properties = getPropertyUtils().getProperties(clazz);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            if (properties != null) {
                for (Property property : properties) {
                    if (Collection.class.isAssignableFrom(property.getType()) || property.getClass().isArray()) {
                        Class<?>[] typeArguments = property.getActualTypeArguments();
                        if (typeArguments != null && typeArguments.length == 1 && typeArguments[0] != null) {
                            typeDescription.putListPropertyType(property.getName(), typeArguments[0]);
                        }
                    }
                    if (Map.class.isAssignableFrom(property.getType())) {
                        Class<?>[] typeArguments = property.getActualTypeArguments();
                        if (typeArguments != null && typeArguments.length == 2 && typeArguments[0] != null && typeArguments[1] != null) {
                            typeDescription.putMapPropertyType(property.getName(), typeArguments[0], typeArguments[1]);
                        }
                    }
                }
            }
            return typeDescription;
        }

        private class ConstructMapping extends Constructor.ConstructMapping {
            @Override
            protected Object createEmptyJavaBean(MappingNode node) {
                Object javaBean = super.createEmptyJavaBean(node);
                if (javaBean instanceof UpdatableConfig) {
                    ((UpdatableConfig) javaBean).update(node);
                }
                return javaBean;
            }
        }
    }

    private static class MyPropertyUtils extends PropertyUtils {
        @Override
        protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) throws IntrospectionException {
            Map<String, Property> newPropertyMap = new LinkedHashMap<>();
            Map<String, Property> propertiesMap = super.getPropertiesMap(type, bAccess);
            for (Iterator<Map.Entry<String, Property>> iterator = propertiesMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Property> entry = iterator.next();
                boolean updated = false;
                if (entry.getValue() instanceof FieldProperty) {
                    try {
                        Field field = type.getDeclaredField(entry.getValue().getName());
                        Path path = field.getAnnotation(Path.class);
                        if (path != null) {
                            newPropertyMap.put(path.value(), new CustomNameFieldProperty(field, path.value()));
                            updated = true;
                        }
                    } catch (NoSuchFieldException ignored) {
                    }
                }
                if (!updated) {
                    newPropertyMap.put(entry.getKey(), entry.getValue());
                }
            }

            return newPropertyMap;
        }

        @Override
        protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess)
                throws IntrospectionException {
            Set<Property> properties = new LinkedHashSet<>();
            Collection<Property> props = getPropertiesMap(type, bAccess).values();
            for (Property property : props) {
                if (property.isReadable() && property.isWritable()) {
                    properties.add(property);
                }
            }
            return properties;
        }
    }

    private static class CustomNameFieldProperty extends FieldProperty {

        private final String customName;

        public CustomNameFieldProperty(Field field, String customName) {
            super(field);
            this.customName = customName;
        }

        @Override
        public String getName() {
            return customName;
        }
    }

}
