package com.photos.gridmod

import android.content.Context
import android.content.res.Configuration
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
                "android.app.Activity",
                lpparam.classLoader,
                "attachBaseContext",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val context = param.args[0] as Context
                        val config = Configuration(context.resources.configuration)

                        // 1. Force the DPI to 420+ to trigger the 5/6 column photo grid
                        config.densityDpi = 420

                        // 2. Proportionally boost the font size so the text DOES NOT shrink!
                        // (Adjust this number slightly if the text is still too small or too big)
                        config.fontScale = 1.18f

                        // Apply the fake configuration to the Google Photos context
                        val newContext = context.createConfigurationContext(config)
                        param.args[0] = newContext
                    }
                }
            )
            XposedBridge.log("PhotosGridMod: DPI Spoofing and Font Scaling hooks applied!")
        } catch (e: Throwable) {
            XposedBridge.log("PhotosGridMod Error: ${e.message}")
        }
    }
}
