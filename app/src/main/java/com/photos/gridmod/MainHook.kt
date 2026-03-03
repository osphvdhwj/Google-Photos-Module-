package com.photos.gridmod

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.google.android.apps.photos") return

        val targetDpi = 420
        val targetFontScale = 1.18f
        val targetDensity = targetDpi / 160f

        try {
            // Step 1: The official Android method to inject a config early in the lifecycle
            XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "attachBaseContext",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as android.app.Activity
                        val config = Configuration()
                        config.densityDpi = targetDpi
                        config.fontScale = targetFontScale

                        // Merge our fake DPI/Font with the real system config
                        activity.applyOverrideConfiguration(config)
                    }
                }
            )

            // Step 2: Brute-force the raw physical metrics (Because Google Photos bypasses configs)
            XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                lpparam.classLoader,
                "onCreate",
                Bundle::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as android.app.Activity

                        // Hijack the Activity-level metrics
                        val metrics = activity.resources.displayMetrics
                        metrics.densityDpi = targetDpi
                        metrics.density = targetDensity
                        metrics.scaledDensity = targetDensity * targetFontScale

                        // Hijack the Application-level metrics (Crucial for Google Photos!)
                        val appMetrics = activity.applicationContext.resources.displayMetrics
                        appMetrics.densityDpi = targetDpi
                        appMetrics.density = targetDensity
                        appMetrics.scaledDensity = targetDensity * targetFontScale
                    }
                }
            )

            XposedBridge.log("PhotosGridMod: A15 DPI Spoofing injected into Activity & Application Contexts.")

        } catch (e: Throwable) {
            XposedBridge.log("PhotosGridMod Error: ${e.message}")
        }
    }
}
