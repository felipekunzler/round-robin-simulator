package com.felipek.roundrobin.core;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class RoundRobin
{

    private final ReentrantLock newJobLock = new ReentrantLock();
    private final Condition newJobCondition = newJobLock.newCondition();

    private final ReentrantLock ioLock = new ReentrantLock();
    private final Condition ioCondition = ioLock.newCondition();

    private final int newJobsFrequency;
    private final int quantum;
    private final int jobDuration;
    private final int ioBlock;

    private final Queue<Job> queue = new LinkedBlockingQueue<>();
    private final Queue<Job> ioQueue = new LinkedBlockingQueue<>();

    public RoundRobin(int newJobsFrequency, int quantum, int jobDuration, int ioBlock)
    {
        this.newJobsFrequency = newJobsFrequency;
        this.quantum = quantum;
        this.jobDuration = jobDuration;
        this.ioBlock = ioBlock >= 0 && ioBlock <= 100 ? ioBlock : 15;
    }

    public void start()
    {
        addJobsAsync(newJobsFrequency);
        runIoRequestsAsync();
        runJobs();
    }

    /*
    When a job is ran, it may be IO blocked. Which means it'll run (or not?) and go to another queue
    of blocked processes for x ms. When it is finished, it goes back to the normal queue.
     */
    private void runJobs()
    {

            while (true) // NOSONAR
            {
                System.out.println("runJobs waiting for lock newJobLock");
                newJobLock.lock();
                System.out.println("runJobs got lock newJobLock");
                if (!queue.isEmpty())
                {
                    newJobLock.unlock();
                    System.out.println("runJobs released lock newJobLock");
                    Job job = queue.poll();
                    int runFor = job.getDuration() >= quantum ? quantum : job.getDuration();
                    onRunJob.accept(String.format(Util.JOB_RUNNING_MSG, job, runFor, job.getDuration()));
                    job.run(runFor);
                    if (!job.isFinished())
                    {
                        if (jobNeedsIo())
                        {
                            System.out.println("runJobs waiting for lock ioLock");
                            ioLock.lock();
                            ioQueue.offer(job);
                            onJobIoBlocked.accept(Job.copy(job));
                            System.out.println("runJobs got ioLock");
                            ioCondition.signalAll();
                            ioLock.unlock();
                            System.out.println("runJobs released ioLock");
                        }
                        else
                        {
                            queue.offer(job);
                        }
                    }
                    onJobRan.accept(Job.copy(job));
                }
                else
                {
                    System.out.println("runJobs released lock newJobLock [waiting]");
                    Util.waitForCondition(newJobCondition);
                }
            }

    }

    /**
     * Adiciona novos jobs continuamente em uma thread separada.
     *
     * @param newJobsFrequency frequência em ms em que jobs serão criado
     */
    private void addJobsAsync(int newJobsFrequency)
    {
        new Thread(() ->
        {
            while (true)
            {
                Util.sleep(newJobsFrequency);
                System.out.println("new job waiting for lock newJobLock");
                newJobLock.lock();
                System.out.println("new job got lock newJobLock");
                Job job = Job.createRandomJob(jobDuration);
                queue.add(job);
                onNewJob.accept(Job.copy(job));
                System.out.println("new job released newJobLock");
                newJobCondition.signalAll();
                newJobLock.unlock();
            }
        }).start();
    }

    private void runIoRequestsAsync()
    {
        new Thread(() ->
        {
            while (true)
            {
                System.out.println("ioLock waiting for lock ioLock");
                ioLock.lock();
                System.out.println("ioLock got lock ioLock");
                if (!ioQueue.isEmpty())
                {
                    ioLock.unlock();
                    System.out.println("ioLock released ioLock");
                    Util.sleep(10000);
                    Job job = ioQueue.poll();
                    queue.offer(job);
                    onJobIoFinished.accept(Job.copy(job));
                }
                else
                {
                    System.out.println("ioLock released ioLock");
                    Util.waitForCondition(ioCondition);
                }
            }
        }).start();
    }

    private boolean jobNeedsIo()
    {
        Random random = new Random();
        return random.nextInt(100) < ioBlock;
    }

    private Consumer<Job> onNewJob = j -> System.out.println(String.format(Util.JOB_ADDED_MSG, j));
    private Consumer<String> onRunJob = System.out::println;
    private Consumer<Job> onJobIoFinished = j -> System.out.println(String.format(Util.JOB_IO_FINISHED_MSG, j));
    private Consumer<Job> onJobIoBlocked = j -> System.out.println(String.format(Util.JOB_IO_BLOCKED_MSG, j));
    private Consumer<Job> onJobRan = j ->
    {
        if (j.isFinished())
        {
            System.out.println(String.format(Util.JOB_FINISHED_MSG, j));
        }
    };

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
}
