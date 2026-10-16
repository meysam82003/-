package io.github.meysam82003.tvcursor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;

/** Remote-driven global cursor. User must explicitly enable Accessibility service. */
public final class MouseAccessibilityService extends AccessibilityService {
    static volatile MouseAccessibilityService running;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private HandlerThread nodeThread;
    private Handler nodeHandler;
    private final boolean[] pressed = new boolean[4];
    private Prefs prefs;
    private WindowManager wm;
    private CursorOverlayView overlay;
    private boolean active, scrolling, dashboardVisible, centerDown, centerHeld, backDown;
    private long backStart, holdStart, frameLast, edgeSince, lastEdgeScroll;
    private int screenW, screenH, hoverVersion;
    private Runnable pendingHover;
    private float px, py;
    private long lastMotionClick;
    private boolean motionButtonDown;
    private boolean motionSeen;
    private final Runnable hideTask = () -> { if (overlay != null) overlay.visible(false); };
    private final Runnable centerHoldTask = () -> {
        if (!centerDown || !active) return;
        centerHeld = true; scrolling = !scrolling;
        if (overlay != null) overlay.scrollMode(scrolling);
        wake();
    };
    private final Runnable frames = new Runnable() {
        @Override public void run() {
            if (!active || !moving()) { frameLast = 0; return; }
            long now = SystemClock.uptimeMillis();
            if (frameLast == 0) frameLast = now - 16;
            float dt = (now - frameLast) / 1000f;
            frameLast = now;
            int dx = (pressed[3] ? 1 : 0) - (pressed[2] ? 1 : 0);
            int dy = (pressed[1] ? 1 : 0) - (pressed[0] ? 1 : 0);
            if (!scrolling) {
                float n = CursorMath.framePixels((now - holdStart) / 1000f, dt, prefs.speedMultiplier());
                float factor = CursorMath.diagonalFactor(dx, dy);
                moveTo(px + n * dx * factor, py + n * dy * factor);
                maybeEdgeScroll(dx, dy, now);
            }
            ui.postDelayed(this, 16);
        }
    };

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        running = this;
        prefs = new Prefs(this);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        nodeThread = new HandlerThread("Cursor-Accessibility-Nodes"); nodeThread.start();
        nodeHandler = new Handler(nodeThread.getLooper());
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            setServiceInfo(info);
        }
        measureDisplay(); px = screenW * 0.5f; py = screenH * 0.5f;
        configureAirInput();
        if (prefs.autoRestore()) ui.postDelayed(this::enable, 350);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { /* Do not store page text or events. */ }
    @Override public void onInterrupt() { disable(); }

    @Override protected boolean onKeyEvent(KeyEvent event) {
        if (dashboardVisible) return false; // Let native focus work inside app settings.
        int key = event.getKeyCode();
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        // Optional dedicated remote MENU shortcut. Always consume a complete down/up stream.
        if (key == KeyEvent.KEYCODE_MENU && prefs != null && prefs.menuToggle()) {
            if (up) toggle();
            return true;
        }
        if (!active) return false;
        if (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_ESCAPE) {
            if (down && !backDown) { backDown = true; backStart = event.getEventTime(); }
            if (up && backDown) {
                backDown = false;
                if (event.getEventTime() - backStart >= 800) disable();
                else performGlobalAction(GLOBAL_ACTION_BACK);
            }
            return true;
        }
        if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER || key == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            if (down && !centerDown) {
                centerDown = true; centerHeld = false;
                ui.postDelayed(centerHoldTask, 700);
            }
            if (up && centerDown) {
                ui.removeCallbacks(centerHoldTask);
                centerDown = false;
                if (!centerHeld) {
                    long now = SystemClock.uptimeMillis();
                    if (now - lastMotionClick > 220) { lastMotionClick = now; click(); }
                }
            }
            return true;
        }
        int idx = switch(key) {
            case KeyEvent.KEYCODE_DPAD_UP -> 0;
            case KeyEvent.KEYCODE_DPAD_DOWN -> 1;
            case KeyEvent.KEYCODE_DPAD_LEFT -> 2;
            case KeyEvent.KEYCODE_DPAD_RIGHT -> 3;
            default -> -1;
        };
        if (idx < 0) return false;
        if (down && !pressed[idx]) {
            if (scrolling) {
                if (idx == 0 || idx == 1) scroll(idx == 1);
            } else {
                boolean wasMoving = moving();
                pressed[idx] = true;
                if (!wasMoving) { holdStart = SystemClock.uptimeMillis(); frameLast = 0; }
                int x = (pressed[3] ? 1 : 0) - (pressed[2] ? 1 : 0);
                int y = (pressed[1] ? 1 : 0) - (pressed[0] ? 1 : 0);
                moveTo(px + x * 12f * prefs.speedMultiplier(), py + y * 12f * prefs.speedMultiplier());
                if (!wasMoving) ui.post(frames);
            }
        } else if (up) { pressed[idx] = false; edgeSince = 0; }
        wake();
        return true;
    }
    void dashboard(boolean visible) {
        dashboardVisible = visible;
        configureAirInput();
    }

    /** Mouse HID events are available to accessibility services on Android 14+.
     * This does NOT read raw gyroscope/accelerometer data from a remote. */
    @Override public void onMotionEvent(MotionEvent event) {
        if (Build.VERSION.SDK_INT < 34 || !active || dashboardVisible || !prefs.airMouse()) return;
        int source = event.getSource();
        boolean absolute = (source & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE;
        boolean relative = (source & InputDevice.SOURCE_MOUSE_RELATIVE) == InputDevice.SOURCE_MOUSE_RELATIVE;
        if (!absolute && !relative) return;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_HOVER_MOVE || action == MotionEvent.ACTION_MOVE) {
            float x, y;
            if (relative) {
                // Some HID remotes report relative delta axes, not display coordinates.
                x = px + event.getAxisValue(MotionEvent.AXIS_RELATIVE_X) * prefs.airMultiplier();
                y = py + event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y) * prefs.airMultiplier();
            } else {
                // Absolute cursor location is already in screen pixels.
                x = px + (event.getX() - px) * prefs.airMultiplier();
                y = py + (event.getY() - py) * prefs.airMultiplier();
            }
            if (Float.isFinite(x) && Float.isFinite(y)) {
                motionSeen = true;
                moveTo(x, y);
                wake();
            }
        } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_BUTTON_PRESS) {
            if (!motionButtonDown && (event.getButtonState() & MotionEvent.BUTTON_PRIMARY) != 0) {
                motionButtonDown = true;
                long now = SystemClock.uptimeMillis();
                if (now - lastMotionClick > 220) { lastMotionClick = now; click(); }
            }
        } else if (action == MotionEvent.ACTION_BUTTON_RELEASE || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            motionButtonDown = false;
        } else if (action == MotionEvent.ACTION_SCROLL) {
            float wheel = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (wheel != 0f) scroll(wheel < 0);
        }
    }

    boolean hasObservedMotion() { return motionSeen; }

    /** Avoid stealing mouse input when air mode is disabled or the dashboard is open. */
    private void configureAirInput() {
        if (Build.VERSION.SDK_INT < 34 || prefs == null) return;
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) return;
        info.setMotionEventSources(active && !dashboardVisible && prefs.airMouse()
                ? (InputDevice.SOURCE_MOUSE | InputDevice.SOURCE_MOUSE_RELATIVE) : 0);
        setServiceInfo(info);
    }
    boolean isActive() { return active; }
    boolean isScrolling() { return scrolling; }
    void toggle() { if (active) disable(); else enable(); }
    void updateSettings() {
        if (overlay != null) overlay.appearance(prefs.size(), prefs.color(), prefs.highlight());
        configureAirInput();
        if (active) wake();
    }
    private void measureDisplay() {
        Point p = new Point();
        // Legacy API intentionally used for Android 9 (API 28) support on TVs.
        wm.getDefaultDisplay().getRealSize(p);
        screenW = Math.max(100, p.x); screenH = Math.max(100, p.y);
    }
    private void enable() {
        if (active || wm == null) return;
        measureDisplay();
        if (px < 0 || px >= screenW) px = screenW / 2f;
        if (py < 0 || py >= screenH) py = screenH / 2f;
        CursorOverlayView v = new CursorOverlayView(this);
        v.appearance(prefs.size(), prefs.color(), prefs.highlight());
        v.pointer(px, py);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.setTitle("TV Remote Cursor pointer");
        try { wm.addView(v, lp); overlay = v; active = true; scrolling = false; configureAirInput(); wake(); updateHover(); }
        catch (RuntimeException ignored) { overlay = null; active = false; }
    }
    private void disable() {
        active = false; scrolling = false; centerDown = false; backDown = false; motionButtonDown = false;
        configureAirInput();
        for (int i = 0; i < 4; i++) pressed[i] = false;
        ui.removeCallbacks(frames); ui.removeCallbacks(hideTask); ui.removeCallbacks(centerHoldTask);
        frameLast = 0; hoverVersion++;
        if (overlay != null) {
            try { wm.removeView(overlay); } catch (RuntimeException ignored) { }
            overlay = null;
        }
    }
    private boolean moving() {
        for (boolean b : pressed) if (b) return true;
        return false;
    }
    private void moveTo(float x, float y) {
        px = CursorMath.clamp(x, 0, screenW - 30f);
        py = CursorMath.clamp(y, 0, screenH - 50f);
        if (overlay != null) overlay.pointer(px, py);
        updateHover();
    }
    private void wake() {
        if (overlay == null) return;
        overlay.visible(true);
        ui.removeCallbacks(hideTask);
        if (prefs.timeout() > 0) ui.postDelayed(hideTask, prefs.timeout() * 1000L);
    }
    private void maybeEdgeScroll(int dx, int dy, long now) {
        if (!prefs.edgeScroll() || dx != 0 || dy == 0) { edgeSince = 0; return; }
        boolean atEdge = dy > 0 ? py > screenH - 58f : py < 9f;
        if (!atEdge) { edgeSince = 0; return; }
        if (edgeSince == 0) edgeSince = now;
        if (now - edgeSince >= 650 && now - lastEdgeScroll >= 900) {
            lastEdgeScroll = now; scroll(dy > 0);
        }
    }
    private void updateHover() {
        if (nodeHandler == null || overlay == null || !prefs.highlight()) return;
        int token = ++hoverVersion;
        float x = px, y = py;
        if (pendingHover != null) nodeHandler.removeCallbacks(pendingHover);
        pendingHover = () -> {
            Target t = findTarget(x, y, prefs.snap());
            Rect bounds = t == null ? null : new Rect(t.bounds);
            if (t != null) t.release();
            ui.post(() -> { if (active && overlay != null && token == hoverVersion) overlay.target(bounds); });
        };
        nodeHandler.postDelayed(pendingHover, 110);
    }
    private void click() {
        if (nodeHandler == null) return;
        wake();
        float x = px, y = py;
        nodeHandler.post(() -> {
            Target t = findTarget(x, y, prefs.snap());
            boolean ok = false;
            if (t != null) {
                try { ok = t.node.performAction(AccessibilityNodeInfo.ACTION_CLICK); }
                catch (RuntimeException ignored) { }
                t.release();
            }
            if (!ok) ui.post(() -> { if (active) gesture(x, y, x, y, 75); });
        });
    }
    private void scroll(boolean forward) {
        if (nodeHandler == null) return;
        float x = px, y = py;
        nodeHandler.post(() -> {
            boolean scrolled = false;
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                try { scrolled = scrollInside(root, x, y, forward, 0); }
                finally { root.recycle(); }
            }
            if (!scrolled) ui.post(() -> {
                if (!active) return;
                float distance = Math.min(270f, screenH * 0.28f);
                float startY = CursorMath.clamp(y, distance + 15, screenH - distance - 15);
                gesture(x, startY, x, startY + (forward ? -distance : distance), 290);
            });
        });
    }
    private boolean scrollInside(AccessibilityNodeInfo node, float x, float y, boolean forward, int depth) {
        if (depth > 24 || !node.isVisibleToUser()) return false;
        Rect r = new Rect(); node.getBoundsInScreen(r);
        if (!r.contains((int)x, (int)y)) return false;
        for (int i = node.getChildCount() - 1; i >= 0; i--) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            try { if (scrollInside(child, x, y, forward, depth + 1)) return true; }
            finally { child.recycle(); }
        }
        return node.isScrollable() && node.performAction(forward ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }
    private Target findTarget(float x, float y, boolean snap) {
        float radius = snap ? Math.min(screenW * .045f, 75f) : 0f;
        Target best = null;
        try {
            List<AccessibilityWindowInfo> windows = getWindows();
            if (windows != null) {
                for (AccessibilityWindowInfo w : windows) {
                    if (w.getType() == AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY) continue;
                    AccessibilityNodeInfo root = w.getRoot();
                    if (root == null) continue;
                    try { best = traverse(root, x, y, radius, 0, best); }
                    finally { root.recycle(); }
                    if (best != null && best.distance == 0) break;
                }
            }
            if (best == null) {
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root != null) {
                    try { best = traverse(root, x, y, radius, 0, best); }
                    finally { root.recycle(); }
                }
            }
        } catch (RuntimeException ignored) { }
        return best;
    }
    private Target traverse(AccessibilityNodeInfo node, float x, float y, float radius, int depth, Target best) {
        if (depth > 28 || !node.isVisibleToUser()) return best;
        Rect r = new Rect(); node.getBoundsInScreen(r);
        if (r.isEmpty() || x < r.left - radius || x > r.right + radius ||
                y < r.top - radius || y > r.bottom + radius) return best;
        if (node.isClickable() && node.isEnabled()) {
            float d2 = CursorMath.squaredDistanceToRect(x, y, r.left, r.top, r.right, r.bottom);
            long area = (long) r.width() * r.height();
            if (d2 <= radius * radius && (best == null || d2 < best.distance || (d2 == best.distance && area < best.area))) {
                if (best != null) best.release();
                best = new Target(AccessibilityNodeInfo.obtain(node), new Rect(r), d2, area);
            }
        }
        int count = Math.min(node.getChildCount(), 100);
        for (int i = count - 1; i >= 0; i--) {
            AccessibilityNodeInfo c = node.getChild(i);
            if (c == null) continue;
            try { best = traverse(c, x, y, radius, depth + 1, best); }
            finally { c.recycle(); }
        }
        return best;
    }
    private void gesture(float sx, float sy, float ex, float ey, long millis) {
        Path path = new Path(); path.moveTo(sx, sy);
        if (sx != ex || sy != ey) path.lineTo(ex, ey);
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, millis);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
    }
    private static final class Target {
        final AccessibilityNodeInfo node; final Rect bounds; final float distance; final long area;
        Target(AccessibilityNodeInfo n, Rect r, float d, long a) { node=n; bounds=r; distance=d; area=a; }
        void release() { node.recycle(); }
    }
    @Override public void onDestroy() {
        disable();
        if (nodeHandler != null) nodeHandler.removeCallbacksAndMessages(null);
        if (nodeThread != null) nodeThread.quitSafely();
        if (running == this) running = null;
        super.onDestroy();
    }
}