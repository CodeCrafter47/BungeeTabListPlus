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

package codecrafter47.bungeetablistplus.template;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.yamlconfig.Subtype;

import java.util.function.Function;

@Subtype(type = IconTemplate.ConfigIconTemplate.class)
public abstract class IconTemplate {

    public abstract Icon evaluate(Context context);

    public static class ConfigIconTemplate extends IconTemplate {

        private Function<Context, Icon> getIcon;

        public ConfigIconTemplate(String text) {
            if (text.equals("${player skin}")) {
                getIcon = context -> {
                    Player player = context.get(Context.KEY_PLAYER);
                    if (player != null) {
                        return player.getOpt(BTLPBungeeDataKeys.DATA_KEY_ICON).orElse(Icon.DEFAULT);
                    } else {
                        return Icon.DEFAULT;
                    }
                };
            } else if (text.equals("${viewer skin}")) {
                getIcon = context -> {
                    Player player = context.get(Context.KEY_VIEWER);
                    if (player != null) {
                        return player.getOpt(BTLPBungeeDataKeys.DATA_KEY_ICON).orElse(Icon.DEFAULT);
                    } else {
                        return Icon.DEFAULT;
                    }
                };
            } else {
                getIcon = context -> {
                    Icon icon = BungeeTabListPlus.getInstance().getSkinManager().getIcon(text);
                    return icon != null ? icon : Icon.DEFAULT;
                };
            }
        }

        @Override
        public Icon evaluate(Context context) {
            return getIcon.apply(context);
        }
    }

    public static final IconTemplate PLAYER_ICON = new IconTemplate() {
        @Override
        public Icon evaluate(Context context) {
            return context.get(Context.KEY_PLAYER).getOpt(BTLPBungeeDataKeys.DATA_KEY_ICON).orElse(Icon.DEFAULT);
        }
    };

    public static final IconTemplate DEFAULT_ICON = new IconTemplate() {
        @Override
        public Icon evaluate(Context context) {
            return context.get(Context.KEY_DEFAULT_ICON).evaluate(context);
        }
    };
}
