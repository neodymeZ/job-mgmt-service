package com.iza.jms.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Reader of the specified JMS configuration file which contains job scheduler properties
 * {@link com.iza.jms.scheduler.JobScheduler}
 *
 * @author Zakhar Izverov
 * created on 15.09.2021
 */

public class PropertiesReader {

    private static final String defaultJobThreadPoolSize = "10";
    private static final String defaultJobQueueSize = "100";
    private static final String defaultSchedulerShutdownTimeoutSec = "10";

    private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

    /**
     * Reads the job scheduler properties from the properties file and stores
     * them in a Props object ({@link Props})
     * If no properties file was specified, or it contains errors,
     * default parameters are returned
     * If some properties are not specified in the properties file,
     * default values are returned for the respective parameters
     *
     * @param propertiesFilePath path to the properties file,
     *                           e.g. "/jms.properties"
     * @return Props object with configuration properties
     */

    public static Props readProperties(String propertiesFilePath) {

        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("THREAD_POOL_SIZE", defaultJobThreadPoolSize);
        defaultProperties.setProperty("QUEUE_SIZE", defaultJobQueueSize);
        defaultProperties.setProperty("SHUTDOWN_TIMEOUT", defaultSchedulerShutdownTimeoutSec);

        Properties properties = new Properties(defaultProperties);

        if (propertiesFilePath != null) {
            try (InputStream inputStream = PropertiesReader.class.getResourceAsStream(propertiesFilePath)) {
                properties.load(inputStream);
                logger.info("Successfully read properties from file {}", propertiesFilePath);
            } catch (Exception e) {
                logger.warn("Unable to read properties from file {}, using defaults", propertiesFilePath);
                logger.debug("An exception occurred: ", e);
            }
        } else {
            logger.info("Properties file path not provided, using defaults");
        }

        return parseProperties(properties, defaultProperties);
    }

    private static Props parseProperties(Properties properties, Properties defaultProperties) {

        int jobThreadPoolSize;
        int jobQueueSize;
        int schedulerShutdownTimeout;

        try {
            jobThreadPoolSize = Integer.parseInt(properties.getProperty("THREAD_POOL_SIZE"));
            jobQueueSize = Integer.parseInt(properties.getProperty("QUEUE_SIZE"));
            schedulerShutdownTimeout = Integer.parseInt(properties.getProperty("SHUTDOWN_TIMEOUT"));

            if (jobThreadPoolSize <= 0 || jobQueueSize <= 0 || schedulerShutdownTimeout <= 0) {
                throw new NumberFormatException("negative properties values");
            }
        } catch (NumberFormatException e) {
            logger.warn("Unable to parse properties, using defaults");
            logger.debug("An exception occurred: ", e);

            jobThreadPoolSize = Integer.parseInt(defaultProperties.getProperty("THREAD_POOL_SIZE"));
            jobQueueSize = Integer.parseInt(defaultProperties.getProperty("QUEUE_SIZE"));
            schedulerShutdownTimeout = Integer.parseInt(defaultProperties.getProperty("SHUTDOWN_TIMEOUT"));
        }

        return new Props(jobThreadPoolSize, jobQueueSize, schedulerShutdownTimeout);
    }
}
