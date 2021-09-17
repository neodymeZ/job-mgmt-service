package com.iza.jms.properties;

import org.junit.Test;

import static org.junit.Assert.*;

public class PropertiesReaderTest {

    @Test
    public void readPropertiesTestCorrect() {
        Props props = PropertiesReader.readProperties("/jms-test-1.properties");

        assertEquals(1, props.getJobThreadPoolSize());
        assertEquals(50, props.getJobQueueSize());
        assertEquals(1, props.getSchedulerShutdownTimeoutSec());
    }

    @Test
    public void readPropertiesTestNotNumber() {
        Props props = PropertiesReader.readProperties("/jms-test-2.properties");

        assertEquals(10, props.getJobThreadPoolSize());
        assertEquals(100, props.getJobQueueSize());
        assertEquals(10, props.getSchedulerShutdownTimeoutSec());
    }

    @Test
    public void readPropertiesTestNegNumber() {
        Props props = PropertiesReader.readProperties("/jms-test-3.properties");

        assertEquals(10, props.getJobThreadPoolSize());
        assertEquals(100, props.getJobQueueSize());
        assertEquals(10, props.getSchedulerShutdownTimeoutSec());
    }

    @Test
    public void readPropertiesTestPartial() {
        Props props = PropertiesReader.readProperties("/jms-test-4.properties");

        assertEquals(10, props.getJobThreadPoolSize());
        assertEquals(100, props.getJobQueueSize());
        assertEquals(5, props.getSchedulerShutdownTimeoutSec());
    }

    @Test
    public void readPropertiesTestNoPath() {
        Props props = PropertiesReader.readProperties(null);

        assertEquals(10, props.getJobThreadPoolSize());
        assertEquals(100, props.getJobQueueSize());
        assertEquals(10, props.getSchedulerShutdownTimeoutSec());
    }
}