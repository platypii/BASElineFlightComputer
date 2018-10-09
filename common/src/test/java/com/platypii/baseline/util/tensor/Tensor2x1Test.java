package com.platypii.baseline.util.tensor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Ensure that we are mathing correctly
 */
public class Tensor2x1Test {

    @Test
    public void initializeToIdentity() {
        Tensor2x1 t = new Tensor2x1();

        assertEquals(1, t.p1, 0.001);
        assertEquals(0, t.p2, 0.001);
    }

    @Test
    public void beReal() {
        Tensor2x1 t = new Tensor2x1();
        assertTrue(t.isReal());
    }

    @Test
    public void beUnReal() {
        Tensor2x1 t = new Tensor2x1();
        t.p2 = Double.NaN;
        assertFalse(t.isReal());
    }

    @Test
    public void matrixDot1x2() {
        Tensor2x1 t1 = new Tensor2x1();
        Tensor1x2 t2 = new Tensor1x2();
        Tensor2x2 t = new Tensor2x2();

        t1.set(1, 2);
        t2.set(10, 20);
        t1.dot(t2, t);

        assertEquals(10, t.p11, 0.001);
        assertEquals(20, t.p12, 0.001);
        assertEquals(20, t.p21, 0.001);
        assertEquals(40, t.p22, 0.001);
    }

    @Test
    public void matrixToString() {
        Tensor2x1 t = new Tensor2x1();
        t.set(1, 2);

        assertEquals("[[1.000000],[2.000000]]", t.toString());
    }

}
