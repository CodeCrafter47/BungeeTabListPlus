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

package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.config.components.Component;
import codecrafter47.bungeetablistplus.template.IconTemplate;
import codecrafter47.bungeetablistplus.template.TextTemplate;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Config implements ITabListConfig {

    private String type;

    private Expression showTo;

    private int priority;

    private boolean showHeaderFooter;

    private List<TextTemplate> header;

    private float headerAnimationUpdateInterval;

    private List<TextTemplate> footer;

    private float footerAnimationUpdateInterval;

    private Map<String, PlayerSet> playerSets;

    private int size;

    private IconTemplate defaultIcon;

    private int defaultPing;

    private List<Component> components;
}
