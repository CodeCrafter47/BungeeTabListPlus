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

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Iterator;

public class YamlNode {

    private final MappingNode node;

    public YamlNode(MappingNode node) {
        this.node = node;
    }

    public Node get(String id) {
        for (NodeTuple tuple : node.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode) {
                String key = ((ScalarNode) tuple.getKeyNode()).getValue();
                if (id.equals(key)) {
                    return tuple.getValueNode();
                }
            }
        }
        return null;
    }

    public boolean contains(String id) {
        return get(id) != null;
    }

    public void remove(String id) {
        for (Iterator<NodeTuple> iterator = node.getValue().iterator(); iterator.hasNext(); ) {
            NodeTuple tuple = iterator.next();
            if (tuple.getKeyNode() instanceof ScalarNode) {
                String key = ((ScalarNode) tuple.getKeyNode()).getValue();
                if (id.equals(key)) {
                    iterator.remove();
                }
            }
        }
    }

    public void put(String id, Node value) {
        remove(id);
        node.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, id, null, null, null), value));
    }
}
