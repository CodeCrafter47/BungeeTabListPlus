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

package codecrafter47.bungeetablistplus.expression;

import lombok.NonNull;

import javax.annotation.Nonnull;

public class Token {
    public static final Token AND = new Token("&&");
    public static final Token OR = new Token("||");
    public static final Token OPENING_PARENTHESIS = new Token("(");
    public static final Token CLOSING_PARENTHESIS = new Token(")");
    public static final Token EQUAL = new Token("==");
    public static final Token NOT_EQUAL = new Token("!=");
    public static final Token NEGATION = new Token("!");
    public static final Token GREATER_THAN = new Token(">");
    public static final Token LESSER_THAN = new Token("<");
    public static final Token GREATER_OR_EQUAL_THAN = new Token(">=");
    public static final Token LESSER_OR_EQUAL_THAN = new Token("<=");

    private final String value;

    public Token(@Nonnull @NonNull String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
