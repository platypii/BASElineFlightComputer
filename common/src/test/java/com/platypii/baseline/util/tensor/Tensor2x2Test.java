package com.platypii.baseline.util.tensor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are mathing correctly
 */
public class Tensor2x2Test {

    @Test
    public void initializeToIdentity() {
        Tensor2x2 t = new Tensor2x2();

        assertEquals(1, t.p11, 0.001);
        assertEquals(0, t.p12, 0.001);
        assertEquals(0, t.p21, 0.001);
        assertEquals(1, t.p22, 0.001);
    }

    @Test
    public void matrixScale() {
        Tensor2x2 t = new Tensor2x2();
        t.set(1, 2, 3, 4);
        t.scale(10);

        assertEquals(10, t.p11, 0.001);
        assertEquals(20, t.p12, 0.001);
        assertEquals(30, t.p21, 0.001);
        assertEquals(40, t.p22, 0.001);
    }

    @Test
    public void matrixAdd() {
        Tensor2x2 t1 = new Tensor2x2();
        Tensor2x2 t2 = new Tensor2x2();
        Tensor2x2 t = new Tensor2x2();

        t1.set(1, 2, 3, 4);
        t2.set(10, 20, 30, 40);
        t1.plus(t2, t);

        assertEquals(11, t.p11, 0.001);
        assertEquals(22, t.p12, 0.001);
        assertEquals(33, t.p21, 0.001);
        assertEquals(44, t.p22, 0.001);
    }

//    @Test
//    public void matrixDot2x1() {
//        Tensor2x2 t1 = new Tensor2x2();
//        Tensor2x1 t2 = new Tensor2x1();
//        Tensor2x1 t = new Tensor2x1();
//
//        t1.set(1, 2, 3, 4);
//        t2.set(10, 20);
//        t1.dot(t2, t);
//
//        assertEquals(50, t.p1, 0.001);
//        assertEquals(110, t.p2, 0.001);
//    }

    @Test
    public void matrixDot2x2() {
        Tensor2x2 t1 = new Tensor2x2();
        Tensor2x2 t2 = new Tensor2x2();
        Tensor2x2 t = new Tensor2x2();

        t1.set(1, 2, 3, 4);
        t2.set(10, 20, 30, 40);
        t1.dot(t2, t);

        assertEquals(70, t.p11, 0.001);
        assertEquals(100, t.p12, 0.001);
        assertEquals(150, t.p21, 0.001);
        assertEquals(220, t.p22, 0.001);
    }

    @Test
    public void matrixDotTranspose() {
        Tensor2x2 t1 = new Tensor2x2();
        Tensor2x2 t2 = new Tensor2x2();
        Tensor2x2 t = new Tensor2x2();

        t1.set(1, 2, 3, 4);
        t2.set(10, 20, 30, 40);
        t1.dotTranspose(t2, t);

        assertEquals(50, t.p11, 0.001);
        assertEquals(110, t.p12, 0.001);
        assertEquals(110, t.p21, 0.001);
        assertEquals(250, t.p22, 0.001);
    }

    @Test
    public void matrixToString() {
        Tensor2x2 t = new Tensor2x2();
        t.set(1, 2, 3, 4);

        assertEquals("[[1.000000,2.000000],[3.000000,4.000000]]", t.toString());
    }

}
