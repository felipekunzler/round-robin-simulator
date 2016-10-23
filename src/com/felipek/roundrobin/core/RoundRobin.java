package com.felipek.roundrobin.core;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class RoundRobin
{
    private Lock jobLock = new ReentrantLock(true);
    private Condition jobCondition = jobLock.newCondition();

    private Lock ioLock = new ReentrantLock(true);
    private Condition ioCondition = ioLock.newCondition();

    private final int newJobsFrequency;
    private final int quantum;
    private final int jobDuration;
    private final int ioBlockPercentage;
    private final int ioBlockDuration;

    private final Queue<Job> queue = new LinkedBlockingQueue<>();
    private final Queue<Job> ioQueue = new LinkedBlockingQueue<>();

    public RoundRobin(int newJobsFrequency, int quantum, int jobDuration, int ioBlockPercentage, int ioBlockDuration)
    {
        this.newJobsFrequency = newJobsFrequency;
        this.quantum = quantum;
        this.jobDuration = jobDuration;
        this.ioBlockPercentage = ioBlockPercentage >= 0 && ioBlockPercentage <= 100 ? ioBlockPercentage : 15;
        this.ioBlockDuration = ioBlockDuration;
    }

    public void start()
    {
        addJobsAsync(newJobsFrequency);
        runIoRequestsAsync();
        runJobs();
    }

    private void runJobs()
    {
        while (true) // NOSONAR
        {
            jobLock.lock();
            if (!queue.isEmpty())
            {
                jobLock.unlock();
                Job job = queue.poll();
                int runFor = job.getDuration() >= quantum ? quantum : job.getDuration();
                onRunJob.accept(String.format(Util.JOB_RUNNING_MSG, job, runFor, job.getDuration()));
                job.run(runFor);
                if (!job.isFinished())
                {
                    if (jobNeedsIo())
                    {
                        ioQueue.offer(job);
                        onJobIoBlocked.accept(Job.copy(job));
                        ioLock.lock();
                        ioCondition.signalAll();
                        ioLock.unlock();
                    }
                    else
                    {
                        queue.offer(job);
                        onJobRan.accept(Job.copy(job));
                    }
                }
                else
                {
                    onJobFinished.accept(Job.copy(job));
                }
            }
            else
            {
                try
                {
                    jobCondition.await();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    jobLock.unlock();
                }
            }
        }

    }

    private void addJobsAsync(int newJobsFrequency)
    {
        new Thread(() ->
        {
            while (true)
            {
                Util.sleep(newJobsFrequency);
                Job job = Job.createRandomJob(jobDuration);
                jobLock.lock();
                queue.add(job);
                onNewJob.accept(Job.copy(job));
                jobCondition.signalAll();
                jobLock.unlock();
            }
        }).start();
    }

    private void runIoRequestsAsync()
    {
        new Thread(() ->
        {
            while (true)
            {
                ioLock.lock();
                if (!ioQueue.isEmpty())
                {
                    ioLock.unlock();
                    Util.sleep(ioBlockDuration);
                    Job job = ioQueue.poll();
                    queue.offer(job);
                    onJobIoFinished.accept(Job.copy(job));
                }
                else
                {
                    try
                    {
                        ioCondition.await();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        ioLock.unlock();
                    }
                }
            }
        }).start();
    }

    private boolean jobNeedsIo()
    {
        Random random = new Random();
        return random.nextInt(100) < ioBlockPercentage;
    }

    private Consumer<Job> onNewJob = j -> System.out.println(String.format(Util.JOB_ADDED_MSG, j));
    private Consumer<String> onRunJob = System.out::println;
    private Consumer<Job> onJobIoFinished = j -> System.out.println(String.format(Util.JOB_IO_FINISHED_MSG, j));
    private Consumer<Job> onJobIoBlocked = j -> System.out.println(String.format(Util.JOB_IO_BLOCKED_MSG, j));
    private Consumer<Job> onJobFinished = j -> System.out.println(String.format(Util.JOB_FINISHED_MSG, j));
    private Consumer<Job> onJobRan = j ->  System.out.println(String.format(Util.JOB_FINISHED_MSG, j));

    public void onNewJob(Consumer<Job> onNewJob)
    {
        this.onNewJob = onNewJob;
    }

    public void onJobRan(Consumer<Job> onJobRan)
    {
        this.onJobRan = onJobRan;
    }

    public void onRunJob(Consumer<String> onRunJob)
    {
        this.onRunJob = onRunJob;
    }

    public void onJobIoFinished(Consumer<Job> onJobIoFinished)
    {
        this.onJobIoFinished = onJobIoFinished;
    }

    public void onJobIoBlocked(Consumer<Job> onJobIoBlocked)
    {
        this.onJobIoBlocked = onJobIoBlocked;
    }

    public void onJobFinished(Consumer<Job> onJobFinished)
    {
        this.onJobFinished = onJobFinished;
    }

}
