package com.gaioz.stats.exception;

/**
 * Exception thrown when a timeout occurs while trying to get statistics.
 * This happens when thread is trying to aquire lock on a stat or get a stat from the database.
 * If the timeout is reached, this exception is thrown.
 * This is system fault, not user fault.
 * Easy reason this could happen in dev mode is that retry interval is shorter than the
 * simulated delay in the database.
 */
public class GetStatTimeoutException extends RuntimeException {
    public GetStatTimeoutException(String message) {
        super(message);
    }
}
