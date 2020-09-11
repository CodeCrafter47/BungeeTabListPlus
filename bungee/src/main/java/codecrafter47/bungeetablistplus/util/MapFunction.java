/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.util;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.function.Function;

@EqualsAndHashCode
public class MapFunction implements Function<String, String> {

    private final Map<String, String> map;

    public MapFunction(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public String apply(String s) {
        return map.getOrDefault(s, s);
    }
}
