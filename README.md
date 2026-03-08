# PhotosGridMod (DEAD PROJECT / NOT WORKING) ⚠️

**This project is officially dead and no longer maintained.**

## Why is it dead?

This LSPosed module was originally created to force the Google Photos app (specifically v7.64.x on Android 15) to display a denser, 6-column photo grid instead of the default 4-column maximum zoomed-out view, while preserving native pinch-to-zoom gestures.

Despite numerous deep-dive technical attempts, Google Photos uses a highly custom, heavily obfuscated, and mathematically rigid rendering engine that makes hooking the UI nearly impossible without breaking the app or causing visual glitches.

### The Failed Approaches (A Technical Post-Mortem)

1.  **Direct Span Count Hooking (`GridLayoutManager.setSpanCount`)**
    *   **The Theory:** Hook the standard Android `GridLayoutManager` and intercept the method that sets the number of columns (`setSpanCount`), forcing it from 4 to 6.
    *   **The Reality:** Google Photos doesn't use the modern `androidx.recyclerview.widget.GridLayoutManager`. They use a legacy support library (`android.support.v7.widget.GridLayoutManager`), and the methods are heavily obfuscated by R8/ProGuard (e.g., `setSpanCount` is renamed to `q`). Even after successfully finding and hooking the obfuscated `q` method, the grid did not visually change.
    *   **Why it failed:** "The Physical Width Problem." The Google Photos adapter mathematically hardcodes the physical pixel width of the thumbnails (e.g., `screenWidth / 4`) *before* the `GridLayoutManager` organizes them. Even if we tell the manager to make 6 columns, the photos are physically too wide to fit, so the grid wraps them to the next line anyway, resulting in zero visual change.

2.  **The "Least Common Multiple" Spy Net**
    *   **The Theory:** We suspected Google Photos was using a Least Common Multiple grid strategy (e.g., using a base span of 12, where a span size of 3 equals 4 columns). We built a "Spy Net" module to hook `RecyclerView.setLayoutManager` to log every single span count request during pinch-to-zoom.
    *   **The Reality:** While we successfully intercepted the exact `LayoutManager` classes, attempting to mathematically override the span sizes dynamically during the pinch-to-zoom gesture proved to be a fragile and unmaintainable nightmare due to constant obfuscation changes.

3.  **App-Specific DPI Spoofing (The Final Attempt)**
    *   **The Theory:** If we can't change the grid math, we change the screen. We attempted to trick Google Photos into thinking the phone was running at a dense 420 DPI (which natively forces a 5/6 column grid), while simultaneously boosting the `fontScale` by 1.18x so the text didn't shrink to microscopic levels.
    *   **The Reality:** We implemented a brutal two-pronged attack on Android 15. We hooked `Activity.attachBaseContext` to inject a fake `Configuration` via `applyOverrideConfiguration(config)`, and we intercepted `Activity.onCreate` to hard-overwrite `densityDpi`, `density`, and `scaledDensity` inside both the Activity's and the Application Context's `displayMetrics`.
    *   **Why it failed:** Google's custom rendering engine still bypasses or misinterprets these deep system-level metric overrides, leading to inconsistent UI scaling, broken margins, and unreadable text elements that don't respect the `fontScale` multiplier.

### Conclusion

Google Photos is not built using standard Android UI paradigms that are friendly to Xposed/LSPosed modification. The amount of effort required to reverse-engineer their obfuscated math formulas and physical layout constraints—which change with every minor app update—is not worth the maintenance burden.

**Do not attempt to compile or install this module.** It does not work.
