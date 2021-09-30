package codecrafter47.bungeetablistplus.api.velocity;

public abstract class ServerVariable {
    private final String name;
    
    public ServerVariable(String name) {
        this.name = name;
    }
    
    public abstract String getReplacement(String serverName);
    
    public final String getName() {
        return name;
    }
}
