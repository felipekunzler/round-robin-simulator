package com.felipek.roundrobin.core;

import java.util.concurrent.locks.Condition;

public final class Util
{

    public static final String JOB_RUNNING_MSG = "%s Executando por: %sms. Tempo restante para terminar job: %sms";
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

    public static void waitForCondition(Condition condition)
    {
        try
        {
            condition.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
