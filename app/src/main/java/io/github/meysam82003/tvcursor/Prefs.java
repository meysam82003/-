package io.github.meysam82003.tvcursor;

import android.content.Context;
import android.content.SharedPreferences;

/** Only local preferences; never synced or transmitted. */
final class Prefs {
    private final SharedPreferences p;
    Prefs(Context context) { p = context.getSharedPreferences("tv_cursor", Context.MODE_PRIVATE); }
    int speed() { return p.getInt("speed", 2); }
    void speed(int v) { p.edit().putInt("speed", Math.max(0, Math.min(4, v))).apply(); }
    float speedMultiplier() { return new float[]{0.50f, 0.75f, 1f, 1.35f, 1.75f}[speed()]; }
    int size() { return p.getInt("size", 1); }
    void size(int v) { p.edit().putInt("size", Math.max(0, Math.min(2, v))).apply(); }
    int color() { return p.getInt("color", 0); }
    void color(int v) { p.edit().putInt("color", Math.max(0, Math.min(2, v))).apply(); }
    int timeout() { return p.getInt("timeout", 20); }
    void timeout(int v) { p.edit().putInt("timeout", v).apply(); }
    boolean snap() { return p.getBoolean("snap", true); }
    void snap(boolean v) { p.edit().putBoolean("snap", v).apply(); }
    boolean highlight() { return p.getBoolean("highlight", true); }
    void highlight(boolean v) { p.edit().putBoolean("highlight", v).apply(); }
    boolean edgeScroll() { return p.getBoolean("edge_scroll", false); }
    void edgeScroll(boolean v) { p.edit().putBoolean("edge_scroll", v).apply(); }
    boolean menuToggle() { return p.getBoolean("menu_toggle", true); }
    void menuToggle(boolean v) { p.edit().putBoolean("menu_toggle", v).apply(); }
    boolean airMouse() { return p.getBoolean("air_mouse", false); }
    void airMouse(boolean v) { p.edit().putBoolean("air_mouse", v).apply(); }
    int airSensitivity() { return p.getInt("air_sensitivity", 2); }
    void airSensitivity(int v) { p.edit().putInt("air_sensitivity", Math.max(0, Math.min(4, v))).apply(); }
    float airMultiplier() { return new float[]{0.4f, 0.7f, 1.0f, 1.4f, 2.0f}[airSensitivity()]; }
    boolean autoRestore() { return p.getBoolean("auto_restore", false); }
    void autoRestore(boolean v) { p.edit().putBoolean("auto_restore", v).apply(); }
}