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

package codecrafter47.bungeetablistplus.util;

import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandlingEventExecutor extends SingleThreadEventExecutor {

    private final Logger logger;

    public ExceptionHandlingEventExecutor(EventExecutorGroup parent, Executor executor, Logger logger) {
        super(parent, executor, true);
        this.logger = logger;
    }

    @Override
    protected void run() {
        do {
            Runnable task = this.takeTask();
            if (task != null) {
                try {
                    task.run();
                } catch (Throwable th) {
                    logger.log(Level.WARNING, "An unexpected error occurred: " + th.getMessage(), th);
                }
                this.updateLastExecutionTime();
            }
        } while (!this.confirmShutdown());
    }
}
