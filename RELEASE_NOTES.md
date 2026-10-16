# TV Remote Cursor v1.4.0 — Neon UI 🖱️📺

- Completely redesigned RTL-friendly dashboard: deep navy, neon cyan, polished cards and large remote-focusable controls.
- Replaced the broken, narrow English header and refreshed the application icon.
- Cleaner Accessibility permission status and one-tap activation flow.
- Adjustable pointer speed, size, color, idle timeout (default 20 seconds), target snap, highlight and optional edge scrolling.
- Optional Android 14+ HID Air Mouse mode (requires a compatible remote/device).
- Android TV 9 / API 28 minimum, Android SDK 35 target; no network permissions, root, ads or trackers.

## Installation

Install `TV-Remote-Cursor-v1.4.0-Android9plus.apk` on a compatible Android TV 9+ device. Enable **TV Remote Cursor** manually under **Settings → Accessibility**, return to the app and press **Turn on mouse**. D-pad moves, OK clicks.

### Release-signing notice

Unless the repository owner configures the four `TV_CURSOR_*` GitHub Actions signing secrets, the automated APK is a **preview release signed with the Android debug certificate**. It is installable, but is **not suitable for store publication**. Because GitHub runners may generate different debug keys, users may need to uninstall v1.3.0 before installing v1.4.0; this clears app preferences. A stable production release requires the owner's private release keystore, which must never be committed to GitHub.

### Compatibility limitations

Accessibility key interception and gesture dispatch depend on TV manufacturer firmware and destination app. Air Mouse requires a compatible HID mouse-motion input source; Android cannot infer gyro movement from a D-pad-only remote. Real-device validation is still needed.