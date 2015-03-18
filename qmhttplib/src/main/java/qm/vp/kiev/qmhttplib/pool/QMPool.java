package qm.vp.kiev.qmhttplib.pool;


import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;



public class QMPool extends PausableThreadPoolExecutor {

    private static final int POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 10;

    private static BlockingQueue<Runnable> worksQueue = new ArrayBlockingQueue<>(400);


    public QMPool() {
        super(POOL_SIZE, MAX_POOL_SIZE, worksQueue);

        Thread monitor = new Thread(new MyMonitorThread(this));
        monitor.setDaemon(true);
        monitor.start();
    }

    public void add(qm.vp.kiev.qmhttplib.abstraction.QMRequest qmRequest) {
        submit(qmRequest);
    }

    public class MyMonitorThread implements Runnable {
        ThreadPoolExecutor executor;

        public MyMonitorThread(ThreadPoolExecutor executor) {
            this.executor = executor;
        }

        @Override
        public void run() {
            try {
                do {
                    Log.v("QueryMasterThreadPool", String.format("[QueryMasterThreadPool monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated()));
                    Thread.sleep(10000);
                }
                while (true);
            } catch (Exception e) {
            }
        }
    }
}
