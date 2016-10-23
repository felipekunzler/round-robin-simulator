package com.felipek.roundrobin.core;

import static com.felipek.roundrobin.core.Util.sleep;

public class Job implements Cloneable
{

    private static int nextPid;

    private int pid;
    private int duration;

    public static Job createRandomJob(int duration)
    {
        Job job = new Job();
        job.pid = nextPid++;
        job.duration = duration;
        return job;
    }

    public void run(int quantum)
    {
        duration -= quantum;
        sleep(quantum);
    }

    public boolean isFinished()
    {
        return duration <= 0;
    }

    public int getDuration()
    {
        return duration;
    }

    @Override
    public String toString()
    {
        return "[PID: " + this.pid + "]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return pid == job.pid;

    }

    @Override
    public int hashCode()
    {
        return pid;
    }

    public static Job copy(Job job)
    {
        Job copy = new Job();
        copy.pid = job.pid;
        copy.duration = job.duration;
        return copy;
    }

}
