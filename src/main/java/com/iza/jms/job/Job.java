package com.iza.jms.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Runnable Job which can be scheduled to the JMS scheduler
 * {@link com.iza.jms.scheduler.JobScheduler}
 *
 * @author Zakhar Izverov
 * created on 14.09.2021
 */

public class Job implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private final Runnable jobRunnable;
    private final String jobName;
    private final String jobId;

    private JobStatus jobStatus;
    private final JobPriority jobPriority;
    private final JobSchedule jobSchedule;

    private final long initialDelayMs;
    private final long periodMs;

    private Job(Runnable jobRunnable,
               String jobName,
               JobPriority jobPriority,
               JobSchedule jobSchedule,
               long initialDelayMs,
               long periodMs) {
        this.jobRunnable = jobRunnable;
        this.jobName = jobName;
        this.jobId = UUID.randomUUID().toString();
        this.jobStatus = JobStatus.CREATED;
        this.jobPriority = jobPriority;
        this.jobSchedule = jobSchedule;
        this.initialDelayMs = initialDelayMs;
        this.periodMs = periodMs;
    }

    /**
     * Creates a new Job with low priority and immediate execution
     *
     * @param jobRunnable the runnable task
     * @return a Job with the specified parameters
     * @throws IllegalArgumentException if the job parameters are invalid
     */

    public static Job newJob(Runnable jobRunnable) throws IllegalArgumentException {
        if (jobRunnable == null) {
            throw new IllegalArgumentException("job parameters cannot be null");
        }

        return new Job(jobRunnable, "job", JobPriority.LOW, JobSchedule.IMMEDIATE, 0L, 0L);
    }

    /**
     * Creates a new Job with the specified name and priority, and immediate execution
     *
     * @param jobRunnable the runnable task
     * @param jobName job name
     * @param jobPriority job priority {@link JobPriority}
     * @return a Job with the specified parameters
     * @throws IllegalArgumentException if the job parameters are invalid
     */

    public static Job newJob(Runnable jobRunnable,
                             String jobName,
                             JobPriority jobPriority)
            throws IllegalArgumentException {
        if (jobRunnable == null || jobName == null || jobPriority == null) {
            throw new IllegalArgumentException("job parameters cannot be null");
        }

        return new Job(jobRunnable, jobName, jobPriority, JobSchedule.IMMEDIATE, 0L, 0L);
    }

    /**
     * Creates a new Job with the specified name and priority, executed after the specified delay
     *
     * @param jobRunnable the runnable task
     * @param jobName job name
     * @param jobPriority job priority {@link JobPriority}
     * @param initialDelayMs delay before running the Job task in milliseconds
     * @return a Job with the specified parameters
     * @throws IllegalArgumentException if the job parameters are invalid
     */

    public static Job newJob(Runnable jobRunnable,
                             String jobName,
                             JobPriority jobPriority,
                             long initialDelayMs)
            throws IllegalArgumentException {
        if (jobRunnable == null || jobName == null || jobPriority == null) {
            throw new IllegalArgumentException("job parameters cannot be null");
        }

        if (initialDelayMs <= 0L) {
            throw new IllegalArgumentException("initial delay must be positive");
        }

        return new Job(jobRunnable, jobName, jobPriority, JobSchedule.DELAYED, initialDelayMs, 0L);
    }

    /**
     * Creates a new Job with the specified name and priority, executed periodically after the specified initial delay
     *
     * @param jobRunnable the runnable task
     * @param jobName job name
     * @param jobPriority job priority {@link JobPriority}
     * @param initialDelayMs delay before the Job first run in milliseconds
     * @param periodMs delay after each consecutive Job run in milliseconds
     * @return a Job with the specified parameters
     * @throws IllegalArgumentException if the job parameters are invalid
     */

    public static Job newJob(Runnable jobRunnable,
                             String jobName,
                             JobPriority jobPriority,
                             long initialDelayMs,
                             long periodMs) throws IllegalArgumentException {

        if (jobRunnable == null || jobName == null || jobPriority == null) {
            throw new IllegalArgumentException("job parameters cannot be null");
        }

        if (initialDelayMs < 0L || periodMs <= 0L) {
            throw new IllegalArgumentException("initial delay must not be negative and period must be positive");
        }

        return new Job(jobRunnable, jobName, jobPriority, JobSchedule.PERIODIC, initialDelayMs, periodMs);
    }

    /**
     * Runs the Job's task, setting the corresponding JobStatus ({@link JobStatus})
     */

    @Override
    public void run() {
        logger.info("Running job \"{}\" (id {}) with {} priority", jobName, jobId, jobPriority.name());
        this.setJobStatus(JobStatus.RUNNING);

        try {
            jobRunnable.run();
        } catch (Exception e) {
            logger.error("Job \"{}\" (id {}) failed with an exception", jobName, jobId, e);
            this.setJobStatus(JobStatus.FAILED);
            return;
        }

        logger.info("Job \"{}\" (id {}) completed successfully", jobName, jobId);
        this.setJobStatus(JobStatus.SUCCESS);
    }

    public Runnable getJobRunnable() {
        return jobRunnable;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobId() {
        return jobId;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public JobPriority getJobPriority() {
        return jobPriority;
    }

    public JobSchedule getJobSchedule() {
        return jobSchedule;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public long getPeriodMs() {
        return periodMs;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobName='" + jobName + '\'' +
                ", jobId='" + jobId + '\'' +
                ", jobStatus=" + jobStatus +
                ", jobPriority=" + jobPriority +
                ", jobSchedule=" + jobSchedule +
                ", initialDelayMs=" + initialDelayMs +
                ", periodMs=" + periodMs +
                '}';
    }
}
