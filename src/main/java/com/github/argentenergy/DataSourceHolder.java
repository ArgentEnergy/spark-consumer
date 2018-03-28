package com.github.argentenergy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.Serializable;

/**
 * The {@link DataSourceHolder} class is needed to access the data source bean for the
 * {@link com.github.argentenergy.sinks.JDBCSink} to utilize connection pooling.
 */
@Service
public final class DataSourceHolder implements Serializable {

    private static DataSource ds;

    /**
     * Constructor that is used by Spring to inject the data source.
     */
    @Autowired
    public DataSourceHolder(final DataSource ds) {
        DataSourceHolder.ds = ds;
    }

    /**
     * Gets the data source.
     *
     * @return the data source.
     */
    public static DataSource getDataSource() {
        return DataSourceHolder.ds;
    }
}
