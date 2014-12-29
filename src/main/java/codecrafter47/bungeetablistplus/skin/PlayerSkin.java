package codecrafter47.bungeetablistplus.skin;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class PlayerSkin implements Skin {
    UUID player;
    String properties[];

    @Override
    public String[] toProperty() {
        return properties;
    }

    @Override
    public UUID getOwner() {
        return player;
    }
}
