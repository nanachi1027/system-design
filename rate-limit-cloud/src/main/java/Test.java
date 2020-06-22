import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) {
        double QPS = 10; // 100 request / s

        // 1 ms 应该投放多少个 token
        long period =  new Double(1000 * 1/QPS).longValue();
        System.out.println("should send " + period + " token per milliseconds.");
        System.out.println(new Date(System.currentTimeMillis()));

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            System.out.println("Add token = " + period + ", timestamp:" + (System.currentTimeMillis()));
        }, 1000, period, TimeUnit.MILLISECONDS);
    }
}
