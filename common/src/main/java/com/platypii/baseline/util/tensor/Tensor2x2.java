package com.platypii.baseline.util.tensor;

import androidx.annotation.NonNull;
import java.util.Locale;

/**
 * Represents a fast 2x2 matrix
 *
 * [p11 p12]
 * [p21 p22]
 */
public class Tensor2x2 {

    // Initialize to identity
    public double p11 = 1;
    public double p12 = 0;
    public double p21 = 0;
    public double p22 = 1;

    public void set(double q11, double q12, double q21, double q22) {
        p11 = q11;
        p12 = q12;
        p21 = q21;
        p22 = q22;
    }

    /**
     * this plus mat -> output
     * Writes to an output tensor because we want to avoid allocating memory. Self is ok.
     *
     * @param mat the matrix to dot against
     * @param output matrix to store the output (this okay)
     */
    public void plus(@NonNull Tensor2x2 mat, @NonNull Tensor2x2 output) {
        output.p11 = p11 + mat.p11;
        output.p12 = p12 + mat.p12;
        output.p21 = p21 + mat.p21;
        output.p22 = p22 + mat.p22;
    }

    /**
     * this dot mat -> output
     * Writes to an output tensor because we want to avoid allocating memory. Self is ok.
     *
     * @param mat the matrix to dot against
     * @param output matrix to store the output (this okay)
     */
    public void dot(@NonNull Tensor2x1 mat, @NonNull Tensor2x1 output) {
        final double q1 = p11 * mat.p1 + p12 * mat.p2;
        final double q2 = p21 * mat.p1 + p22 * mat.p2;
        output.set(q1, q2);
    }

    /**
     * this dot mat -> output
     * Writes to an output tensor because we want to avoid allocating memory. Self is ok.
     *
     * @param mat the matrix to dot against
     * @param output matrix to store the output (this okay)
     */
    public void dot(@NonNull Tensor2x2 mat, @NonNull Tensor2x2 output) {
        final double q11 = p11 * mat.p11 + p12 * mat.p21;
        final double q12 = p11 * mat.p12 + p12 * mat.p22;
        final double q21 = p21 * mat.p11 + p22 * mat.p21;
        final double q22 = p21 * mat.p12 + p22 * mat.p22;
        output.p11 = q11;
        output.p12 = q12;
        output.p21 = q21;
        output.p22 = q22;
    }

    /**
     * this dot mat^T -> output
     * Writes to an output tensor because we want to avoid allocating memory. Self is ok.
     *
     * @param mat the matrix to dot against
     * @param output matrix to store the output (this okay)
     */
    public void dotTranspose(@NonNull Tensor2x2 mat, @NonNull Tensor2x2 output) {
        final double q11 = p11 * mat.p11 + p12 * mat.p12;
        final double q12 = p11 * mat.p21 + p12 * mat.p22;
        final double q21 = p21 * mat.p11 + p22 * mat.p12;
        final double q22 = p21 * mat.p21 + p22 * mat.p22;
        output.p11 = q11;
        output.p12 = q12;
        output.p21 = q21;
        output.p22 = q22;
    }

    public void scale(double factor) {
        p11 *= factor;
        p12 *= factor;
        p21 *= factor;
        p22 *= factor;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "[[%f,%f],[%f,%f]]", p11, p12, p21, p22);
    }

}
