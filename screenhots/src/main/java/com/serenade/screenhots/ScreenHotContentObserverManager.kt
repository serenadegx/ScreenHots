package com.serenade.screenhots

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore


/**
 * @author guoxinrui
 * 截图内容监听
 */
class ScreenHotContentObserverManager(private var context: Context) {
    private var mInternalObserver: ScreenHotContentObserver? = null
    private var mExternalObserver: ScreenHotContentObserver? = null

    fun registerScreenHotContentObserver(listener: ScreenHotListener) {
        val mHandlerThread = HandlerThread("Screenshot_Observer")
        mHandlerThread.start()
        val mHandler = Handler(mHandlerThread.looper)
        mInternalObserver =
            ScreenHotContentObserver(
                context,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                mHandler,
                listener
            )
        mExternalObserver =
            ScreenHotContentObserver(
                context,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mHandler,
                listener
            )
        mInternalObserver?.let {
            context.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                false, it
            )
        }
        mExternalObserver?.let {
            context.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false, it
            )
        }

    }

    fun unregisterScreenHotContentObserver() {
        mInternalObserver?.let { context.getContentResolver().unregisterContentObserver(it) }
        mExternalObserver?.let { context.getContentResolver().unregisterContentObserver(it) }
    }

    interface ScreenHotListener {
        fun onScreenHotSuccess(path: String, dateTaken: Long)
    }
}