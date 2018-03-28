package com.github.argentenergy.models;

import java.io.Serializable;

/**
 * The {@link Message} class is a database representation of the message analytics.
 */
public final class Message implements Serializable {

    // Had to change from UUID to String to satisfy Spark class generation
    private String id;
    private String word;
    private int wordCount;

    /**
     * Default constructor that is needed by Spark.
     */
    public Message() {
    }

    /**
     * Creates a message object with the provided id, word, and word count.
     *
     * @param id        The message id.
     * @param word      The word.
     * @param wordCount The word count.
     */
    public Message(final String id, final String word, final int wordCount) {
        this.id = id;
        this.word = word;
        this.wordCount = wordCount;
    }

    /**
     * Gets the id of the message. Needed by Spark.
     *
     * @return the message id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the message. Needed by Spark.
     *
     * @param id The message id.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the word in the message. Needed by Spark.
     *
     * @return the message word.
     */
    public String getWord() {
        return word;
    }

    /**
     * Sets the word in the message. Needed by Spark.
     *
     * @param word The word in the message.
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Gets the word count in the message. Needed by Spark.
     *
     * @return the word count in the message.
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Sets the word count in the message. Needed by Spark.
     *
     * @param wordCount The word count in the message.
     */
    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
