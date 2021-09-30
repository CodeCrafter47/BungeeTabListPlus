package codecrafter47.bungeetablistplus.api.velocity;

import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CustomTabList {
    void setSize(int size);
    
    int getSize();
    
    int getRows();
    
    int getColumns();
    
    @Nonnull
    Icon getIcon(int row, int column);
    
    @Nonnull
    String getText(int row, int column);
    
    int getPing(int row, int column);
    
    void setSlot(int row, int column, @Nonnull @NonNull Icon icon,@Nonnull @NonNull String text, int ping);
    
    @Nullable
    String getHeader();
    
    void setHeader(@Nullable String header);
    
    @Nullable
    String getFooter();
    
    void setFooter(@Nullable String footer);
}
