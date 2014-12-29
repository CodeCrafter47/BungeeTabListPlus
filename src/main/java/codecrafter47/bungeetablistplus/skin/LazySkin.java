package codecrafter47.bungeetablistplus.skin;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;

import java.util.UUID;

public class LazySkin implements Skin {

    private final String nameOrUUID;

    public LazySkin(String nameOrUUID) {
        this.nameOrUUID = nameOrUUID;
    }

    @Override
    public String[] toProperty() {
        return BungeeTabListPlus.getInstance().getSkinManager().getSkin(nameOrUUID).toProperty();
    }

    @Override
    public UUID getOwner() {
        return BungeeTabListPlus.getInstance().getSkinManager().getSkin(nameOrUUID).getOwner();
    }
}
