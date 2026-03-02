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
            // Hook EVERY time a LayoutManager is attached to a RecyclerView
            val recyclerViewClass = XposedHelpers.findClass(
                "android.support.v7.widget.RecyclerView",
                lpparam.classLoader
            )

            XposedBridge.hookAllMethods(
                recyclerViewClass,
                "setLayoutManager",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val layoutManager = param.args[0]
                        if (layoutManager != null) {
                            val className = layoutManager.javaClass.name

                            // Log it using standard Android Log so Termux catches it easily
                            android.util.Log.i("PhotosGridSpy", "Found LayoutManager: $className")
                        }
                    }
                }
            )
        } catch (e: Throwable) {
            android.util.Log.e("PhotosGridSpy", "Failed to hook RecyclerView: ${e.message}")
        }
    }
}
