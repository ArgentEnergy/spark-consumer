package com.github.argentenergy.services;

import com.github.argentenergy.models.Message;
import com.github.argentenergy.models.Word;
import com.github.argentenergy.sinks.JDBCSink;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The {@link AnalyticsService} class is used to run analytics over the messages retrieved from the broker(s) and
 * saves the new data in the system.
 */
@Service
public final class AnalyticsService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsService.class);

    private JDBCSink sink;

    @PostConstruct
    public void init() throws Exception {
        sink = new JDBCSink();
    }

    /**
     * Entry point into running the analytics on the messages and saving into the system.
     *
     * @param messages The messages to process.
     * @throws StreamingQueryException If the stream fails to run.
     */
    public void run(final Dataset<Row> messages) throws StreamingQueryException {
        LOG.info("Running analytics on messages.");
        Dataset<Row> lines = messages.selectExpr("CAST(value AS STRING)", "timestamp");

        // Mapping a word to a timestamp so we can use a watermark
        Dataset<Word> words = lines
                .flatMap((FlatMapFunction<Row, Word>) x -> {
                    List<String> ws = Arrays.asList(x.getString(0).split(" "));
                    return ws.stream().map(w -> new Word(w, x.getTimestamp(1))).iterator();
                }, Encoders.bean(Word.class));

        // Watermark is used to purge previous state data by Spark
        Dataset<Message> wordCounts = words.withWatermark("timestamp", "2 seconds")
                // Had to group by timestamp as well for the watermark to work
                .groupBy("value", "timestamp")
                .count()
                // Had to cast the count as an integer due to an error being thrown when using getInt
                .map((MapFunction<Row, Message>) row -> new Message(UUID.randomUUID().toString(), row.getString(0),
                        (int) row.getLong(2)), Encoders.bean(Message.class));

        // Starts the logic for getting word counts
        StreamingQuery q = wordCounts.writeStream()
                .foreach(sink)
                // Update mode makes sure the incoming message gets saved immediately
                // and doesn't use the past state stored by Spark
                .outputMode("update")
                .start();
        q.awaitTermination();
        LOG.info("Finished running analytics on messages.");
    }
}
