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

package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.config.components.*;
import codecrafter47.bungeetablistplus.config.old.TabListConfig;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class NewConfig {

    @SneakyThrows
    public static ITabListConfig read(InputStream is) {
        Yaml yaml = new Yaml(new MyConstructor());
        return yaml.loadAs(is, ITabListConfig.class);
    }

    private static class MyConstructor extends Constructor {

        public MyConstructor() {
            super();
            yamlConstructors.put(new Tag("!animated"), new ConstructClass(AnimatedComponent.class));
            yamlConstructors.put(new Tag("!conditional"), new ConstructClass(ConditionalComponent.class));
            yamlConstructors.put(new Tag("!table"), new ConstructClass(TableComponent.class));
            yamlConstructors.put(new Tag("!players_by_server"), new ConstructClass(PlayersByServerComponent.class));
            yamlConstructors.put(new Tag("!players"), new ConstructClass(PlayersComponent.class));
        }

        @Override
        protected Object constructObject(Node node) {
            if (node.getTag().matches(ITabListConfig.class) && node.getNodeId() == NodeId.mapping) {
                boolean set = false;
                for (NodeTuple tuple : ((MappingNode) node).getValue()) {
                    if (tuple.getKeyNode() instanceof ScalarNode) {
                        tuple.getKeyNode().setType(String.class);
                        String key = (String) constructObject(tuple.getKeyNode());
                        if ("type".equals(key)) {
                            if (tuple.getValueNode() instanceof ScalarNode) {
                                tuple.getValueNode().setType(String.class);
                                String value = (String) constructObject(tuple.getValueNode());
                                node.setType(Config.class);
                                node.setTag(new Tag(Config.class));
                                set = true;
                            }
                        }
                    }
                }
                if (!set) {
                    node.setType(TabListConfig.class);
                    node.setTag(new Tag(TabListConfig.class));
                }
            }
            if (node.getType() == Component.class) {
                if (node.getNodeId() == NodeId.mapping) {
                    node.setType(BasicComponent.class);
                }
                if (node.getNodeId() == NodeId.sequence) {
                    node.setType(List.class);
                    SequenceNode snode = (SequenceNode) node;
                    snode.setListType(Component.class);
                    @SuppressWarnings("unchecked")
                    List<Component> list = (List<Component>) super.constructObject(snode);
                    return new ListComponent(list);
                }
            }
            return super.constructObject(node);
        }

        private class ConstructClass extends ConstructMapping {
            private final Class aClass;

            private ConstructClass(Class aClass) {
                this.aClass = aClass;
            }

            @Override
            public Object construct(Node node) {
                node.setType(aClass);
                return super.construct(node);
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        InputStream stream = new FileInputStream(new File("bungee/src/main/resources/tablist2.yml"));
        /*BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while (null != (line = reader.readLine())) {
            System.out.println(line);
        }*/
        System.out.println(read(stream).toString());
    }
}
