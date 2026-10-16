package io.github.meysam82003.tvcursor;

import org.junit.Test;
import static org.junit.Assert.*;

public class CursorMathTest {
    @Test public void clamps() {
        assertEquals(0f, CursorMath.clamp(-1, 0, 100), 0.01f);
        assertEquals(100f, CursorMath.clamp(101, 0, 100), 0.01f);
    }
    @Test public void accelerates() {
        assertTrue(CursorMath.framePixels(1.3f, 0.016f, 1f) > CursorMath.framePixels(0f, 0.016f, 1f));
        assertEquals(0f, CursorMath.framePixels(0f, -1f, 1f), 0.01f);
    }
    @Test public void hitTests() {
        assertEquals(0f, CursorMath.squaredDistanceToRect(5, 5, 0, 0, 10, 10), 0.01f);
        assertEquals(100f, CursorMath.squaredDistanceToRect(20, 5, 0, 0, 10, 10), 0.01f);
        assertEquals(0.70710678f, CursorMath.diagonalFactor(1, 1), 0.00001f);
    }
}