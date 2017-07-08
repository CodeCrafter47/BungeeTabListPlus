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

import codecrafter47.bungeetablistplus.config.components.BasicComponent;
import codecrafter47.bungeetablistplus.expression.Expression;
import codecrafter47.bungeetablistplus.template.TextTemplate;
import codecrafter47.bungeetablistplus.yamlconfig.Validate;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Config implements ITabListConfig, Validate {

    private String type;

    private Expression showTo = new Expression("all");

    private int priority = 0;

    private boolean showHeaderFooter = false;

    private List<TextTemplate> header;

    private float headerAnimationUpdateInterval;

    private List<TextTemplate> footer;

    private float footerAnimationUpdateInterval;

    private Map<String, CustomPlaceholder> customPlaceholders = new HashMap<>();

    private Map<String, PlayerSet> playerSets;

    private BasicComponent.LongTextBehaviour longText = BasicComponent.LongTextBehaviour.DISPLAY_ALL;

    @Override
    public void validate() {
        Preconditions.checkNotNull(showTo, "showTo is null");
        if (showHeaderFooter) {
            Preconditions.checkNotNull(header, "header is null");
            Preconditions.checkArgument(!header.isEmpty(), "header is empty");
            Preconditions.checkArgument(headerAnimationUpdateInterval > 0, "headerAnimationUpdateInterval is negative");
            Preconditions.checkNotNull(footer, "footer is null");
            Preconditions.checkArgument(!footer.isEmpty(), "footer is empty");
            Preconditions.checkArgument(footerAnimationUpdateInterval > 0, "footerAnimationUpdateInterval is negative");
        }
        Preconditions.checkNotNull(customPlaceholders, "customPlaceholders is null");
        Preconditions.checkNotNull(playerSets, "playerSets is null");
        Preconditions.checkNotNull(longText, "longText is null");
        for (Map.Entry<String, PlayerSet> entry : playerSets.entrySet()) {
            Preconditions.checkNotNull(entry.getKey(), "playerSet with null key");
            Preconditions.checkNotNull(entry.getValue(), "playerSet definition for " + entry.getKey() + " is incomplete");
            Preconditions.checkNotNull(entry.getValue().getFilter(), "playerSet definition for " + entry.getKey() + " is missing the filter option");
            Preconditions.checkNotNull(entry.getValue().getHiddenPlayers(), "playerSet definition for " + entry.getKey() + " is missing the hiddenPlayers option");
        }

    }
}
