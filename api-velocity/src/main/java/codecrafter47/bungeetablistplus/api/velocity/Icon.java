package codecrafter47.bungeetablistplus.api.velocity;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.UUID;

@Data
public class Icon implements Serializable {
    
    private static final long serialVersionID = 1L;
    
    private final UUID player;
    @NonNull
    private final String[][] properties;
    
    public static final Icon DEFAULT = new Icon(null, new String[0][]);
}
