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
            // Hooking the specific obfuscated method 'q' in the legacy support GridLayoutManager
            XposedHelpers.findAndHookMethod(
                "android.support.v7.widget.GridLayoutManager",
                lpparam.classLoader,
                "q", // The obfuscated name for setSpanCount
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val originalSpan = param.args[0] as Int

                        // Intercept the max zoomed-out state (4) and force to 6
                        if (originalSpan == 4 || originalSpan == 5) {
                            param.args[0] = 6
                            XposedBridge.log("PhotosGridMod: Intercepted method 'q' with span $originalSpan, forced to 6")
                            android.util.Log.i("PhotosGridMod", "Successfully forced 6-column grid!")
                        }
                    }
                }
            )

            logDebug("Hooks applied successfully for GridLayoutManager.q")
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
