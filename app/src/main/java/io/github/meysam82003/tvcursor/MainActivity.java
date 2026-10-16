package io.github.meysam82003.tvcursor;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Overscan-safe, focus-friendly RTL dashboard for real television remotes. */
public final class MainActivity extends Activity {
    private static final int BG = 0xFF091323, PANEL = 0xFF13243D, CYAN = 0xFF56EDE2;
    private static final int MUTED = 0xFFADC0D4, BORDER = 0xFF2B4665;
    private Prefs prefs;
    private TextView status, statusDetail;
    private Button toggle, speed, size, color, timeout, snap, highlight, edge, menu, restore, air, airSpeed;
    private TextView airStatus;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override public void run() { refresh(); handler.postDelayed(this, 900); }
    };
    @Override public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        prefs = new Prefs(this);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        buildUi(); refresh();
    }
    private void buildUi() {
        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true); scroller.setBackgroundColor(BG);
        scroller.setClipToPadding(false);
        LinearLayout page = column();
        page.setPadding(dp(38), dp(23), dp(38), dp(32));
        scroller.addView(page);
        LinearLayout hero = row(); hero.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(this); icon.setImageResource(R.drawable.app_icon);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(82), dp(82));
        ilp.leftMargin = dp(22); hero.addView(icon, ilp);
        LinearLayout heading = column();
        addText(heading, "TV REMOTE CURSOR", 30, Color.WHITE, true, 0);
        addText(heading, "موس هوشمند تلویزیون  |  نسخه ۱.۳", 16, CYAN, false, 0);
        hero.addView(heading, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView edition = text("ANDROID TV  •  GOOGLE TV", 12, CYAN, true);
        edition.setPadding(dp(16), dp(9), dp(16), dp(9)); edition.setBackground(shape(0xFF183950, 14, CYAN, 1));
        hero.addView(edition);
        page.addView(hero); space(page, 24);
        int sw = getResources().getConfiguration().screenWidthDp;
        boolean landscape = sw >= 680;
        LinearLayout split = landscape ? row() : column();
        split.setGravity(Gravity.TOP); page.addView(split);
        LinearLayout main = card();
        addText(main, "کنترل موس", 25, Color.WHITE, true, 5);
        addText(main, "با یک دکمه موس را فعال کن و در سایر اپ‌ها از آن استفاده کن.", 16, MUTED, false, 18);
        status = addText(main, "", 20, CYAN, true, 6);
        statusDetail = addText(main, "", 14, MUTED, false, 15);
        toggle = button(main, "فعال‌کردن موس", true, () -> {
            MouseAccessibilityService s = MouseAccessibilityService.running;
            if (s == null) openAccessibility();
            else { s.toggle(); refresh(); }
        });
        button(main, "تنظیم دسترسی موردنیاز  ↗", false, this::openAccessibility);
        space(main, 12);
        addText(main, "راهنمای کلیدهای ریموت", 20, CYAN, true, 9);
        addText(main, "↑ ↓ ← →   حرکت موس / نگه‌داشتن برای سرعت بیشتر\nOK   کلیک   •   نگه‌داشتن OK   حالت اسکرول\nMENU   روشن/خاموش‌کردن موس (در صورت پشتیبانی)\nBack کوتاه   برگشت عادی   •   Back طولانی   خاموش\nموس پس از زمان تعیین‌شده مخفی می‌شود؛ با جهت‌ها بازمی‌گردد.", 15, Color.WHITE, false, 13);
        addText(main, "🔒 بدون اینترنت، حساب کاربری، تبلیغات یا روت", 14, MUTED, false, 0);
        LinearLayout controls = card();
        addText(controls, "تنظیمات و شخصی‌سازی", 24, Color.WHITE, true, 5);
        addText(controls, "هر گزینه را با دکمه OK کنترل تلویزیون تغییر بده.", 15, MUTED, false, 14);
        addText(controls, "موس حرکتی (Air Mouse)", 21, CYAN, true, 7);
        airStatus = addText(controls, "", 13, MUTED, false, 10);
        air = button(controls, "", false, () -> {
            if (Build.VERSION.SDK_INT >= 34) { prefs.airMouse(!prefs.airMouse()); changed(); }
        });
        airSpeed = button(controls, "", false, () -> { prefs.airSensitivity((prefs.airSensitivity() + 1) % 5); changed(); });
        addText(controls,
                "موس چهارجهته از Android TV ۹ به بالا کار می‌کند. حالت Air Mouse داخلی فقط در Android ۱۴+ با ریموتی که رویداد HID Mouse ارسال کند قابل آزمایش است. ریموت فاقد حسگر حرکت با نرم‌افزار به ریموت حرکتی تبدیل نمی‌شود.",
                13, MUTED, false, 15);
        speed = button(controls, "", false, () -> { prefs.speed((prefs.speed() + 1) % 5); changed(); });
        size = button(controls, "", false, () -> { prefs.size((prefs.size() + 1) % 3); changed(); });
        color = button(controls, "", false, () -> { prefs.color((prefs.color() + 1) % 3); changed(); });
        timeout = button(controls, "", false, () -> {
            int[] ts = {0, 5, 10, 20, 30, 60}; int v = prefs.timeout(); int i = 0;
            for (int j = 0; j < ts.length; j++) if (ts[j] == v) i = j;
            prefs.timeout(ts[(i + 1) % ts.length]); changed();
        });
        snap = button(controls, "", false, () -> { prefs.snap(!prefs.snap()); changed(); });
        highlight = button(controls, "", false, () -> { prefs.highlight(!prefs.highlight()); changed(); });
        edge = button(controls, "", false, () -> { prefs.edgeScroll(!prefs.edgeScroll()); changed(); });
        menu = button(controls, "", false, () -> { prefs.menuToggle(!prefs.menuToggle()); changed(); });
        restore = button(controls, "", false, () -> { prefs.autoRestore(!prefs.autoRestore()); changed(); });
        if (landscape) {
            LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, -2, 0.93f);
            left.setMarginEnd(dp(18)); split.addView(main, left);
            split.addView(controls, new LinearLayout.LayoutParams(0, -2, 1.07f));
        } else {
            split.addView(main, new LinearLayout.LayoutParams(-1, -2));
            space(split, 18); split.addView(controls, new LinearLayout.LayoutParams(-1, -2));
        }
        space(page, 16);
        addText(page, "پشتیبانی نرم‌افزاری از Android TV ۹ و جدیدتر / Google TV و Fire OS پشتیبانی‌شده؛ نه Tizen / webOS / Roku. عملکرد به محدودیت‌های سازنده و برنامه مقصد بستگی دارد.", 13, MUTED, false, 0);
        setContentView(scroller);
        toggle.requestFocus();
    }
    private void openAccessibility() { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
    private void changed() {
        MouseAccessibilityService s = MouseAccessibilityService.running;
        if (s != null) s.updateSettings();
        refresh();
    }
    @Override protected void onResume() {
        super.onResume();
        MouseAccessibilityService s = MouseAccessibilityService.running;
        if (s != null) s.dashboard(true);
        handler.removeCallbacks(refreshTask); handler.post(refreshTask);
    }
    @Override protected void onPause() {
        handler.removeCallbacks(refreshTask);
        MouseAccessibilityService s = MouseAccessibilityService.running;
        if (s != null) s.dashboard(false);
        super.onPause();
    }
    private void refresh() {
        if (status == null) return;
        MouseAccessibilityService s = MouseAccessibilityService.running;
        boolean available = s != null, active = available && s.isActive();
        status.setText(active ? "●  موس روشن است" : available ? "●  موس آماده است" : "●  نیازمند فعال‌سازی دسترسی");
        status.setTextColor(active ? CYAN : available ? 0xFFFFD378 : 0xFFFFA5A5);
        statusDetail.setText(active ? "اکنون می‌توانید به برنامه‌های دیگر برگردید." :
                available ? "سرویس فعال است. برای شروع، دکمه زیر را بزنید." :
                "ابتدا سرویس Accessibility را از تنظیمات تلویزیون روشن کنید.");
        toggle.setText(active ? "■  غیرفعال‌کردن موس" : available ? "▶  فعال‌کردن موس" : "⚙  فعال‌سازی دسترسی");
        speed.setText("سرعت حرکت   ·   " + new String[]{"خیلی کند", "کند", "معمولی", "سریع", "خیلی سریع"}[prefs.speed()]);
        size.setText("اندازه نشانگر   ·   " + new String[]{"کوچک", "متوسط", "بزرگ"}[prefs.size()]);
        color.setText("رنگ موس   ·   " + new String[]{"فیروزه‌ای", "بنفش", "طلایی"}[prefs.color()]);
        timeout.setText("مخفی‌شدن نشانگر   ·   " + (prefs.timeout() == 0 ? "هرگز" : prefs.timeout() + " ثانیه"));
        snap.setText("چسبیدن به دکمه نزدیک   ·   " + yes(prefs.snap()));
        highlight.setText("کادر دور دکمه انتخابی   ·   " + yes(prefs.highlight()));
        edge.setText("اسکرول در لبه صفحه   ·   " + yes(prefs.edgeScroll()));
        menu.setText("کلید MENU برای روشن/خاموش   ·   " + yes(prefs.menuToggle()));
        restore.setText("شروع خودکار پس از سرویس   ·   " + yes(prefs.autoRestore()));
        boolean apiOk = Build.VERSION.SDK_INT >= 34;
        int pointers = 0;
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice d = InputDevice.getDevice(id);
            if (d != null && (d.supportsSource(InputDevice.SOURCE_MOUSE) ||
                    d.supportsSource(InputDevice.SOURCE_MOUSE_RELATIVE))) pointers++;
        }
        air.setText("حرکت با تکان دادن ریموت   ·   " + (apiOk ? yes(prefs.airMouse()) : "نیازمند Android 14+"));
        air.setEnabled(apiOk);
        airSpeed.setText("حساسیت موس حرکتی   ·   " + new String[]{"خیلی کم", "کم", "معمولی", "زیاد", "خیلی زیاد"}[prefs.airSensitivity()]);
        airSpeed.setEnabled(apiOk);
        airStatus.setText(!apiOk ? "نسخه اندروید تلویزیون: " + Build.VERSION.RELEASE + "؛ حالت دریافت رویداد حرکتی پشتیبانی نمی‌شود." :
                (s != null && s.hasObservedMotion() ? "✓ رویداد حرکت از یک ورودی سازگار دریافت شد." :
                pointers > 0 ? "ورودی اشاره‌گر متصل شناسایی شد؛ برای اطمینان از کارکرد، حرکتش را آزمایش کنید." :
                "فعلاً ورودی موس سازگار شناسایی نشده است؛ یک Air Mouse سازگار متصل کنید."));
    }
    private String yes(boolean yes) { return yes ? "روشن ✓" : "خاموش"; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); return l; }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout card() {
        LinearLayout c = column(); c.setBackground(shape(PANEL, 22, BORDER, 1));
        c.setPadding(dp(23), dp(24), dp(23), dp(24)); return c;
    }
    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(sp);
        t.setTextColor(color); t.setLineSpacing(dp(5), 1f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private TextView addText(LinearLayout parent, String value, int sp, int color, boolean bold, int bottom) {
        TextView t = text(value, sp, color, bold);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(bottom);
        parent.addView(t, p); return t;
    }
    private Button button(LinearLayout parent, String label, boolean primary, Runnable action) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(primary ? 20 : 15); b.setAllCaps(false);
        b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        b.setPadding(dp(19), 0, dp(19), 0);
        b.setTextColor(primary ? BG : Color.WHITE); b.setFocusable(true);
        b.setBackground(shape(primary ? CYAN : 0xFF1C334F, 12, primary ? CYAN : BORDER, 1));
        b.setOnClickListener(v -> action.run());
        b.setOnFocusChangeListener((v, hasFocus) -> {
            b.setBackground(shape(hasFocus ? (primary ? 0xFF9BFFF3 : 0xFF284967) : (primary ? CYAN : 0xFF1C334F),
                    12, hasFocus ? CYAN : (primary ? CYAN : BORDER), hasFocus ? 3 : 1));
            b.animate().scaleX(hasFocus ? 1.015f : 1f).scaleY(hasFocus ? 1.02f : 1f).setDuration(140).start();
        });
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(primary ? 66 : 53));
        p.bottomMargin = dp(primary ? 12 : 8); parent.addView(b, p);
        return b;
    }
    private GradientDrawable shape(int color, int radius, int stroke, int borderDp) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color);
        d.setCornerRadius(dp(radius)); d.setStroke(dp(borderDp), stroke); return d;
    }
    private void space(LinearLayout l, int height) { View v = new View(this); l.addView(v, new LinearLayout.LayoutParams(1, dp(height))); }
    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}