package com.iza.jms.job;

/**
 * Priority of a job in the job scheduler queue
 */

public enum JobPriority {

    /**
     * Jobs with HIGH priority are taken from the queue first
     */
    HIGH,

    /**
     * Jobs with MEDIUM priority are taken from the queue after jobs with HIGH priority
     */
    MEDIUM,

    /**
     * Jobs with LOW priority are taken from the queue last
     */
    LOW
}
