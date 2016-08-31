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
import codecrafter47.bungeetablistplus.template.IconTemplate;
import codecrafter47.bungeetablistplus.template.PingTemplate;
import codecrafter47.bungeetablistplus.template.TextTemplate;
import codecrafter47.bungeetablistplus.util.FastChat;
import codecrafter47.util.chat.ChatUtil;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicComponent extends Component {

    private TextTemplate text;
    private IconTemplate icon;
    private PingTemplate ping;
    private Alignment alignment = Alignment.LEFT;

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

    public class Instance extends Component.Instance {

        public Instance(Context context) {
            super(context);
        }

        @Override
        public void update2ndStep() {
            super.update2ndStep();
            CustomTablist tablist = context.getTablist();
            String text = getText().evaluate(context);

            if (alignment != Alignment.LEFT) {
                int slotWidth = 80;
                if (tablist.getSize() <= 60) {
                    slotWidth = 110;
                } else if (tablist.getSize() <= 40) {
                    slotWidth = 180;
                } else if (tablist.getSize() <= 20) {
                    slotWidth = 360;
                }
                int textLength = FastChat.legacyTextLength(text, '&');
                int space = slotWidth - textLength;
                if (space > 0) {
                    int spaces = (int) (space / ChatUtil.getCharWidth(' ', false));
                    if (alignment == Alignment.CENTER) {
                        spaces >>= 1;
                    }
                    text = Strings.repeat(" ", spaces) + text;
                }
            }

            tablist.setSlot(row, column, getIcon().evaluate(context), text, getPing().evaluate(context));
        }

        @Override
        public int getMinSize() {
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
}
