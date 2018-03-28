package com.github.argentenergy.models;

import java.sql.Timestamp;

/**
 * The {@link Word} class is used to convert a Spark row to this object.
 */
public final class Word {

    private String value;
    private Timestamp timestamp;

    /**
     * Default constructor that is needed by Spark.
     */
    public Word() {
    }

    /**
     * Creates a word object with the value and timestamp.
     *
     * @param value     The actual word.
     * @param timestamp The timestamp of the message in Kafka.
     */
    public Word(final String value, final Timestamp timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    /**
     * Gets the actual word. Needed by Spark.
     *
     * @return the actual word.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the actual word. Needed by Spark.
     *
     * @param value The actual word.
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the message timestamp. Needed by Spark.
     *
     * @return the message timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the message timestamp. Needed by Spark.
     *
     * @param timestamp The message timestamp.
     */
    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
