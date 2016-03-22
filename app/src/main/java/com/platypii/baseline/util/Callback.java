package com.platypii.baseline.util;

public interface Callback<T> {
    /** Success case */
    void apply(T t);

    /** Error case */
    void error(String error);
}
