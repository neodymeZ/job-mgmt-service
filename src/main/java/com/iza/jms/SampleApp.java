package com.iza.jms;

import com.iza.jms.job.Job;
import com.iza.jms.job.JobPriority;
import com.iza.jms.scheduler.JobScheduler;

/**
 * A sample application to demonstrate how the JMS works
 * May be compiled to an executable JAR with "mvn clean compile assembly:single" command
 *
 * @author Zakhar Izverov
 * created on 17.09.2021
 */

public class SampleApp
{
    public static void main(String[] args) {

        // We create a scheduler with 3 job execution threads, so that a lower priority ping job will have to wait
        // in the queue
        JobScheduler scheduler = JobScheduler.newJobScheduler("/jms.properties");

        Job emailJob1 = createEmailJob("Hello, Bob!", "bob@gmail.com");
        Job emailJob2 = createEmailJob("Hello, John!", "john@live.com");
        Job emailJob3 = createEmailJob("Hello, Tom!", "tom@icloud.com");

        Job pingJob = createPingJob(64, 100, "8.8.8.8");

        // We first schedule job with the lower priority to demonstrate that jobs with higher priority will be
        // launched before it
        scheduler.scheduleJob(pingJob);

        scheduler.scheduleJob(emailJob1);
        scheduler.scheduleJob(emailJob2);
        scheduler.scheduleJob(emailJob3);

        scheduler.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // As the ping job is still running, we cancel it and then stop the scheduler
        scheduler.cancelJob(pingJob.getJobId(), true);
        scheduler.stop();
    }

    private static Job createEmailJob(String emailText, String recipientAddress) {

        return Job.newJob(() -> {
            System.out.println("Sending email to: " + recipientAddress + ", message body is: " + emailText);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Email sent");
        }, "emailJob", JobPriority.HIGH);
    }

    private static Job createPingJob(int packetSize, int numberOfPackets, String destinationIp) {

        return Job.newJob(() -> {
            System.out.println("PING " + destinationIp + " (" + destinationIp + "): " + (packetSize - 8) + " data bytes");

            for (int i = 0; i < numberOfPackets; i++) {
                System.out.println(packetSize + " bytes from " + destinationIp + ": icmp_seq=" + i
                        + " ttl=117 time=1.001 ms");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "pingJob", JobPriority.LOW);
    }
}
