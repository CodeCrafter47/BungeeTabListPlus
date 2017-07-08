/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.config.components;

import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.tablist.component.ComponentTablistAccess;
import codecrafter47.bungeetablistplus.template.IconTemplate;
import codecrafter47.bungeetablistplus.template.PingTemplate;
import codecrafter47.bungeetablistplus.template.TextTemplate;
import codecrafter47.bungeetablistplus.util.FastChat;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import codecrafter47.util.chat.ChatUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BasicComponent extends Component implements Validate {

    private TextTemplate text = TextTemplate.EMPTY;
    private IconTemplate icon = IconTemplate.DEFAULT_ICON;
    private PingTemplate ping = PingTemplate.DEFAULT_PING;
    private Alignment alignment = Alignment.LEFT;
    private LongTextBehaviour longText = null;

    public BasicComponent(String text) {
        this.text = new TextTemplate(text);
    }

    @Override
    public boolean hasConstantSize() {
        return true;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public Instance toInstance(Context context) {
        return new Instance(context);
    }

    @Override
    public void validate() {
        Preconditions.checkNotNull(text, "text is null");
        Preconditions.checkNotNull(icon, "icon is null");
        Preconditions.checkNotNull(ping, "ping is null");
        Preconditions.checkNotNull(alignment, "alignment is null");
    }

    public class Instance extends Component.Instance {

        public Instance(Context context) {
            super(context);
        }

        @Override
        public void update2ndStep() {
            super.update2ndStep();

            ComponentTablistAccess cta = getTablistAccess();
            if (cta != null) {

                CustomTablist tablist = context.get(Context.KEY_TAB_LIST);
                String text = getText().evaluate(context);

                // get long text behaviour
                LongTextBehaviour longTextBehaviour = BasicComponent.this.longText;
                if (longTextBehaviour == null) {
                    longTextBehaviour = context.get(Context.KEY_DEFAULT_LONG_TEXT_BEHAVIOUR);
                }

                // handle alignment
                if (alignment != Alignment.LEFT || longTextBehaviour != LongTextBehaviour.DISPLAY_ALL) {
                    int slotWidth = getSlotWidth(tablist);
                    int textLength = FastChat.legacyTextLength(text, '&');

                    if (longTextBehaviour != LongTextBehaviour.DISPLAY_ALL && textLength > slotWidth) {
                        String suffix = "";
                        if (longTextBehaviour == LongTextBehaviour.CROP_2DOTS) {
                            suffix = "..";
                        } else if (longTextBehaviour == LongTextBehaviour.CROP_3DOTS) {
                            suffix = "...";
                        }
                        int suffixLength = FastChat.legacyTextLength(suffix, '&');
                        text = FastChat.cropLegacyText(text, '&', slotWidth - suffixLength) + suffix;
                        textLength = slotWidth;
                    }

                    int space = slotWidth - textLength;
                    if (alignment != Alignment.LEFT && space > 0) {
                        int spaces = (int) (space / ChatUtil.getCharWidth(' ', false));
                        int spacesBefore = spaces;
                        int spacesBehind = 0;
                        if (alignment == Alignment.CENTER) {
                            spacesBefore = spaces >> 1;
                            spacesBehind = spaces - spacesBefore;
                        }
                        text = Strings.repeat(" ", spacesBefore) + text + "&r" + Strings.repeat(" ", spacesBehind);
                    }
                }

                // update the tab list
                cta.setSlot(0, getIcon().evaluate(context), text, getPing().evaluate(context));
            }
        }

        private int getSlotWidth(CustomTablist tablist) {
            int slotWidth = 80;
            if (tablist.getSize() <= 60) {
                slotWidth = 110;
            } else if (tablist.getSize() <= 40) {
                slotWidth = 180;
            } else if (tablist.getSize() <= 20) {
                slotWidth = 360;
            }
            return slotWidth;
        }

        @Override
        public int getMinSize() {
            return 1;
        }

        @Override
        public int getPreferredSize() {
            return 1;
        }

        @Override
        public int getMaxSize() {
            return 1;
        }

        @Override
        public boolean isBlockAligned() {
            return false;
        }
    }

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    public enum LongTextBehaviour {
        DISPLAY_ALL, CROP, CROP_2DOTS, CROP_3DOTS;
    }
}
