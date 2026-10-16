# TV Remote Cursor 🖱️📺

[فارسی](#راهنمای-فارسی) | [English](#english)

A privacy-first, open-source **remote-controlled mouse for Android TV 9 (API 28) and newer, Google TV, and compatible Fire OS**. No root, no Wi-Fi, no account, no ad SDK, no network permission.

## راهنمای فارسی

این اپ برای **تلویزیون‌های Android TV نسخه ۹ به بالا / Google TV** ساخته شده است، مستقل از برند سازنده: TCL، Sony، Xiaomi، Philips، Hisense و غیره، *در صورتی که سیستم‌عامل و دسترس‌پذیری دستگاه سازگار باشند*. روی **Samsung Tizen / LG webOS / Roku TV** قابل نصب نیست.

### سازگاری نسخه‌ها

- **Android TV 9 / 10 / 11 / 12 / 13 و نسخه‌های جدیدتر:** نصب برنامه، موس جهت‌دار، کلیک و اسکرول با Accessibility (مشروط به پشتیبانی firmware).
- **Android TV 14 به بالا:** علاوه بر موارد بالا، آزمایش دریافت MotionEvent از ریموت‌های سازگار دارای ورودی HID Mouse در حالت Air Mouse اختیاری.
- **Android TV 9 تا 13:** گزینه Air Mouse در اپ غیرفعال است؛ بعضی ریموت‌های موس‌دار ممکن است نشانگر **بومی تلویزیون** را مستقل از اپ کنترل کنند.
- محدودیت مربوط به ریموت/سازنده/مجوزهاست، نه صرفاً نسخه اندروید. سازگاری همه مدل‌ها یا نسخه‌های آینده تضمین‌شده نیست.

### امکانات

- کنترل نشانگر با چهار جهت کنترل تلویزیون
- دکمه OK برای کلیک واقعی روی اجزای قابل کلیک (یا شبیه‌سازی لمس به عنوان راهکار جایگزین)
- کلیک، اسکرول و نشانگر شناور سراسری در اپ‌های دیگر (با محدودیت‌های احتمالی سازنده)
- مخفی‌شدن نشانگر بعد از ۲۰ ثانیه، با تنظیم ۱۰/۲۰/۳۰ ثانیه؛ بازگشت با فشردن جهت‌ها
- افزایش تدریجی سرعت هنگام نگه‌داشتن جهت و پنج سرعت قابل تنظیم
- **حالت اختیاری Air Mouse** برای ورودی‌های حرکتی HID استاندارد روی Android 14+؛ با کلید مستقل فعال/غیرفعال و حساسیت پنج‌مرحله‌ای
- تشخیص حضور ورودی Mouse و نمایش وضعیت «رویداد حرکتی دریافت شد» پس از دریافت واقعی داده؛ شناسایی ورودی Mouse به‌تنهایی اثبات وجود ژیروسکوپ نیست
- سه اندازه نشانگر
- خاموش‌کردن موس با Back؛ فعال‌سازی با نگه‌داشتن OK یا از داخل اپ
- بدون مجوز شبکه، روت، اکانت، تبلیغات، جمع‌آوری یا ارسال داده

### نصب

۱. APK خروجی `Actions` را روی تلویزیون Android TV/Google TV نصب کنید (یا سورس را در Android Studio باز کنید).

۲. اپ را باز کنید و با زدن «بازکردن تنظیمات دسترس‌پذیری»، سرویس **TV Remote Cursor** را به‌صورت دستی فعال کنید. این اجازه را باید خود کاربر بدهد.

۳. به اپ برگردید و «روشن‌کردن موس» را بزنید؛ بعد وارد برنامه‌های دیگر شوید.

۴. هنگام خاموش بودن موس می‌توانید OK را حدود ۰٫۸۵ ثانیه نگه دارید تا موس روشن شود. **روی بعضی تلویزیون‌ها نگه‌داشتن OK ممکن است انتخاب عادی قبلی را هم اجرا کند.** در این موارد اپ را باز کنید و موس را از آنجا روشن کنید.

۵. در حالت روشن، نگه‌داشتن OK حدود ۰٫۸۵ ثانیه **حالت اسکرول** را فعال/غیرفعال می‌کند. در حالت اسکرول، بالا/پایین صفحه را پیمایش می‌کند؛ OK کوتاه حالت اسکرول را می‌بندد. Back موس را خاموش می‌کند.

۶. اگر نشانگر دیده نمی‌شود، یک جهت را فشار دهید. سرویس پس از روشن‌شدن دستگاه معمولاً توسط اندروید مدیریت می‌شود؛ اما بسته به firmware ممکن است مجبور شوید دوباره فعالش کنید.

### آیا با تکان دادن کنترل معمولی موس حرکت می‌کند؟

**خیر، مگر ریموت سخت‌افزار حرکتی سازگار داشته باشد و داده را به اندروید بدهد.** ریموت IR/Bluetooth که تنها کلیدهای D-pad می‌فرستد، دادهٔ ژیروسکوپ در اختیار اپ قرار نمی‌دهد. Android 14 (API 34)+ می‌تواند برخی `MotionEvent`های HID mouse / relative mouse را با Accessibility Service دریافت کند. گزینه Air Mouse پیش‌فرض **خاموش** است و در Android پایین‌تر قابل فعال‌سازی نیست. بعضی ریموت‌های ژیروسکوپی از پروتکل اختصاصی استفاده می‌کنند و با این روش هم کار نمی‌کنند. در حالت خاموش سرویس هیچ رویداد Mouse را رهگیری نمی‌کند؛ اما این اپ کنترلی بر نشانگر موس بومی خود تلویزیون ندارد.

### محدودیت‌های شناخته‌شده

- این اپ **موس سخت‌افزاری واقعی در سطح سیستم ایجاد نمی‌کند**: روی Android از `AccessibilityService`، رویدادهای کلیک accessibility و ژست لمس استفاده می‌کند. بعضی بازی‌ها، DRMها، WebViewهای خاص یا صفحات سیستمی ممکن است کلیک را نپذیرند.
- برخی firmwareها (از جمله برخی مدل‌های TCL / Fire TV) کلیدها را قبل از Accessibility می‌گیرند یا به سرویس اجازه فیلتر کلید نمی‌دهند. بدون تست روی دستگاه نمی‌توان سازگاری همه مدل‌ها را تضمین کرد.
- اگر سرویس Accessibility دیگری قبلاً مسئول فیلتر کلیدها باشد، ممکن است موس نتواند کلیدها را دریافت کند.
- **Back در حالت موس صرفاً موس را خاموش می‌کند**؛ برای عملکرد عادی Back یک بار دیگر بزنید.
- فعال شدن قابلیت Accessibility به رضایت آگاهانه و دستی کاربر نیاز دارد. اپ هیچ کاری برای دورزدن مجوزها انجام نمی‌دهد.

### حریم خصوصی

سرویس صرفاً رویدادهای DPAD/OK و مختصات/قابلیت کلیک اجزای UI را بررسی می‌کند. محتوای متنی صفحه ذخیره یا ارسال نمی‌شود؛ اپ مجوز اینترنت ندارد. دسترسی Accessibility به طور بالقوه قدرتمند است؛ سورس آن در `MouseAccessibilityService.java` قابل بررسی است.

### ساخت APK

- Android Studio: پروژه را Open کنید؛ Build > Build APK(s).
- Gradle CLI: JDK 17 + Android SDK API 35 + Gradle 8.13، سپس `gradle assembleDebug`.
- GitHub: همه فایل‌های پروژه را در ریشه مخزن خود قرار دهید. در تب **Actions → Android APK → Run workflow** بیلد را اجرا و `TV-Remote-Cursor-v1-debug` را از Artifacts دانلود کنید.
- نسخه Debug برای تست است، نه انتشار رسمی مارکت. برای انتشار نهایی نسخه Release را با کلید شخصی خودتان امضا کنید؛ هیچ کلید امضایی در مخزن قرار ندهید.

## English

Android project in **Java**, minSdk 28 / targetSdk 35, no third-party runtime libraries. Accessibility service requests `canRequestFilterKeyEvents`, `canRetrieveWindowContent`, and `canPerformGestures`, and displays a non-touchable accessibility overlay. Keys are not recorded or uploaded. MIT licensed.

Air-mouse compatibility: API 34+ only, compatible HID mouse-motion input only. Standard remote gyroscope data is not accessible unless forwarded as normal Android input events. Native system mouse pointer cannot always be hidden/suppressed on old OEM firmware.

### Control reference

| Remote key | Action when enabled |
|---|---|
| D-pad | Move cursor; hold to accelerate |
| OK short | Click / exit scroll mode |
| OK hold | Toggle scroll mode; when disabled, turn mouse on |
| Up/down in scroll mode | Scroll |
| Back | Turn mouse off |

**Not supported:** non-Android TV systems (Tizen, webOS, Roku). On some OEM versions, accessibility key interception can be blocked. Real device testing remains required.