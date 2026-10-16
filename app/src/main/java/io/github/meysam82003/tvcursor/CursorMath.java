package io.github.meysam82003.tvcursor;

/** Plain-Java movement and hit-test math (unit-testable without Android). */
public final class CursorMath {
    private CursorMath() {}
    public static float clamp(float n, float lo, float hi) { return Math.max(lo, Math.min(hi, n)); }
    public static float framePixels(float heldSeconds, float frameSeconds, float speed) {
        float ramp = clamp(heldSeconds / 1.20f, 0f, 1f);
        float velocity = (230f + 860f * ramp * ramp) * speed;
        return velocity * clamp(frameSeconds, 0f, 0.05f);
    }
    public static float squaredDistanceToRect(float x, float y, int left, int top, int right, int bottom) {
        float dx = x < left ? left - x : x > right ? x - right : 0f;
        float dy = y < top ? top - y : y > bottom ? y - bottom : 0f;
        return dx * dx + dy * dy;
    }
    public static float diagonalFactor(int dx, int dy) { return dx != 0 && dy != 0 ? 0.70710678f : 1f; }
}