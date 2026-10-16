package io.github.meysam82003.tvcursor;

import android.app.Activity;
import android.content.Intent;
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

/** Neon dashboard optimized for Android TV / Google TV remotes. */
public final class MainActivity extends Activity {
    private static final int BG_TOP = 0xFF071121;
    private static final int BG_BOTTOM = 0xFF0A1C36;
    private static final int PANEL = 0xCC102642;
    private static final int PANEL_2 = 0xE0142F52;
    private static final int PANEL_3 = 0xD719355B;
    private static final int BORDER = 0xFF2D5A87;
    private static final int CYAN = 0xFF4EEFFF;
    private static final int CYAN_2 = 0xFF25C2FF;
    private static final int TEXT = 0xFFF6FAFF;
    private static final int MUTED = 0xFFA8BED7;
    private static final int GOLD = 0xFFFFB545;
    private static final int GOLD_DARK = 0x66251809;
    private static final int SUCCESS = 0xFF45EACB;
    private static final int DANGER = 0xFFFF8D8D;

    private Prefs prefs;
    private TextView statusTitle, statusDetail, warningTitle, warningDetail, airStatus, versionView;
    private Button primaryToggle, quickAccess,
            speed, size, color, timeout, snap, highlight, edge, menu, restore, air, airSpeed;
    private View warningCard;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override public void run() {
            refresh();
            handler.postDelayed(this, 900);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        getWindow().setStatusBarColor(BG_TOP);
        getWindow().setNavigationBarColor(BG_TOP);
        buildUi();
        refresh();
    }

    @Override protected void onResume() {
        super.onResume();
        MouseAccessibilityService s = MouseAccessibilityService.running;
        if (s != null) s.dashboard(true);
        handler.removeCallbacks(refreshTask);
        handler.post(refreshTask);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(refreshTask);
        MouseAccessibilityService s = MouseAccessibilityService.running;
        if (s != null) s.dashboard(false);
        super.onPause();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackground(gradient(BG_TOP, BG_BOTTOM, true, 0, 0, 0));

        LinearLayout page = column();
        page.setPadding(dp(24), dp(24), dp(24), dp(30));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        addHero(page);
        space(page, 18);
        addControlCard(page);
        space(page, 16);
        addSettingsCard(page);
        space(page, 16);
        addGuideCard(page);
        space(page, 16);

        TextView footer = text("پشتیبانی نرم‌افزاری برای Android TV 9+، Google TV و Fire OS سازگار. عملکرد نهایی به فریمور تلویزیون و اپ مقصد بستگی دارد.", 13, MUTED, false);
        footer.setLineSpacing(0f, 1.18f);
        page.addView(footer);

        setContentView(scroll);
        if (primaryToggle != null) primaryToggle.requestFocus();
    }

