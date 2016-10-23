package com.felipek.roundrobin.core;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class RoundRobin
{
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
            if (!queue.isEmpty())
            {
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
                Util.sleep(100);
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
                queue.add(job);
                onNewJob.accept(Job.copy(job));
            }
        }).start();
    }

    private void runIoRequestsAsync()
    {
        new Thread(() ->
        {
            while (true)
            {
                if (!ioQueue.isEmpty())
                {
                    Util.sleep(ioBlockDuration);
                    Job job = ioQueue.poll();
                    queue.offer(job);
                    onJobIoFinished.accept(Job.copy(job));
                }
                else
                {
                   Util.sleep(100);
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
