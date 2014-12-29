package codecrafter47.bungeetablistplus.api;

import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.skin.Skin;
import lombok.Getter;
import lombok.Setter;

public class Slot {

    @Getter
    @Setter
    public String text;

    @Getter
    public int ping;

    @Getter
    @Setter
    private Skin skin = SkinManager.defaultSkin;

    public Slot(String text, int ping) {
        super();
        this.text = text;
        if (ping < 0) {
            this.ping = -1;
        } else if (ping < 150) {
            this.ping = 0;
        } else if (ping < 300) {
            this.ping = 150;
        } else if (ping < 600) {
            this.ping = 300;
        } else if (ping < 1000) {
            this.ping = 600;
        } else {
            this.ping = 1000;
        }
    }

    public void setPing(int ping) {
        if (ping < 0) {
            this.ping = -1;
        } else if (ping < 150) {
            this.ping = 0;
        } else if (ping < 300) {
            this.ping = 150;
        } else if (ping < 600) {
            this.ping = 300;
        } else if (ping < 1000) {
            this.ping = 600;
        } else {
            this.ping = 1000;
        }
    }

    public Slot(String text) {
        super();
        this.text = text;
        this.ping = 0;
    }

    public Slot(Slot s) {
        this.ping = s.ping;
        this.skin = s.skin;
        this.text = s.text;
    }
}
