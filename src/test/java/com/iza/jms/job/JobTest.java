package com.iza.jms.job;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class JobTest {

    @Test(expected = IllegalArgumentException.class)
    public void newJobTestNullArg() {
        Job job = Job.newJob(null, "job", JobPriority.LOW);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newJobTestNegDelay() {
        Job job = Job.newJob(() -> { return; }, "job", JobPriority.LOW, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newJobTestNegPeriod() {
        Job job = Job.newJob(() -> { return; }, "job", JobPriority.LOW, 100, -1);
    }

    @Test
    public void runTestSuccess() {
        Job job = Mockito.spy(Job.newJob(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        assertEquals(JobStatus.CREATED, job.getJobStatus());

        job.run();

        assertEquals(JobStatus.SUCCESS, job.getJobStatus());
        Mockito.verify(job).setJobStatus(JobStatus.RUNNING);
    }

    @Test
    public void runTestFail() {
        Job job = Mockito.spy(Job.newJob(() -> {
            throw new NullPointerException("test exception");
        }));

        assertEquals(JobStatus.CREATED, job.getJobStatus());

        job.run();

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        Mockito.verify(job).setJobStatus(JobStatus.RUNNING);
    }
}