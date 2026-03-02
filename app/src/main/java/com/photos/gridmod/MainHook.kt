package com.photos.gridmod

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.google.android.apps.photos") return

        try {
            XposedHelpers.findAndHookMethod(
                "android.support.v7.widget.GridLayoutManager",
                lpparam.classLoader,
                "q", // The obfuscated name for setSpanCount
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val originalSpan = param.args[0] as Int

                        // Hijack the larger grid states and force them to 6
                        if (originalSpan == 3 || originalSpan == 4) {
                            param.args[0] = 6

                            // Log the successful hijack so we can see it in Termux
                            android.util.Log.i("PhotosGridMod", "BOOM! Intercepted span $originalSpan, forced to 6 columns!")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            android.util.Log.e("PhotosGridMod", "Failed to hook GridLayoutManager: ${e.message}")
        }
    }
}
