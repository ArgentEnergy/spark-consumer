package com.github.argentenergy.sinks;

import com.github.argentenergy.DataSourceHolder;
import com.github.argentenergy.models.Message;
import org.apache.spark.sql.ForeachWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * The {@link JDBCSink} class is needed to save stream data to the database. This class can be removed once Spark
 * supports this in their API. Reference:
 * <pre>
 *  <a href="https://databricks.com/blog/2017/04/04/real-time-end-to-end-integration-with-apache-kafka-in-
 *     apache-sparks-structured-streaming.html">Spark Integration With Kafka</a>
 * </pre>
 */
public class JDBCSink extends ForeachWriter<Message> {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCSink.class);

    private Optional<Connection> connection;
    private PreparedStatement s;

    @Override
    public boolean open(final long l, final long l1) {
        try {
            connection = Optional.of(DataSourceHolder.getDataSource().getConnection());
            s = connection.get().prepareStatement("INSERT INTO message (id, word, word_count) VALUES (?, ?, ?)");
        } catch (SQLException e) {
            LOG.warn("Failed to open the connection!", e);
            connection = Optional.empty();
        }

        return true;
    }

    @Override
    public void process(final Message m) {
        try {
            s.setObject(1, UUID.fromString(m.getId()));
            s.setString(2, m.getWord());
            s.setInt(3, m.getWordCount());
            s.execute();
        } catch (SQLException e) {
            LOG.warn("Failed to save the data!", e);
        }
    }

    @Override
    public void close(final Throwable throwable) {
        try {
            if (connection.isPresent()) {
                s.close();
                connection.get().close();
            }
        } catch (final SQLException e) {
            LOG.warn("Failed to close the connection!", e);
        }
    }
}
