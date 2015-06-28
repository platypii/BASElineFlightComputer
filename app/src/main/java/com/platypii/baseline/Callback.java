package com.platypii.baseline;

public interface Callback<T> {
    /** Success case */
    void apply(T t);

    /** Error case */
    void error(String error);
}
