package org.objectscape.wilco;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * Created by plohmann on 06.03.2015.
 */
public class ExecutorScratchTest {

    @Test
    public void executorWorkbook() throws InterruptedException {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        BlockingQueue<Runnable> queue = new LinkedTransferQueue<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                cpuCores * 2,
                cpuCores * 8,
                5000, TimeUnit.MILLISECONDS, queue);

        int numberOfJobs = 100;
        CountDownLatch latch = new CountDownLatch(numberOfJobs);
        Runnable runnable = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print(".");
            if(latch.getCount() % 10 == 0) {
                System.out.print("\n");
            }
            latch.countDown();
        };

        for (int i = 0; i < numberOfJobs; i++) {
            executor.execute(runnable);
        }

        latch.await();
        System.out.println("done!");
        executor.shutdown();
    }

}
