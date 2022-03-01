package com.serenade.screenhots

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * @author guoxinrui
 * 截屏监听管理器
 */
class ScreenHotsManager private constructor() {
    private var mPath: String = ""
    private lateinit var context: Context
    private var contentObserverManager: ScreenHotContentObserverManager? = null

    companion object {
        val KEYWORDS = listOf(
            "screenshots", "screenshot", "screen_shot", "screen-shot", "screen shot",
            "screencapture", "screen_capture", "screen-capture", "screen capture",
            "screencap", "screen_cap", "screen-cap", "screen cap", "截屏"
        )
        const val TAG = "ScreenHotsManager"
        private val instance: ScreenHotsManager by lazy { ScreenHotsManager() }

        fun with(context: Context): ScreenHotsManager {
            instance.context = context
            return instance
        }
    }

    /**
     * 开始监听
     */
    fun start(block: (path: String) -> Unit) {
        //判断存储权限
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Log.d(TAG, "registerScreenShotFileObserver")
        //开启FileObserver监听(主要适配小米、vivo、oppo)
        ScreenShotFileObserverManager.registerScreenShotFileObserver(object :
            ScreenShotFileObserver.ScreenShotLister {
            override fun finishScreenShot(path: String?) {
                path?.let {
                    //判断当前应用是否可见
                    if (!isForeground()) return
                    //判断截图返回路径与上次是否相同(去重)
                    if (it.isEmpty() || it == mPath) return
                    Log.d(TAG, "ScreenShotFileObserver success, path is $it")
                    this@ScreenHotsManager.mPath = path
                    Handler(Looper.getMainLooper()).post {
                        block(path)
                    }
                }
            }
        })
        Log.d(TAG, "registerScreenShotContentObserver")
        contentObserverManager = ScreenHotContentObserverManager(context)
        contentObserverManager?.registerScreenHotContentObserver(object :
            ScreenHotContentObserverManager.ScreenHotListener {
            override fun onScreenHotSuccess(path: String, dateTaken: Long) {
                //判断当前应用是否可见
                if (!isForeground()) return
                //判断截图返回路径与上次是否相同(去重)
                if (path.isEmpty() || path == mPath) return
                Log.d(TAG, "ScreenShotContentObserver success, path is $path,dateTaken:$dateTaken")
                this@ScreenHotsManager.mPath = path
                block(path)
            }
        })
    }

    /**
     * 注销释放
     */
    fun recycle() {
        Log.d(TAG, "unregisterAllScreenShotObserver")
        ScreenShotFileObserverManager.unregisterScreenShotFileObserver()
        contentObserverManager?.unregisterScreenHotContentObserver()
    }

    fun isForeground(): Boolean {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val appProcesses: List<ActivityManager.RunningAppProcessInfo> =
            activityManager.runningAppProcesses
        if (appProcesses.isEmpty()) return false
        for (appProcess in appProcesses) {
            if (appProcess.processName.contains(context.packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                return true
            }
        }
        return false
    }
}