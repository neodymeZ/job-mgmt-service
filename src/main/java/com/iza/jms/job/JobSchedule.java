package com.iza.jms.job;

/**
 * Type of job execution schedule
 */

public enum JobSchedule {

    /**
     * Run job immediately after taking from the queue
     */
    IMMEDIATE,

    /**
     * Run job after the specified delay has passed after taking job from the queue
     */
    DELAYED,

    /**
     * Run job periodically after the specified delay has passed after taking job from the queue
     * Delay between executions (period) is fixed and starts after the previous run has completed
     */
    PERIODIC
}