    private void addHero(LinearLayout parent) {
        boolean wide = getResources().getConfiguration().screenWidthDp >= 680;
        LinearLayout hero = glassCard();
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));

        LinearLayout content = wide ? row() : column();
        content.setGravity(Gravity.CENTER_VERTICAL);
        hero.addView(content, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout titleCol = column();
        titleCol.setGravity(Gravity.CENTER_VERTICAL);
        if (wide) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
            lp.setMarginStart(dp(18));
            content.addView(titleCol, lp);
        } else {
            content.addView(titleCol, new LinearLayout.LayoutParams(-1, -2));
        }

        TextView title = text("TV Remote Cursor", 28, TEXT, true);
        title.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        title.setLineSpacing(0f, 0.96f);
        titleCol.addView(title);

        TextView subtitle = text("کنترل موس برای اندروید تی‌وی", 16, MUTED, false);
        subtitle.setPadding(0, dp(6), 0, 0);
        titleCol.addView(subtitle);

        space(titleCol, 14);
        LinearLayout chips = row();
        chips.setGravity(Gravity.CENTER_VERTICAL);
        chips.addView(chip("ANDROID TV  •  GOOGLE TV", CYAN, 0x2228DFFF));
        versionView = text("نسخه ۱.۴.۰", 14, TEXT, false);
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(-2, -2);
        vlp.setMarginStart(dp(14));
        versionView.setAlpha(0.96f);
        chips.addView(versionView, vlp);
        titleCol.addView(chips);

        if (!wide) space(content, 18);

        LinearLayout iconWrap = column();
        iconWrap.setGravity(Gravity.CENTER);
        iconWrap.setBackground(glowCard(0xFF143462, CYAN_2, 2, 24));
        iconWrap.setPadding(dp(14), dp(14), dp(14), dp(14));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.neon_cursor_icon);
        iconWrap.addView(icon, new LinearLayout.LayoutParams(dp(wide ? 124 : 118), dp(wide ? 124 : 118)));
        if (wide) content.addView(iconWrap, new LinearLayout.LayoutParams(-2, -2));
        else {
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(-2, -2);
            ilp.gravity = Gravity.END;
            content.addView(iconWrap, ilp);
        }

        parent.addView(hero, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addControlCard(LinearLayout parent) {
        LinearLayout card = glassCard();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        parent.addView(card, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout top = row();
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout iconBox = column();
        iconBox.setGravity(Gravity.CENTER);
        iconBox.setPadding(dp(18), dp(18), dp(18), dp(18));
        iconBox.setBackground(glowCard(0xFF12345A, CYAN, 1, 22));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.neon_cursor_icon);
        iconBox.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));
        top.addView(iconBox, new LinearLayout.LayoutParams(dp(96), dp(96)));

        LinearLayout right = column();
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(0, -2, 1f);
        rlp.setMarginStart(dp(16));
        top.addView(right, rlp);
        right.addView(text("کنترل موس", 24, TEXT, true));
        TextView desc = text("با یک دکمه موس را فعال کنید و در سایر اپلیکیشن‌ها از آن استفاده کنید.", 16, MUTED, false);
        desc.setLineSpacing(0f, 1.18f);
        desc.setPadding(0, dp(6), 0, 0);
        right.addView(desc);

        space(card, 16);
        statusTitle = text("", 21, CYAN, true);
        statusDetail = text("", 15, MUTED, false);
        statusDetail.setLineSpacing(0f, 1.2f);
        card.addView(statusTitle);
        LinearLayout.LayoutParams sdlp = new LinearLayout.LayoutParams(-1, -2);
        sdlp.topMargin = dp(4);
        card.addView(statusDetail, sdlp);

        space(card, 16);
        warningCard = warningPanel(card);

        primaryToggle = actionButton(card, "", true, this::handlePrimaryToggle);
        quickAccess = actionButton(card, "", false, this::openAccessibility);

    }

    private View warningPanel(LinearLayout parent) {
        LinearLayout warn = column();
        warn.setPadding(dp(16), dp(16), dp(16), dp(16));
        warn.setBackground(glowCard(GOLD_DARK, GOLD, 1, 20));
        parent.addView(warn, new LinearLayout.LayoutParams(-1, -2));

        warningTitle = text("", 18, GOLD, true);
        warningDetail = text("", 14, 0xFFEBDCC7, false);
        warningDetail.setLineSpacing(0f, 1.18f);
        warn.addView(warningTitle);
        LinearLayout.LayoutParams wdlp = new LinearLayout.LayoutParams(-1, -2);
        wdlp.topMargin = dp(8);
        warn.addView(warningDetail, wdlp);
        return warn;
    }

    private void addSettingsCard(LinearLayout parent) {
        LinearLayout card = glassCard();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        parent.addView(card, new LinearLayout.LayoutParams(-1, -2));

        card.addView(text("تنظیمات و شخصی‌سازی", 23, TEXT, true));
        TextView sub = text("دکمه‌ها با ریموت تلویزیون کاملاً قابل کنترل‌اند.", 14, MUTED, false);
        sub.setPadding(0, dp(6), 0, 0);
        card.addView(sub);

        space(card, 16);
        TextView airTitle = text("موس حرکتی (Air Mouse)", 18, CYAN, true);
        card.addView(airTitle);
        airStatus = text("", 13, MUTED, false);
        airStatus.setLineSpacing(0f, 1.2f);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(-1, -2);
        alp.topMargin = dp(6);
        card.addView(airStatus, alp);
        air = settingButton(card, "", () -> {
            if (Build.VERSION.SDK_INT >= 34) {
                prefs.airMouse(!prefs.airMouse());
                changed();
            }
        });
        airSpeed = settingButton(card, "", () -> {
            prefs.airSensitivity((prefs.airSensitivity() + 1) % 5);
            changed();
        });

        TextView airNote = text("اگر ریموت شما داده حرکتی واقعی ارسال نکند، حالت Air Mouse با نرم‌افزار به‌تنهایی ممکن نیست.", 13, MUTED, false);
        airNote.setLineSpacing(0f, 1.2f);
        airNote.setPadding(0, 0, 0, dp(4));
        card.addView(airNote);

        speed = settingButton(card, "", () -> { prefs.speed((prefs.speed() + 1) % 5); changed(); });
        size = settingButton(card, "", () -> { prefs.size((prefs.size() + 1) % 3); changed(); });
        color = settingButton(card, "", () -> { prefs.color((prefs.color() + 1) % 3); changed(); });
        timeout = settingButton(card, "", () -> {
            int[] ts = {0, 5, 10, 20, 30, 60};
            int v = prefs.timeout(), i = 0;
            for (int j = 0; j < ts.length; j++) if (ts[j] == v) i = j;
            prefs.timeout(ts[(i + 1) % ts.length]);
            changed();
        });
        snap = settingButton(card, "", () -> { prefs.snap(!prefs.snap()); changed(); });
        highlight = settingButton(card, "", () -> { prefs.highlight(!prefs.highlight()); changed(); });
        edge = settingButton(card, "", () -> { prefs.edgeScroll(!prefs.edgeScroll()); changed(); });
        menu = settingButton(card, "", () -> { prefs.menuToggle(!prefs.menuToggle()); changed(); });
        restore = settingButton(card, "", () -> { prefs.autoRestore(!prefs.autoRestore()); changed(); });
    }

    private void addGuideCard(LinearLayout parent) {
        LinearLayout card = glassCard();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        parent.addView(card, new LinearLayout.LayoutParams(-1, -2));
        card.addView(text("راهنمای کلیدهای ریموت", 21, TEXT, true));
        TextView guide = text(
                "•  ↑ ↓ ← →  حرکت موس و نگه‌داشتن برای سرعت بیشتر\n" +
                "•  OK  کلیک اصلی\n" +
                "•  نگه‌داشتن OK  ورود به حالت اسکرول\n" +
                "•  MENU  روشن/خاموش کردن موس (اگر فریمور اجازه دهد)\n" +
                "•  Back کوتاه  بازگشت / Back طولانی  خاموش‌کردن موس\n" +
                "•  مخفی‌شدن خودکار نشانگر و بازگشت با کلیدهای جهت‌دار",
                15, MUTED, false);
        guide.setPadding(0, dp(10), 0, 0);
        guide.setLineSpacing(0f, 1.22f);
        card.addView(guide);
    }

    private void handlePrimaryToggle() {
        MouseAccessibilityService s = MouseAccessibilityService.running;
        if (s == null) openAccessibility();
        else {
            s.toggle();
            refresh();
        }
    }

    private void changed() {
        MouseAccessibilityService s = MouseAccessibilityService.running;
        if (s != null) s.updateSettings();
        refresh();
    }

    private void openAccessibility() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private void refresh() {
        MouseAccessibilityService s = MouseAccessibilityService.running;
        boolean available = s != null;
        boolean active = available && s.isActive();

        statusTitle.setText(active ? "●  موس روشن است" : available ? "●  سرویس آماده است" : "●  نیازمند فعال‌سازی دسترسی");
        statusTitle.setTextColor(active ? SUCCESS : available ? GOLD : DANGER);
        statusDetail.setText(active
                ? "اکنون می‌توانید از اپ خارج شوید و در برنامه‌های دیگر با موس کار کنید."
                : available
                ? "سرویس دسترسی فعال شده است. با دکمه اصلی موس را روشن کنید."
                : "ابتدا سرویس Accessibility را در تنظیمات تلویزیون روشن کنید تا کنترل موس فعال شود.");

        primaryToggle.setText(active ? "خاموش‌کردن موس" : available ? "روشن‌کردن موس" : "فعال‌سازی دسترسی");
        quickAccess.setText("تنظیم دسترسی موردنیاز");

        warningCard.setVisibility(active ? View.GONE : View.VISIBLE);
        warningTitle.setText(available ? "سرویس فعال است، اما موس هنوز روشن نشده" : "نیازمند فعال‌سازی دسترسی");
        warningDetail.setText(available
                ? "برای استفاده واقعی از موس، دکمه «روشن‌کردن موس» را بزنید."
                : "ابتدا سرویس TV Remote Cursor را از تنظیمات Accessibility تلویزیون روشن کنید.");

        speed.setText("سرعت حرکت   •   " + new String[]{"خیلی کند", "کند", "معمولی", "سریع", "خیلی سریع"}[prefs.speed()]);
        size.setText("اندازه نشانگر   •   " + new String[]{"کوچک", "متوسط", "بزرگ"}[prefs.size()]);
        color.setText("رنگ موس   •   " + new String[]{"فیروزه‌ای", "بنفش", "طلایی"}[prefs.color()]);
        timeout.setText("مخفی‌شدن نشانگر   •   " + (prefs.timeout() == 0 ? "هرگز" : prefs.timeout() + " ثانیه"));
        snap.setText("چسبیدن به نزدیک‌ترین دکمه   •   " + yes(prefs.snap()));
        highlight.setText("کادر دور هدف انتخابی   •   " + yes(prefs.highlight()));
        edge.setText("اسکرول در لبه صفحه   •   " + yes(prefs.edgeScroll()));
        menu.setText("کلید MENU برای روشن/خاموش   •   " + yes(prefs.menuToggle()));
        restore.setText("شروع خودکار پس از سرویس   •   " + yes(prefs.autoRestore()));

        boolean apiOk = Build.VERSION.SDK_INT >= 34;
        int mouseDevices = 0;
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice d = InputDevice.getDevice(id);
            if (d != null && ((d.getSources() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE ||
                    (Build.VERSION.SDK_INT >= 34 && (d.getSources() & InputDevice.SOURCE_MOUSE_RELATIVE) == InputDevice.SOURCE_MOUSE_RELATIVE))) {
                mouseDevices++;
            }
        }
        boolean motionSeen = s != null && s.hasObservedMotion();
        airStatus.setText(!apiOk
                ? "Android 14+ لازم است. در Android 9 تا 13 موس جهت‌دار همچنان کامل کار می‌کند."
                : mouseDevices == 0
                ? "فعلاً ریموت یا دستگاه سازگار HID Mouse دیده نشد."
                : motionSeen
                ? "ورودی حرکتی دریافت شده و قابل استفاده است."
                : "سخت‌افزار شناسایی شده؛ پس از روشن‌کردن، ریموت را حرکت دهید تا تست شود.");
        air.setEnabled(apiOk);
        airSpeed.setEnabled(apiOk);
        air.setText("موس حرکتی   •   " + (apiOk ? yes(prefs.airMouse()) : "پشتیبانی‌نشده در این نسخه اندروید"));
        airSpeed.setText("حساسیت Air Mouse   •   " + new String[]{"خیلی کم", "کم", "نرمال", "زیاد", "خیلی زیاد"}[prefs.airSensitivity()]);
    }

    private LinearLayout glassCard() {
        LinearLayout box = column();
        box.setBackground(glowCard(PANEL, BORDER, 1, 28));
        return box;
    }

    private Button actionButton(LinearLayout parent, String label, boolean primary, Runnable action) {
        Button b = baseButton(label, primary ? 18 : 16, true);
        b.setPadding(dp(20), dp(20), dp(20), dp(20));
        b.setMinHeight(dp(66));
        b.setBackground(primary
                ? gradient(0xFF4EEFFF, 0xFF20BFFF, false, CYAN, 2, 22)
                : glowCard(PANEL_2, CYAN_2, 1, 20));
        b.setTextColor(primary ? 0xFF071121 : TEXT);
        b.setOnClickListener(v -> action.run());
        focusFx(b, primary ? 0x884EEFFF : 0x552BC2FF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(12);
        parent.addView(b, lp);
        return b;
    }

    private Button settingButton(LinearLayout parent, String label, Runnable action) {
        Button b = baseButton(label, 15, false);
        b.setPadding(dp(18), dp(16), dp(18), dp(16));
        b.setBackground(glowCard(PANEL_3, BORDER, 1, 18));
        b.setTextColor(TEXT);
        b.setOnClickListener(v -> action.run());
        focusFx(b, 0x554EEFFF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(10);
        parent.addView(b, lp);
        return b;
    }

    private Button baseButton(String label, int sp, boolean bold) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(sp);
        b.setTextColor(TEXT);
        b.setGravity(Gravity.CENTER);
        if (bold) b.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        return b;
    }

    private void focusFx(View v, int glowColor) {
        v.setOnFocusChangeListener((view, hasFocus) -> {
            view.animate().scaleX(hasFocus ? 1.03f : 1f).scaleY(hasFocus ? 1.03f : 1f).setDuration(120).start();
            view.setAlpha(hasFocus ? 1f : 0.96f);
            if (view.getBackground() instanceof GradientDrawable) {
                GradientDrawable bg = (GradientDrawable) view.getBackground();
                bg.setStroke(dp(hasFocus ? 2 : 1), hasFocus ? glowColor : BORDER);
            }
        });
    }

    private TextView chip(String text, int stroke, int fill) {
        TextView tv = this.text(text, 13, TEXT, true);
        tv.setPadding(dp(16), dp(10), dp(16), dp(10));
        tv.setBackground(gradient(fill, fill, false, stroke, 1, 18));
        return tv;
    }

    private GradientDrawable glowCard(int fill, int stroke, int strokeDp, int radiusDp) {
        return gradient(fill, fill, false, stroke, strokeDp, radiusDp);
    }

    private GradientDrawable gradient(int start, int end, boolean vertical, int stroke, int strokeDp, int radiusDp) {
        GradientDrawable d = new GradientDrawable(
                vertical ? GradientDrawable.Orientation.TOP_BOTTOM : GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{start, end});
        d.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) d.setStroke(dp(strokeDp), stroke);
        return d;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        return tv;
    }

    private String yes(boolean v) {
        return v ? "روشن" : "خاموش";
    }

    private void space(LinearLayout parent, int dp) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(-1, dp(dp)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}