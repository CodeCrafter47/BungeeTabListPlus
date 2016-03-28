package codecrafter47.bungeetablistplus.api.bungee;

/**
 *
 * @author James
 * 
 * mode = CustomPlugin
 */
public interface CustomGroupProvider {
    
    public String getMainGroup(IPlayer player);
    
    public String getPrefix(IPlayer player);
    
    public String getSuffix(IPlayer player);
}
