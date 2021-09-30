package codecrafter47.bungeetablistplus.api.velocity;

import com.velocitypowered.api.proxy.Player;

public abstract class Variable {
    private final String name;
    
    public Variable(String name) {
        this.name = name;
    }
    
    public abstract String getReplacement(Player player);
    
    public final String getName() {
        return name;
    }
}
