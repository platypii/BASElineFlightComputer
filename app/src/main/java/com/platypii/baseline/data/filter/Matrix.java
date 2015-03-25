package com.platypii.baseline.data.filter;

/**
 * Basic matrix operations
 * @author platypii
 */
public class Matrix {
    public int rows;
    public int cols;
    public double data[][];
    
    
    public Matrix(double data[][]) {
        // TODO: check dimensions
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = data;
    }

    /**
     * Create a new zero matrix
     */
    public Matrix(int rows, int cols) {
        if(rows < 0 || cols < 0)
            throw new IllegalArgumentException("Matrix dimensions must be non-negative!");
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }
    
    /**
     * Create a new identity matrix
     * @param size The dimensions of the (square) identity matrix
     */
    public Matrix(int size) {
        this.rows = size;
        this.cols = size;
        this.data = new double[size][size];
        for(int i = 0; i < size; i++)
            data[i][i] = 1;
    }
    
    /**
     * Matrix transpose
     */
    public Matrix transpose() {
        Matrix m = new Matrix(cols, rows);
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                m.data[j][i] = data[i][j];
            }
        }
        return m;
    }
    
    /**
     * Matrix addition
     */
    public Matrix plus(Matrix y) {
        if(rows != y.rows || cols != y.cols)
            throw new IllegalArgumentException("Matrix dimensions do not match!");
        Matrix m = new Matrix(rows, cols);
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                m.data[i][j] = data[i][j] + y.data[i][j];
                assert !Double.isNaN(m.data[i][j]);
            }
        }
        return m;
    }
    
    /**
     * Matrix subtraction
     */
    public Matrix minus(Matrix y) {
        if(rows != y.rows || cols != y.cols)
            throw new IllegalArgumentException("Matrix dimensions do not match!");
        Matrix m = new Matrix(rows, cols);
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                m.data[i][j] = data[i][j] + y.data[i][j];
                assert !Double.isNaN(m.data[i][j]);
            }
        }
        return m;
    }
    
    /**
     * Matrix dot product
     */
    public Matrix dot(Matrix y) {
        if(this.cols != y.rows)
            throw new IllegalArgumentException("Invalid matrix dimensions in dot-product!");
        Matrix m = new Matrix(this.rows, y.cols);
        for(int i = 0; i < this.rows; i++) {
            for(int j = 0; j < y.cols; j++) {
                for(int k = 0; k < this.cols; k++) {
                    m.data[i][j] += data[i][k] * y.data[k][j];
                }
                assert !Double.isNaN(m.data[i][j]);
            }
        }
        return m;
    }
    
    /**
     * Scale this matrix by a constant factor
     */
    public Matrix scale(double y) {
        Matrix m = new Matrix(rows, cols);
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                m.data[i][j] = data[i][j] * y;
                assert !Double.isNaN(m.data[i][j]);
            }
        }
        return m;
    }
    
    /**
     * Compute the inverse of this matrix
     * @return A Matrix which is the inverse of this
     */
    public Matrix inverse() {
        if(rows != cols)
            throw new IllegalArgumentException("Non-square matrix in inverse!");
        assert rows <= 3;
        if(cols == 1) {
            Matrix m = new Matrix(1,1);
            m.data[0][0] = Math.sqrt(data[0][0]);
            return m;
        } else if(cols == 2) {
        	// Determinant
            double det = data[0][0] * data[1][1] - data[0][1] * data[1][0];
            if(det == 0) {
                return null;
            } else {
                Matrix m = new Matrix(2,2);
                m.data[0][0] = data[1][1];
                m.data[0][1] = -data[0][1];
                m.data[1][0] = -data[1][0];
                m.data[1][1] = data[0][0];
                return m.scale(1/det);
            }
        } else if(cols == 3) {
            // Use API inverse
            android.graphics.Matrix androidMatrix = new android.graphics.Matrix();
            float linearMatrix[] = new float[9];
            for(int i = 0; i < rows; i++)
                for(int j = 0; j < cols; j++)
                    linearMatrix[i * cols + j] = (float) data[i][j];
            androidMatrix.setValues(linearMatrix);
            android.graphics.Matrix inverseAndroidMatrix = new android.graphics.Matrix();
            if(androidMatrix.invert(inverseAndroidMatrix)) {
                Matrix m = new Matrix(rows, cols);
                for(int i = 0; i < rows; i++)
                    for(int j = 0; j < cols; j++)
                        m.data[i][j] = linearMatrix[i * cols + j];
                return m;
            } else {
                return null;
            }
        } else {
            // TODO: dimensions = 4+
            return null;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < rows; i++) {
            str.append('[');
            for(int j = 0; j < cols; j++) {
                str.append(data[i][j]);
                str.append(' ');
            }
            str.append(']');
        }
        return str.toString();
    }
}
