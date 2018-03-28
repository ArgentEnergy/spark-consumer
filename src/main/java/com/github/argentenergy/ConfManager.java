package com.github.argentenergy;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * The {@link ConfManager} class is used to communicate with the configuration manager to get
 * application configuration.
 */
@Configuration
public class ConfManager {

    @Value("${conf.host}")
    private String host;
    @Value("${conf.timeout}")
    private int timeout;
    @Value("${conf.max.retries}")
    private int maxRetries;
    @Value("${conf.db.node}")
    private String dbNode;
    @Value("${conf.broker.node}")
    private String brokerNode;

    /**
     * Gets the node configuration from the configuration manager.
     *
     * @return the node configuration.
     * @throws Exception If the server fails to get the configuration.
     */
    private Properties getConf(final String node) throws Exception {
        // Client is only created once by Spring
        CuratorFramework client = zkClient();
        byte[] data = client.getData().watched().forPath(node);

        Properties props = new Properties();
        try (ByteArrayInputStream bs = new ByteArrayInputStream(data)) {
            props.load(bs);
        }

        return props;
    }

    /**
     * Gets the broker configuration from the configuration manager.
     *
     * @return the broker configuration.
     * @throws Exception If the server fails to get the configuration.
     */
    public Properties getBrokerConf() throws Exception {
        return getConf(brokerNode);
    }

    /**
     * Creates the ZooKeeper client bean.
     *
     * @return the ZooKeeper client bean.
     */
    @Bean
    public CuratorFramework zkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(timeout, maxRetries);
        CuratorFramework client = CuratorFrameworkFactory.newClient(host, retryPolicy);
        client.start();
        return client;
    }

    /**
     * Creates a JDBC data source Spring bean to load using the db configuration.
     *
     * @return the JDBC data source bean.
     * @throws Exception If the server fails to get the configuration.
     */
    @Bean
    public DataSource dataSource() throws Exception {
        Properties p = getConf(dbNode);
        return DataSourceBuilder.create()
                .url(p.getProperty("spring.datasource.url"))
                .username(p.getProperty("spring.datasource.username"))
                .password(p.getProperty("spring.datasource.password"))
                .driverClassName(p.getProperty("spring.datasource.driver-class-name"))
                .build();
    }

    /**
     * Creates the Spark session Spring bean to load.
     *
     * @return the Spark session bean.
     */
    @Bean
    public SparkSession sparkSession() {
        return SparkSession
                .builder()
                .appName("spark-consumer")
                // In a real application this would be outside the application and wouldn't be local
                .master("local[*]")
                .getOrCreate();
    }
}
