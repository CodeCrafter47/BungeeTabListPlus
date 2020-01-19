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
