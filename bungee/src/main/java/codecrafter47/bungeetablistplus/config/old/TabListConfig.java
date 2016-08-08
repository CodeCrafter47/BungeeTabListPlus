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
package codecrafter47.bungeetablistplus.config.old;

import codecrafter47.bungeetablistplus.config.ITabListConfig;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
public class TabListConfig implements ITabListConfig {

    @Getter
    private transient String name;

    public String showTo;

    public int priority;

    public List<String> header;

    public double headerAnimationUpdateInterval;

    public List<String> footer;

    public double footerAnimationUpdateInterval;

    public boolean shownFooterHeader;

    public String defaultSkin;

    public int defaultPing;

    public boolean autoShrinkTabList;

    public int tab_size;

    public String groupPlayers;

    public boolean showEmptyGroups;

    public boolean verticalMode;

    public List<String> groupLines;

    public List<String> playerLines;

    public List<String> morePlayersLines;

    public List<String> tabList;
}
