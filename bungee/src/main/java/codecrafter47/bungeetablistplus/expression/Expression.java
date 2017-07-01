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

import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.placeholder.Placeholder;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Expression {
    private static final Map<Token, Integer> OPERATORS = ImmutableMap.<Token, Integer>builder()
            .put(Token.CONCAT_STRING, 25)
            .put(Token.AND, 15)
            .put(Token.OR, 10)
            .put(Token.EQUAL, 5)
            .put(Token.NOT_EQUAL, 5)
            .put(Token.GREATER_THAN, 5)
            .put(Token.LESSER_THAN, 5)
            .put(Token.GREATER_OR_EQUAL_THAN, 5)
            .put(Token.LESSER_OR_EQUAL_THAN, 5)
            .build();

    private final Part root;

    public Expression(String expression) {
        ExpressionTokenizer tokenizer = new ExpressionTokenizer(expression);
        List<Token> tokens = new LinkedList<>();
        Token t;
        while (null != (t = tokenizer.nextToken())) {
            tokens.add(t);
        }
        try {
            root = parse(tokens);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error parsing expression \"" + expression + "\"", ex);
        }
        if (!tokens.isEmpty()) {
            throw new IllegalArgumentException("More closing than opening parenthesis in expression \"" + expression + "\"");
        }
    }

    private Part parse(List<Token> tokens) {
        List<Part> parts = new ArrayList<>();
        List<Token> operators = new ArrayList<>();

        parts.add(readPartToken(tokens));

        while (!tokens.isEmpty() && tokens.get(0) != Token.CLOSING_PARENTHESIS) {
            Token token = tokens.remove(0);
            if (!OPERATORS.containsKey(token)) {
                throw new IllegalArgumentException("Invalid expression syntax.");
            }
            operators.add(token);
            if (tokens.isEmpty()) {
                throw new IllegalArgumentException("Invalid expression syntax.");
            }
            parts.add(readPartToken(tokens));
        }
        while (!operators.isEmpty()) {
            int lowest = OPERATORS.get(operators.get(0));
            int start = 0;
            int end = 1;
            for (int i = 1; i < operators.size(); i++) {
                Token token = operators.get(i);
                if (OPERATORS.get(token) < lowest) {
                    lowest = OPERATORS.get(token);
                    start = i;
                    end = i + 1;
                } else if (OPERATORS.get(token) > lowest) {
                    break;
                } else {
                    end++;
                }
            }

            Part replacement;
            if (operators.get(start) == Token.CONCAT_STRING) {
                replacement = new ConcatStringPart(new ArrayList<>(parts.subList(start, end + 1)));
            } else if (operators.get(start) == Token.AND) {
                replacement = new AndPart(new ArrayList<>(parts.subList(start, end + 1)));
            } else if (operators.get(start) == Token.OR) {
                replacement = new OrPart(new ArrayList<>(parts.subList(start, end + 1)));
            } else if (start + 1 == end) {
                replacement = createBinaryOperatorPart(parts.get(start), parts.get(end), operators.get(start));
            } else {
                List<Part> conditions = new ArrayList<>(end - start);
                for (int i = start; i < end; i++) {
                    conditions.add(createBinaryOperatorPart(parts.get(i), parts.get(i + 1), operators.get(i)));
                }
                replacement = new AndPart(conditions);
            }
            for (int i = start; i < end; i++) {
                parts.remove(start);
                operators.remove(start);
            }
            parts.set(start, replacement);
        }
        return parts.get(0);
    }

    private LogicPart createBinaryOperatorPart(Part a, Part b, Token t) {
        if (t == Token.EQUAL) {
            return new EqualPart(a, b);
        } else if (t == Token.NOT_EQUAL) {
            return new NotEqualPart(a, b);
        } else if (t == Token.GREATER_THAN) {
            return new GreaterThanPart(a, b);
        } else if (t == Token.LESSER_THAN) {
            return new LesserThanPart(a, b);
        } else if (t == Token.GREATER_OR_EQUAL_THAN) {
            return new GreaterOrEqualThanPart(a, b);
        } else if (t == Token.LESSER_OR_EQUAL_THAN) {
            return new LesserOrEqualThanPart(a, b);
        } else {
            throw new IllegalArgumentException("Unknown binary operator: " + t);
        }
    }

    private Part readPartToken(List<Token> tokens) {
        Token token = tokens.remove(0);
        if (token instanceof BooleanToken) {
            return new BooleanPart(((BooleanToken) token).getValue());
        } else if (token instanceof NumberToken) {
            return new NumberPart(((NumberToken) token).getValue());
        } else if (token instanceof StringToken) {
            return new StringPart(((StringToken) token).getLiteral());
        } else if (token instanceof PlaceholderToken) {
            return new PlaceholderPart(((PlaceholderToken) token).getPlaceholder());
        } else if (token == Token.NEGATION) {
            return new NegatedPart(readPartToken(tokens));
        } else if (token == Token.OPENING_PARENTHESIS) {
            Part part = parse(tokens);
            if (tokens.isEmpty() || tokens.remove(0) != Token.CLOSING_PARENTHESIS) {
                throw new IllegalArgumentException("Invalid expression syntax.");
            }
            return part;
        } else {
            throw new IllegalArgumentException("Invalid expression syntax.");
        }
    }

    public <T> T evaluate(Context context, ExpressionResult<T> resultType) {
        return root.evaluate(context, resultType);
    }

    private static final NumberFormat format;

    static {
        format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setGroupingUsed(false);
    }

    private static double parseNumber(String text) {
        try {
            return format.parse(text).doubleValue();
        } catch (ParseException e) {
            return 0;
        }
    }

    private static abstract class Part {
        public abstract <T> T evaluate(Context context, ExpressionResult<T> resultType);
    }

    private abstract static class LiteralPart extends Part {
        @Nonnull
        @NonNull
        private final String text;
        private final double doubleResult;
        private final boolean booleanResult;

        protected LiteralPart(@Nonnull String text, double doubleResult, boolean booleanResult) {
            this.text = text;
            this.doubleResult = doubleResult;
            this.booleanResult = booleanResult;
        }

        @Override
        public <T> T evaluate(Context context, ExpressionResult<T> resultType) {
            if (resultType == ExpressionResult.STRING) {
                return (T) text;
            } else if (resultType == ExpressionResult.NUMBER) {
                return (T) (Double) doubleResult;
            } else if (resultType == ExpressionResult.BOOLEAN) {
                return (T) (Boolean) booleanResult;
            } else {
                throw new IllegalArgumentException("Unknown result type" + resultType);
            }
        }
    }

    private static class StringPart extends LiteralPart {

        private StringPart(@Nonnull String text) {
            super(text, parseNumber(text), Boolean.parseBoolean(text));
        }

    }

    private static class NumberPart extends LiteralPart {

        private NumberPart(double number) {
            super(((int) number) == number ? Integer.toString((int) number) : Double.toString(number), number, number != 0);
        }
    }

    private static class BooleanPart extends LiteralPart {

        private BooleanPart(boolean literal) {
            super(Boolean.toString(literal), literal ? 1 : 0, literal);
        }
    }

    private static class PlaceholderPart extends Part {
        private Placeholder placeholder;

        public PlaceholderPart(Placeholder p) {
            this.placeholder = p;
        }

        @Override
        public <T> T evaluate(Context context, ExpressionResult<T> resultType) {
            String result = placeholder.evaluate(context);
            if (resultType == ExpressionResult.STRING) {
                return (T) result;
            } else if (resultType == ExpressionResult.NUMBER) {
                return (T) (Double) parseNumber(result);
            } else if (resultType == ExpressionResult.BOOLEAN) {
                return (T) (Boolean) Boolean.parseBoolean(result);
            } else {
                throw new IllegalArgumentException("Unknown result type" + resultType);
            }
        }
    }

    private static abstract class LogicPart extends Part {
        @Override
        public <T> T evaluate(Context context, ExpressionResult<T> resultType) {
            if (resultType == ExpressionResult.STRING) {
                return (T) Boolean.toString(evaluate(context));
            } else if (resultType == ExpressionResult.NUMBER) {
                return (T) (Double) (evaluate(context) ? 1.0 : 0.0);
            } else if (resultType == ExpressionResult.BOOLEAN) {
                return (T) (Boolean) evaluate(context);
            } else {
                throw new IllegalArgumentException("Unknown result type" + resultType);
            }
        }

        protected abstract boolean evaluate(Context context);
    }

    private static class NegatedPart extends LogicPart {
        private final Part part;

        private NegatedPart(Part part) {
            this.part = part;
        }

        @Override
        protected boolean evaluate(Context context) {
            return !part.evaluate(context, ExpressionResult.BOOLEAN);
        }
    }

    private static class AndPart extends LogicPart {
        private final List<Part> parts;

        private AndPart(List<Part> parts) {
            this.parts = parts;
        }

        @Override
        protected boolean evaluate(Context context) {
            boolean res = true;
            for (int i = 0; res && i < parts.size(); i++) {
                Part part = parts.get(i);
                res = part.evaluate(context, ExpressionResult.BOOLEAN);
            }
            return res;
        }
    }

    private static class OrPart extends LogicPart {
        private final List<Part> parts;

        private OrPart(List<Part> parts) {
            this.parts = parts;
        }

        @Override
        protected boolean evaluate(Context context) {
            boolean res = false;
            for (int i = 0; !res && i < parts.size(); i++) {
                Part part = parts.get(i);
                res = part.evaluate(context, ExpressionResult.BOOLEAN);
            }
            return res;
        }
    }

    private static class EqualPart extends LogicPart {
        private final Part a;
        private final Part b;

        private EqualPart(Part a, Part b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected boolean evaluate(Context context) {
            return Objects.equals(a.evaluate(context, ExpressionResult.STRING), b.evaluate(context, ExpressionResult.STRING));
        }
    }

    private static class NotEqualPart extends LogicPart {
        private final Part a;
        private final Part b;

        private NotEqualPart(Part a, Part b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected boolean evaluate(Context context) {
            return !Objects.equals(a.evaluate(context, ExpressionResult.STRING), b.evaluate(context, ExpressionResult.STRING));
        }
    }

    private static class GreaterThanPart extends LogicPart {
        private final Part a;
        private final Part b;

        private GreaterThanPart(Part a, Part b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected boolean evaluate(Context context) {
            return a.evaluate(context, ExpressionResult.NUMBER) > b.evaluate(context, ExpressionResult.NUMBER);
        }
    }

    private static class LesserThanPart extends LogicPart {
        private final Part a;
        private final Part b;

        private LesserThanPart(Part a, Part b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected boolean evaluate(Context context) {
            return a.evaluate(context, ExpressionResult.NUMBER) < b.evaluate(context, ExpressionResult.NUMBER);
        }
    }

    private static class GreaterOrEqualThanPart extends LogicPart {
        private final Part a;
        private final Part b;

        private GreaterOrEqualThanPart(Part a, Part b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected boolean evaluate(Context context) {
            return a.evaluate(context, ExpressionResult.NUMBER) >= b.evaluate(context, ExpressionResult.NUMBER);
        }
    }

    private static class LesserOrEqualThanPart extends LogicPart {
        private final Part a;
        private final Part b;

        private LesserOrEqualThanPart(Part a, Part b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected boolean evaluate(Context context) {
            return a.evaluate(context, ExpressionResult.NUMBER) <= b.evaluate(context, ExpressionResult.NUMBER);
        }
    }

    private static class ConcatStringPart extends Part {
        private final List<Part> parts;

        private ConcatStringPart(List<Part> parts) {
            this.parts = parts;
        }

        @Override
        public <T> T evaluate(Context context, ExpressionResult<T> resultType) {
            String result = "";
            for (Part part : parts) {
                result += part.evaluate(context, ExpressionResult.STRING);
            }
            if (resultType == ExpressionResult.STRING) {
                return (T) result;
            } else if (resultType == ExpressionResult.NUMBER) {
                try {
                    return (T) (Double) Double.parseDouble(result);
                } catch (NumberFormatException ex) {
                    double length = result.length();
                    return (T) (Double) length;
                }
            } else if (resultType == ExpressionResult.BOOLEAN) {
                return (T) (Boolean) Boolean.parseBoolean(result);
            } else {
                throw new IllegalArgumentException("Unknown result type" + resultType);
            }
        }
    }
}
