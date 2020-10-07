import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class TestLoad {


    public static void main(String[] args) {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        runtime.getName();

    }



    public static void go(){
        ExecutorService pool = Executors.newCachedThreadPool();
        final Semaphore sp = new Semaphore(1, true);
        for (int i = 0; i < 1; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        sp.acquire();

                    } catch (InterruptedException e) {
                    }
                    new SennaLoader();
                    sp.release();
                }
            };
            pool.execute(runnable);
        }
    }


}
