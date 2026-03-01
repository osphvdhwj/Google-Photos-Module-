package com.photos.gridmod

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "PhotosGridMod"
        private const val TARGET_PACKAGE = "com.google.android.apps.photos"
    }

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) return

        logDebug("Google Photos loaded, attempting to apply hooks...")

        try {
            val gridLayoutManagerClass = XposedHelpers.findClass(
                "androidx.recyclerview.widget.GridLayoutManager",
                lpparam.classLoader
            )

            XposedHelpers.findAndHookMethod(
                gridLayoutManagerClass,
                "setSpanCount",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val originalSpanCount = param.args[0] as Int

                        // Default zoomed out view is typically 4. Sometimes 5 on different DPIs.
                        if (originalSpanCount == 4 || originalSpanCount == 5) {
                            param.args[0] = 6
                            logDebug("Intercepted setSpanCount($originalSpanCount). Forcing to 6.")
                        } else {
                            // Leave smaller grid states (1, 2, 3) alone to not break pinch-to-zoom
                            // Optionally log but comment out to avoid spam
                            // logDebug("Ignoring setSpanCount($originalSpanCount).")
                        }
                    }
                }
            )

            logDebug("Hooks applied successfully for GridLayoutManager.setSpanCount")
        } catch (e: Throwable) {
            logError("Failed to hook GridLayoutManager", e)
        }
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
        XposedBridge.log("$TAG: $message")
    }

    private fun logError(message: String, t: Throwable) {
        Log.e(TAG, message, t)
        XposedBridge.log("$TAG: $message")
        XposedBridge.log(t)
    }
}
