package com.iza.jms.scheduler;

import com.iza.jms.job.Job;
import com.iza.jms.job.JobSchedule;
import com.iza.jms.job.JobStatus;
import com.iza.jms.properties.PropertiesReader;
import com.iza.jms.properties.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Job scheduler of the JMS
 * Uses priority blocking queue and scheduled single-threaded executor to execute jobs from the queue
 *
 * @author Zakhar Izverov
 * created on 16.09.2021
 */

public class JobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private final ScheduledExecutorService jobPoolExecutor;
    private final ExecutorService jobScheduler;
    private final PriorityBlockingQueue<Job> queue;
    private final Props props;

    private final HashMap<String, ScheduledFuture<?>> jobFutureMap = new HashMap<>();
    private Boolean jobSchedulerStarted;

    private JobScheduler(ScheduledExecutorService jobPoolExecutor,
                         ExecutorService jobScheduler,
                         PriorityBlockingQueue<Job> queue,
                         Props props) {
        this.jobPoolExecutor = jobPoolExecutor;
        this.jobScheduler = jobScheduler;
        this.queue = queue;
        this.props = props;
    }

    /**
     * Creates a new JobScheduler with default parameters
     *
     * @return a JobScheduler with parameters from the properties file
     */

    public static JobScheduler newJobScheduler() {
        return newJobScheduler(null);
    }

    /**
     * Creates a new JobScheduler with parameters specified in the properties file
     *
     * @param propertiesFilePath relative path to the properties file,
     *                           e.g. "/jms.properties"
     * @return a JobScheduler with parameters from the properties file
     */

    public static JobScheduler newJobScheduler(String propertiesFilePath) {
        logger.info("Configuring job scheduler properties");

        Props props = PropertiesReader.readProperties(propertiesFilePath);

        return new JobScheduler(Executors.newScheduledThreadPool(props.getJobThreadPoolSize()),
                Executors.newSingleThreadExecutor(),
                new PriorityBlockingQueue<>(props.getJobQueueSize(), Comparator.comparing(Job::getJobPriority)),
                props);
    }

    /**
     * Launches a JobScheduler instance
     * JobScheduler continuously runs in a separate thread, taking jobs from the queue
     * until the queue is empty or the job execution thread pool is full
     * After launching a job, JobScheduler puts the ScheduledFuture object into a map to
     * allow managing scheduled jobs
     */

    public void start() {
        if (jobSchedulerStarted != null && jobSchedulerStarted) {
            logger.warn("Job scheduler is running, startup aborted");
            return;
        } else if (jobSchedulerStarted != null && !jobSchedulerStarted) {
            logger.warn("Job scheduler is shut down, startup aborted");
            return;
        }

        logger.info("Starting job scheduler");

        jobScheduler.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Job currentJob = queue.take();
                    JobSchedule schedule = currentJob.getJobSchedule();

                    ScheduledFuture<?> jobFuture;

                    if (schedule == JobSchedule.PERIODIC) {
                        jobFuture = jobPoolExecutor.scheduleWithFixedDelay(currentJob, currentJob.getInitialDelayMs(),
                                currentJob.getPeriodMs(), TimeUnit.MILLISECONDS);
                    } else {
                        jobFuture = jobPoolExecutor.schedule(currentJob, currentJob.getInitialDelayMs(),
                                TimeUnit.MILLISECONDS);
                    }

                    jobFutureMap.put(currentJob.getJobId(), jobFuture);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        logger.info("Job scheduler started");
        this.jobSchedulerStarted = true;
    }

    /**
     * Shuts down a JobScheduler instance, waiting for the running jobs to complete
     * within the configured shutdown timeout
     */

    public void stop() {
        if (!jobSchedulerStarted) {
            logger.warn("Job scheduler not running, shutdown aborted");
            return;
        }
        logger.info("Stopping job scheduler");

        jobPoolExecutor.shutdown();

        try {
            if (!jobPoolExecutor.awaitTermination(props.getSchedulerShutdownTimeoutSec(), TimeUnit.SECONDS)) {
                jobPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            jobPoolExecutor.shutdownNow();
        }

        jobScheduler.shutdown();

        try {
            if (!jobScheduler.awaitTermination(props.getSchedulerShutdownTimeoutSec(), TimeUnit.SECONDS)) {
                jobScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            jobScheduler.shutdownNow();
        }

        logger.info("Job scheduler stopped");
        this.jobSchedulerStarted = false;
    }

    /**
     * Adds a Job to the JobScheduler queue and changes its status to QUEUED
     *
     * @param job Job to be queued
     * @return true if the Job was successfully queued, or false otherwise
     */

    public boolean scheduleJob(Job job) {
        try {
            queue.add(job);
            job.setJobStatus(JobStatus.QUEUED);
        } catch (Exception e) {
            logger.warn("Unable to add job \"{}\" (id {}) to the queue, job status was not changed",
                    job.getJobName(), job.getJobId(), e);
            logger.debug("An exception occurred: ", e);
            return false;
        }
        return true;
    }

    /**
     * Cancels Job to the JobScheduler queue and changes its status to QUEUED
     *
     * @param jobId Id of the Job to be cancelled
     * @param mayInterruptIfRunning if true, the specified Job can be interrupted if running
     * @return true if the Job was successfully cancelled, or false otherwise
     */
    public boolean cancelJob(String jobId, boolean mayInterruptIfRunning) {

        if (jobId == null) {
            logger.warn("Unable to cancel job, jobId is null");
            return false;
        }

        ScheduledFuture<?> jobFuture = jobFutureMap.get(jobId);
        boolean cancelled = false;

        if (jobFuture != null) {
            cancelled = jobFuture.cancel(mayInterruptIfRunning);
        }

        if (cancelled) {
            logger.info("Job with id {} was successfully cancelled", jobId);
        } else {
            logger.warn("Job with id {} cannot be cancelled - it may be completed," +
                    "cancelled before or does not exist", jobId);
        }

        return cancelled;
    }

    /**
     * Checks, if a Job has finished its execution due to normal termination,
     * exception or cancellation
     *
     * @param jobId Id of the Job to be checked
     * @return true if the Job is completed, or false otherwise
     */

    public boolean checkIfJobIsDone(String jobId) {
        if (jobId == null) {
            logger.warn("Unable to check job status, jobId is null");
            return false;
        }

        ScheduledFuture<?> jobFuture = jobFutureMap.get(jobId);
        boolean isDone = false;

        if (jobFuture != null) {
            isDone = jobFuture.isDone();
        }

        return isDone;
    }

    public Props getProps() {
        return props;
    }

    public boolean isJobSchedulerStarted() {
        return jobSchedulerStarted;
    }
}
