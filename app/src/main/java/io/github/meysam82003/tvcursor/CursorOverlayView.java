package io.github.meysam82003.tvcursor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

/** Non-touchable, non-focusable accessibility overlay: no extra overlay permission. */
final class CursorOverlayView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow = new Path();
    private final Rect target = new Rect();
    private float x, y, scale = 1f;
    private int color = 0xFF53EAE2;
    private boolean visible = true, scrolling, showHighlight = true, hasTarget;

    CursorOverlayView(Context c) {
        super(c);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        arrow.moveTo(0f, 0f); arrow.lineTo(0f, 34f); arrow.lineTo(9f, 26f);
        arrow.lineTo(17f, 42f); arrow.lineTo(24f, 38f); arrow.lineTo(16f, 24f);
        arrow.lineTo(27f, 24f); arrow.close();
    }
    void pointer(float px, float py) { x = px; y = py; invalidate(); }
    void visible(boolean v) { visible = v; invalidate(); }
    void scrollMode(boolean v) { scrolling = v; invalidate(); }
    void appearance(int sizeIndex, int colorIndex, boolean highlight) {
        scale = new float[]{0.85f, 1.15f, 1.52f}[sizeIndex];
        color = new int[]{0xFF53EAE2, 0xFFC49AFF, 0xFFFFD378}[colorIndex];
        showHighlight = highlight;
        invalidate();
    }
    void target(Rect r) {
        hasTarget = r != null && !r.isEmpty();
        if (hasTarget) target.set(r);
        invalidate();
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (!visible) return;
        if (hasTarget && showHighlight) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.4f * getResources().getDisplayMetrics().density);
            paint.setColor(color);
            c.drawRoundRect(target.left - 3, target.top - 3, target.right + 3, target.bottom + 3, 10, 10, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        c.save(); c.translate(x, y); c.scale(scale, scale);
        paint.setStyle(Paint.Style.FILL); paint.setColor(0x77000000);
        c.drawCircle(12f, 15f, 26f, paint);
        paint.setColor(0xFF0C1628); c.drawPath(arrow, paint);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f); paint.setColor(0xFFFFFFFF); c.drawPath(arrow, paint);
        paint.setStrokeWidth(2.4f); paint.setColor(color); c.drawPath(arrow, paint);
        paint.setStyle(Paint.Style.FILL);
        if (scrolling) {
            paint.setColor(0xEF182B45); c.drawRoundRect(-5, 51, 106, 82, 12, 12, paint);
            paint.setTextSize(17f); paint.setFakeBoldText(true); paint.setColor(color);
            c.drawText("SCROLL", 10, 73, paint);
        }
        c.restore();
    }
}