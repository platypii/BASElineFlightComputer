package com.platypii.baseline.util;

/**
 * A Try<T> is either a Success<T>(T result) or Failure<T>(String error)
 * @param <T> the type of the result if successful
 */
public abstract class Try<T> {

    public static class Success<T2> extends Try<T2> {
        public final T2 result;
        public Success(T2 result) {
            this.result = result;
        }
        @Override
        public String toString() {
            return "Success(" + result + ")";
        }
    }

    public static class Failure<T2> extends Try<T2> {
        public final String error;
        public Failure(String msg) {
            this.error = msg;
        }
        @Override
        public String toString() {
            return "Failure(" + error + ")";
        }
    }

}
