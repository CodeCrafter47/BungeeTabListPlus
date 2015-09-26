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

package codecrafter47.bungeetablistplus.api.bungee.placeholder;

import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A placeholder
 * <p>
 * This class should not be used directly. Use PlaceHolderProvider instead.
 */
public abstract class Placeholder {
    private final String regex;

    /**
     * @param regex the regular expression this placeholder should capture
     */
    public Placeholder(String regex) {
        this.regex = regex;
        // validate the regular expression
        Pattern.compile(regex);
    }

    /**
     * Get the SlotTemplate which should replace the PlaceHolder
     * <p>
     * This method is invoke when finding the PlaceHolder in the config.
     * Actually resolving the placeholder should be done in the SlotTemplate you return.
     *
     * @param placeholderManager an instance of the PlaceholderManager
     * @param matcher            the Matcher used to match your regular expression
     * @return a SlotTemplate which replaces the placeholder
     */
    public abstract SlotTemplate getReplacement(PlaceholderManager placeholderManager, Matcher matcher);

    /**
     * Get the regular expression encompassing all placeholder strings this Placeholder replaces
     *
     * @return the regular expression
     */
    public final String getRegex() {
        return regex;
    }
}
