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
