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
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.beans.IntrospectionException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
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
            return new Yaml(new MyConstructor());
        }
    };

    public static <T> T read(InputStream is, Class<T> type) {
        return read(new UnicodeReader(is), type);
    }

    public static <T> T read(Reader reader, Class<T> type) {
        return yaml.get().loadAs(reader, type);
    }

    private static class MyConstructor extends Constructor {
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
                        ensureTypeDefinitionPresent(subtype.type());
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
                                        ensureTypeDefinitionPresent(subtype.type());
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
                ensureTypeDefinitionPresent(defaultType);
                node.setType(defaultType);
                node.setTag(new Tag(defaultType));
            }
            return super.constructObject(node);
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
    }
}
