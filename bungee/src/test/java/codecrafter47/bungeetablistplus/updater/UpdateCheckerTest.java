/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.updater;

import org.junit.Assert;
import org.junit.Test;


public class UpdateCheckerTest {

    @Test
    public void testCompareVersions() throws Exception {
        Assert.assertTrue(UpdateChecker.compareVersions("2.0", "1.9"));
        Assert.assertTrue(UpdateChecker.compareVersions("2.0", "2.0-SNAPSHOT"));
        Assert.assertTrue(UpdateChecker.compareVersions("2.1", "2.0-SNAPSHOT"));
        Assert.assertTrue(UpdateChecker.compareVersions("2.1", "2.0"));
        Assert.assertTrue(UpdateChecker.compareVersions("2.0.1", "2.0"));
        Assert.assertFalse(UpdateChecker.compareVersions("2.1.1", "2.1.2"));
        Assert.assertFalse(UpdateChecker.compareVersions("1.9", "2.0"));
        Assert.assertFalse(UpdateChecker.compareVersions("2.0", "2.1"));
        Assert.assertFalse(UpdateChecker.compareVersions("2.0", "2.0.1"));
        Assert.assertFalse(UpdateChecker.compareVersions("2.0", "2.1-SNAPSHOT"));
        Assert.assertFalse(UpdateChecker.compareVersions("2.2", "2.2.1-SNAPSHOT"));
    }
}