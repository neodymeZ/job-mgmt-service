package com.iza.jms.properties;

/**
 * Object that holds configurable properties of a job scheduler
 * {@link com.iza.jms.scheduler.JobScheduler}
 *
 * @author Zakhar Izverov
 * created on 15.09.2021
 */

public class Props {

    private final int jobThreadPoolSize;
    private final int jobQueueSize;
    private final int schedulerShutdownTimeoutSec;

    public Props(int jobThreadPoolSize, int jobQueueSize, int schedulerShutdownTimeoutSec) {
        this.jobThreadPoolSize = jobThreadPoolSize;
        this.jobQueueSize = jobQueueSize;
        this.schedulerShutdownTimeoutSec = schedulerShutdownTimeoutSec;
    }

    public int getJobThreadPoolSize() {
        return jobThreadPoolSize;
    }

    public int getJobQueueSize() {
        return jobQueueSize;
    }

    public int getSchedulerShutdownTimeoutSec() {
        return schedulerShutdownTimeoutSec;
    }

    @Override
    public String toString() {
        return "Props{" +
                "jobThreadPoolSize=" + jobThreadPoolSize +
                ", jobQueueSize=" + jobQueueSize +
                ", schedulerShutdownTimeoutSec=" + schedulerShutdownTimeoutSec +
                '}';
    }
}
