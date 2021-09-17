package com.iza.jms.scheduler;

import com.iza.jms.job.Job;
import com.iza.jms.job.JobPriority;
import com.iza.jms.job.JobStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class JobSchedulerTest {

    private JobScheduler jobScheduler;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setUp() {

        if(testName.getMethodName().equals("scheduleJobTestPriority")) { return; }

        // Job scheduler uses a single threaded job execution pool for testing purposes
        jobScheduler = JobScheduler.newJobScheduler("/jms-test-1.properties");
        jobScheduler.start();
    }

    @After
    public void tearDown() {
        jobScheduler.stop();
    }

    @Test
    public void scheduleJobTestImmediateSuccess() throws InterruptedException {
        Job job = Mockito.spy(Job.newJob(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        assertEquals(JobStatus.CREATED, job.getJobStatus());

        jobScheduler.scheduleJob(job);
        Thread.sleep(100);

        assertEquals(JobStatus.SUCCESS, job.getJobStatus());
        assertTrue(jobScheduler.checkIfJobIsDone(job.getJobId()));

        Mockito.verify(job).setJobStatus(JobStatus.QUEUED);
        Mockito.verify(job).setJobStatus(JobStatus.RUNNING);
    }

    @Test
    public void scheduleJobTestImmediateFail() throws InterruptedException {
        Job job = Mockito.spy(Job.newJob(() -> {
            throw new RuntimeException();
        }));

        assertEquals(JobStatus.CREATED, job.getJobStatus());

        jobScheduler.scheduleJob(job);
        Thread.sleep(50);

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertTrue(jobScheduler.checkIfJobIsDone(job.getJobId()));

        Mockito.verify(job).setJobStatus(JobStatus.QUEUED);
        Mockito.verify(job).setJobStatus(JobStatus.RUNNING);
    }

    @Test
    public void scheduleJobTestDelayed() throws InterruptedException {
        Job job = Job.newJob(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "job", JobPriority.LOW, 100);

        assertEquals(JobStatus.CREATED, job.getJobStatus());

        jobScheduler.scheduleJob(job);

        Thread.sleep(50);
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));
        assertEquals(JobStatus.QUEUED, job.getJobStatus());

        Thread.sleep(50);
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));

        Thread.sleep(100);
        assertEquals(JobStatus.SUCCESS, job.getJobStatus());
        assertTrue(jobScheduler.checkIfJobIsDone(job.getJobId()));
    }

    @Test
    public void scheduleJobTestPeriodic() throws InterruptedException {
        Job job = Job.newJob(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "job", JobPriority.LOW, 50, 100);

        assertEquals(JobStatus.CREATED, job.getJobStatus());

        jobScheduler.scheduleJob(job);

        Thread.sleep(25);
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));
        assertEquals(JobStatus.QUEUED, job.getJobStatus());

        Thread.sleep(100);
        assertEquals(JobStatus.SUCCESS, job.getJobStatus());
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));

        Thread.sleep(100);
        assertEquals(JobStatus.RUNNING, job.getJobStatus());
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));

        Thread.sleep(100);
        assertEquals(JobStatus.SUCCESS, job.getJobStatus());
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));

        jobScheduler.cancelJob(job.getJobId(), true);
        Thread.sleep(50);
        assertTrue(jobScheduler.checkIfJobIsDone(job.getJobId()));
    }

    @Test
    public void scheduleJobTestPriority() throws InterruptedException {
        Job jobLow = Job.newJob(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "jobLow", JobPriority.LOW);

        Job jobMedium = Job.newJob(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "jobMedium", JobPriority.MEDIUM);

        Job jobHigh = Job.newJob(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "jobHigh", JobPriority.HIGH);

        // Create scheduler
        jobScheduler = JobScheduler.newJobScheduler("/jms-test-1.properties");

        // Schedule jobs from low to high priority (execution order should be inversed)
        jobScheduler.scheduleJob(jobLow);
        jobScheduler.scheduleJob(jobMedium);
        jobScheduler.scheduleJob(jobHigh);

        jobScheduler.start();

        Thread.sleep(25);
        assertEquals(JobStatus.QUEUED, jobLow.getJobStatus());
        assertEquals(JobStatus.QUEUED, jobMedium.getJobStatus());
        assertEquals(JobStatus.RUNNING, jobHigh.getJobStatus());

        Thread.sleep(100);
        assertEquals(JobStatus.QUEUED, jobLow.getJobStatus());
        assertEquals(JobStatus.RUNNING, jobMedium.getJobStatus());
        assertEquals(JobStatus.SUCCESS, jobHigh.getJobStatus());

        Thread.sleep(100);
        assertEquals(JobStatus.RUNNING, jobLow.getJobStatus());
        assertEquals(JobStatus.SUCCESS, jobMedium.getJobStatus());
        assertEquals(JobStatus.SUCCESS, jobHigh.getJobStatus());

        Thread.sleep(100);
        assertEquals(JobStatus.SUCCESS, jobLow.getJobStatus());
        assertEquals(JobStatus.SUCCESS, jobMedium.getJobStatus());
        assertEquals(JobStatus.SUCCESS, jobHigh.getJobStatus());

        assertTrue(jobScheduler.checkIfJobIsDone(jobLow.getJobId()));
        assertTrue(jobScheduler.checkIfJobIsDone(jobMedium.getJobId()));
        assertTrue(jobScheduler.checkIfJobIsDone(jobHigh.getJobId()));
    }

    @Test
    public void cancelJob() throws InterruptedException {
        Job job = Job.newJob(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "job", JobPriority.LOW);

        jobScheduler.scheduleJob(job);

        Thread.sleep(25);
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));
        assertEquals(JobStatus.RUNNING, job.getJobStatus());

        boolean cancelled = jobScheduler.cancelJob(job.getJobId(), true);
        Thread.sleep(25);

        assertTrue(jobScheduler.checkIfJobIsDone(job.getJobId()));
        assertTrue(cancelled);
        assertEquals(JobStatus.SUCCESS, job.getJobStatus());
    }

    @Test
    public void checkIfJobIsDone() throws InterruptedException {
        Job job = Job.newJob(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "job", JobPriority.LOW);

        jobScheduler.scheduleJob(job);

        Thread.sleep(25);
        assertFalse(jobScheduler.checkIfJobIsDone(job.getJobId()));
        assertEquals(JobStatus.RUNNING, job.getJobStatus());

        Thread.sleep(100);
        assertTrue(jobScheduler.checkIfJobIsDone(job.getJobId()));
        assertEquals(JobStatus.SUCCESS, job.getJobStatus());
    }
}