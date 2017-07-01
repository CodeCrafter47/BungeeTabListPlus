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

import org.junit.Assert;
import org.junit.Test;

public class ExpressionTest {

    private static void assertTrue(String expr) {
        Assert.assertTrue(new Expression(expr).evaluate(null, ExpressionResult.BOOLEAN));
    }

    private static void assertFalse(String expr) {
        Assert.assertFalse(new Expression(expr).evaluate(null, ExpressionResult.BOOLEAN));
    }

    @Test
    public void testExpressions() {
        Expression expression = new Expression("4");
        Assert.assertTrue(expression.evaluate(null, ExpressionResult.BOOLEAN));
        Assert.assertEquals(4, expression.evaluate(null, ExpressionResult.NUMBER), 0.001);
        Assert.assertEquals("4", expression.evaluate(null, ExpressionResult.STRING));

        expression = new Expression("\"four\"");
        Assert.assertFalse(expression.evaluate(null, ExpressionResult.BOOLEAN));
        Assert.assertEquals(0, expression.evaluate(null, ExpressionResult.NUMBER), 0.001);
        Assert.assertEquals("four", expression.evaluate(null, ExpressionResult.STRING));

        expression = new Expression("\"true\"");
        Assert.assertTrue(expression.evaluate(null, ExpressionResult.BOOLEAN));
        Assert.assertEquals(0, expression.evaluate(null, ExpressionResult.NUMBER), 0.001);
        Assert.assertEquals("true", expression.evaluate(null, ExpressionResult.STRING));

        expression = new Expression("true");
        Assert.assertTrue(expression.evaluate(null, ExpressionResult.BOOLEAN));
        Assert.assertEquals(1, expression.evaluate(null, ExpressionResult.NUMBER), 0.001);
        Assert.assertEquals("true", expression.evaluate(null, ExpressionResult.STRING));

        expression = new Expression("all");
        Assert.assertTrue(expression.evaluate(null, ExpressionResult.BOOLEAN));
        Assert.assertEquals(1, expression.evaluate(null, ExpressionResult.NUMBER), 0.001);
        Assert.assertEquals("true", expression.evaluate(null, ExpressionResult.STRING));

        expression = new Expression("false");
        Assert.assertFalse(expression.evaluate(null, ExpressionResult.BOOLEAN));
        Assert.assertEquals(0, expression.evaluate(null, ExpressionResult.NUMBER), 0.001);
        Assert.assertEquals("false", expression.evaluate(null, ExpressionResult.STRING));

        expression = new Expression("\"A\" . \"B\"");
        Assert.assertEquals("AB", expression.evaluate(null, ExpressionResult.STRING));
    }

    @Test
    public void testBooleanExpressions() {
        assertTrue("1 == 1");
        assertTrue("\"test\" == \"test\"");
        assertFalse("\"test\" != \"test\"");
        assertFalse("\"test\" == \"abc\"");
        assertTrue("\"test\" != \"abc\"");
        assertTrue("1 < 2 < 3");
        assertTrue("1 < 2 <= 3");
        assertFalse("1 < 2 <= 1");
        assertFalse("1 < 2 && 2 < 1");
        assertTrue("1 < 2 && 2 < 3 && true");
        assertTrue("true || false || true");
        assertTrue("true || false || false");
        assertTrue("false || false || true");
        assertFalse("false || false || false");
        assertFalse("false && false && false");
        assertFalse("true && false && true");
        assertTrue("true && true && true");
        assertTrue("true && true && false || true");
    }

}