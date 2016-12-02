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

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class ExpressionTokenizer {
    private static final Pattern PATTERN_SPACE = Pattern.compile("\\s");
    private static final Pattern PATTERN_NUMBER = Pattern.compile("[-0-9.]");
    private static final ImmutableMap<String, Token> tokens3 = ImmutableMap.<String, Token>builder()
            .put("all", BooleanToken.TRUE)
            .put("and", Token.AND).build();
    private static final ImmutableMap<String, Token> tokens2 = ImmutableMap.<String, Token>builder()
            .put("&&", Token.AND)
            .put("||", Token.OR)
            .put("or", Token.OR)
            .put("==", Token.EQUAL)
            .put("!=", Token.NOT_EQUAL)
            .put(">=", Token.GREATER_OR_EQUAL_THAN)
            .put("<=", Token.LESSER_OR_EQUAL_THAN).build();
    private static final ImmutableMap<String, Token> tokens1 = ImmutableMap.<String, Token>builder()
            .put("(", Token.OPENING_PARENTHESIS)
            .put(")", Token.CLOSING_PARENTHESIS)
            .put("!", Token.NEGATION)
            .put(">", Token.GREATER_THAN)
            .put("<", Token.LESSER_THAN)
            .put(".", Token.CONCAT_STRING).build();

    private final String expression;

    private int index = 0;

    public ExpressionTokenizer(@Nonnull String expression) {
        this.expression = expression;
    }

    @Nullable
    public Token nextToken() {
        // skip spaces
        while (index < expression.length() && PATTERN_SPACE.matcher(expression.substring(index, index + 1)).matches()) {
            index += 1;
        }

        if (index >= expression.length()) {
            return null;
        }

        // identify next token
        if (index + 4 < expression.length() && "false".equals(expression.substring(index, index + 5))) {
            index += 5;
            return new BooleanToken(false);
        }

        if (index + 3 < expression.length() && "true".equals(expression.substring(index, index + 4))) {
            index += 4;
            return new BooleanToken(true);
        }

        if (index + 2 < expression.length()) {
            Token token = tokens3.get(expression.substring(index, index + 3));
            if (token != null) {
                index += 3;
                return token;
            }
        }

        if (index + 1 < expression.length()) {
            Token token = tokens2.get(expression.substring(index, index + 2));
            if (token != null) {
                index += 2;
                return token;
            }
        }

        if (index < expression.length()) {
            Token token = tokens1.get(expression.substring(index, index + 1));
            if (token != null) {
                index += 1;
                return token;
            }
        }

        if (index + 1 < expression.length() && expression.charAt(index) == '$' && expression.charAt(index + 1) == '{') {
            int startIndex = index;

            // search for closing parenthesis
            while (index < expression.length() && '}' != expression.charAt(index)) {
                index += 1;
            }

            if (index >= expression.length()) {
                throw new IllegalArgumentException(format("Incomplete placeholder starting at index %d in \"%s\"", startIndex, expression));
            }

            index += 1;

            return new PlaceholderToken(expression.substring(startIndex, index));
        }

        if (index < expression.length() && expression.charAt(index) == '"') {
            int startIndex = index;

            while (++index < expression.length() && '"' != expression.charAt(index)) ;

            if (index >= expression.length()) {
                throw new IllegalArgumentException(format("Incomplete string literal starting at index %d in \"%s\"", startIndex, expression));
            }

            index += 1;

            return new StringToken(expression.substring(startIndex, index));
        }

        if (index < expression.length() && expression.charAt(index) == '\'') {
            int startIndex = index;

            while (++index < expression.length() && '\'' != expression.charAt(index)) ;

            if (index >= expression.length()) {
                throw new IllegalArgumentException(format("Incomplete string literal starting at index %d in \"%s\"", startIndex, expression));
            }

            index += 1;

            return new StringToken(expression.substring(startIndex, index));
        }

        if (index < expression.length() && PATTERN_NUMBER.matcher(expression.substring(index, index + 1)).matches()) {
            int startIndex = index;

            while (index < expression.length() && PATTERN_NUMBER.matcher(expression.substring(index, index + 1)).matches()) {
                index += 1;
            }

            return new NumberToken(Double.valueOf(expression.substring(startIndex, index)));
        }

        throw new IllegalArgumentException(format("Illegal token '%c' at index %d in \"%s\"", expression.charAt(index), index, expression));
    }
}
