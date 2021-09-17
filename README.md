## Job Management Service

A simple job scheduling and execution system 

### Description

The Job Management Service (JMS) is capable of handling multiple
types of jobs, like sending emails or loading data into a database.

*NB: For details please see the [technical specification](tech_spec_1.0.pdf).*

### Prerequisites
* JDK 8 or later
* Maven

### Usage

* Create a job scheduler instance:
```java
JobScheduler scheduler = 
        JobScheduler.newJobScheduler("/jms.properties");
```

* Create a job instance passing a task Runnable as an argument:
```java
Job job = Job.newJob(() -> {
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}));
 ```
Optionally, job name, priority (high, medium or low) and scheduling
options may be specified

* Add the job to the job queue:
```java
scheduler.scheduleJob(job);
```

* Start the job scheduler service:
```java
scheduler.start();
```
Job scheduler will start to consume scheduled jobs from the queue
according to their priority and launch them in a separate thread pool

* To stop the job scheduler:
```java
scheduler.stop();
```
This method will first try to shut down the scheduler gracefully,
allowing tasks to finish execution within the specified time, and
then, if needed, performs a forceful shutdown.

* To cancel a running job:
```java
scheduler.cancelJob(job.getJobId(), true);
```
Jobs are uniquely identified by id, which should be passed to the method.
Second boolean argument allows or disallows task interruption while cancelling.
Method returns `true` if cancellation was successful.

* To check, if a job is running or not:
```java
scheduler.checkIfJobIsDone(job.getJobId());
```
The method returns `true` if the job has completed in any way (successful or not).

*NB: More elaborate code example is provided in [SampleApp](src/main/java/com/iza/jms/SampleApp.java)*

### Configuration

When creating a job scheduler instance, the factory method can take no arguments,
or take a path to the configuration file:
```java
JobScheduler scheduler = 
        JobScheduler.newJobScheduler("/jms.properties");
```

This file allows configuration of the following JMS parameters:
* Job execution thread pool size (default - 10)
* Job queue size (default - 100)
* Job scheduler shutdown timeout in seconds (default - 10)

If this file was not provided, could not be read or contains incorrect values,
default configuration will be loaded.

### Changelog

#### [1.0]
* First version of the service - implemented basic functionality