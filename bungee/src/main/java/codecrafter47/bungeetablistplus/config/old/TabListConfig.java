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

import java.util.ArrayList;
import java.util.List;

@ToString
public class TabListConfig implements ITabListConfig {

    @Getter
    private transient String name;

    public String showTo = "all";

    public int priority = 0;

    public List<String> header = new ArrayList<>();

    public double headerAnimationUpdateInterval = 1;

    public List<String> footer = new ArrayList<>();

    public double footerAnimationUpdateInterval = 1;

    public boolean shownFooterHeader = false;

    public String defaultSkin = "colors/dark_gray.png";

    public int defaultPing = 1000;

    public boolean autoShrinkTabList = false;

    public int tab_size = 80;

    public String groupPlayers = "SERVER";

    public boolean showEmptyGroups = false;

    public boolean verticalMode = false;

    public List<String> groupLines = new ArrayList<>();

    public List<String> playerLines = new ArrayList<>();

    public List<String> morePlayersLines = new ArrayList<>();

    public List<String> tabList = new ArrayList<>();
}
