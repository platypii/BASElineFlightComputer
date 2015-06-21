package com.platypii.baseline;

public class Try<T> {

    public static class Success<T2> extends Try<T2> {
        private T2 result;
        public Success(T2 result) {
            this.result = result;
        }
        @Override
        public String toString() {
            return "Success(" + result + ")";
        }
    }

    public static class Failure<T2> extends Try<T2> {
        private String msg;
        public Failure(String msg) {
            this.msg = msg;
        }
        @Override
        public String toString() {
            return "Failure(" + msg + ")";
        }
    }

}
