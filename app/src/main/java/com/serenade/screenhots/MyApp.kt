package com.serenade.screenhots

import android.app.Application
import android.util.Log
import android.widget.Toast

class MyApp:Application() {
    override fun onCreate() {
        super.onCreate()
        ScreenHotsManager.with(this).start {
            Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTerminate() {
        ScreenHotsManager.with(this).recycle()
        super.onTerminate()
    }
}