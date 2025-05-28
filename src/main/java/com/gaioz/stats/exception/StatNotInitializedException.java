package com.gaioz.stats.exception;


/**
 * At least one stat must always exist in the database. - this is invariant in my application.
 * This exception is thrown when a stat is requested but has not been initialized yet.
 * It is not users responsibility to initialize the stat, it is the application's responsibility.
 * So if this exception is throws, it is a bug in the application.
 */
public class StatNotInitializedException extends IllegalStateException {
    public StatNotInitializedException(String message) {
        super(message);
    }
}
