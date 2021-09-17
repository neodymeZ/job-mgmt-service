package com.iza.jms.job;

/**
 * Status of a job which reflects the job's state
 */

public enum JobStatus {

    /**
     * Initial state of a job
     */
    CREATED,

    /**
     * Job placed into the job scheduler queue
     */
    QUEUED,

    /**
     * Job runnable task started execution
     */
    RUNNING,

    /**
     * Job successfully finished its execution
     */
    SUCCESS,

    /**
     * Job failed to finish execution due to an unchecked exception
     */
    FAILED
}
