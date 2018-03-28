package com.github.argentenergy.services;

import com.github.argentenergy.ConfManager;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * The {@link BrokerService} class is used to communicate with the broker to retrieve the messages to stream.
 */
@Service
public final class BrokerService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerService.class);

    @Autowired
    private ConfManager confManager;
    @Autowired
    private SparkSession session;
    private Properties conf;

    /**
     * Initializes any internal resources before use.
     *
     * @throws Exception If the server fails to get the configuration.
     */
    @PostConstruct
    public void init() throws Exception {
        this.conf = confManager.getBrokerConf();
    }

    /**
     * Gets the messages from the brokers and topics.
     *
     * @return the messages.
     */
    public Dataset<Row> getMessages() {
        LOG.info("Getting messages from broker(s).");

        Dataset<Row> messages = session
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", conf.getProperty("brokers"))
                .option("subscribe", conf.getProperty("topics"))
                .load();

        LOG.info("Finished getting messages from broker(s).");
        return messages;
    }
}
