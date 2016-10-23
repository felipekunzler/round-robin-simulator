import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main
{
    private Lock lock1 = new ReentrantLock();
    private Lock lock2 = new ReentrantLock();

    public static void main(String[] args)
    {
        Main main = new Main();
        main.addAsync1();
        main.addAsync2();
    }

    private void addAsync1()
    {
        runInNewThread(() ->
        {
            while (true)
            {
                lock1.lock();
                System.out.println("1111111111");
                sleep(100);
                lock1.unlock();
            }
        });
    }

    private void addAsync2()
    {
        runInNewThread(() ->
        {
            while (true)
            {
                lock2.lock();
                System.out.println("222");
                sleep(100);
                lock2.unlock();
            }
        });
    }

    private static void runInNewThread(Runnable runnable)
    {
        new Thread(runnable).start();
    }

    private void sleep(int i)
    {
        try
        {
            Thread.sleep(i);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
