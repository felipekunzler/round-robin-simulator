package com.felipek.roundrobin.core;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public final class Util
{

    public static final String JOB_RUNNING_MSG = "%s Executando por: %sms. Restante: %sms";
    public static final String JOB_ADDED_MSG = "Novo Job %s adicionado a fila.";
    public static final String JOB_FINISHED_MSG = "%s Finalizado.";
    public static final String JOB_IO_FINISHED_MSG = "%s IO Finalizado.";
    public static final String JOB_IO_BLOCKED_MSG = "%s IO Blocked";

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void waitForCondition(Condition condition, Lock lock)
    {
        try
        {
            condition.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            lock.unlock();
        }
    }

}
