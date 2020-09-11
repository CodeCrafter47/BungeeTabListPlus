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

package codecrafter47.bungeetablistplus.common.util;

import java.util.concurrent.Executor;

public class RateLimitedExecutor implements Executor {

    private final long delay;

    private long lastExecuted = Long.MIN_VALUE;

    public RateLimitedExecutor(long delay) {
        this.delay = delay;
    }

    @Override
    public void execute(Runnable command) {
        long now = System.currentTimeMillis();
        if (lastExecuted + delay <= now) {
            lastExecuted = now;
            command.run();
        }
    }
}
