package com.platypii.baseline;

public class Try<T> {

    public static class Success<T2> extends Try<T2> {
        public T2 result;
        public Success(T2 result) {
            this.result = result;
        }
        @Override
        public String toString() {
            return "Success(" + result + ")";
        }
    }

    public static class Failure<T2> extends Try<T2> {
        public String error;
        public Failure(String msg) {
            this.error = error;
        }
        @Override
        public String toString() {
            return "Failure(" + error + ")";
        }
    }

}
