package com.platypii.baseline.util.tensor;

import com.platypii.baseline.util.Numbers;
import android.support.annotation.NonNull;
import java.util.Locale;

/**
 * Represents a fast 2x1 matrix
 *
 * [p1]
 * [p2]
 */
public class Tensor2x1 {

    public double p1 = 1;
    public double p2 = 0;

    /**
     * Return true iff all numbers are real
     */
    public boolean isReal() {
        return Numbers.isReal(p1) && Numbers.isReal(p2);
    }

    public void set(double z, double v) {
        p1 = z;
        p2 = v;
    }

    /**
     * this dot mat -> output
     * Writes to an output tensor because we want to avoid allocating memory.
     * @param mat the matrix to dot against
     * @param output matrix to store the output
     */
    public void dot(@NonNull Tensor1x2 mat, @NonNull Tensor2x2 output) {
        output.set(
                p1 * mat.p1, p1 * mat.p2,
                p2 * mat.p1, p2 * mat.p2
        );
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "[[%f],[%f]]", p1, p2);
    }

}
