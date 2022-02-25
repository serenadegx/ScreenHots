package com.serenade.screenhots

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.util.*

class ScreenHotContentObserver(
    private var mContext: Context,
    private var contentUri: Uri,
    private var handler: Handler,
    private var mListener: ScreenHotContentObserverManager.ScreenHotListener
) : ContentObserver(handler) {
    private val MEDIA_PROJECTIONS = arrayOf(
        MediaStore.Images.ImageColumns.DATA,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
    )

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        handleMediaContentChange(contentUri)
    }

    @SuppressLint("Range")
    private fun handleMediaContentChange(contentUri: Uri) {
        if (ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            val cursor: Cursor =
                mContext.getContentResolver().query(
                    contentUri, MEDIA_PROJECTIONS, null, null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
                ) ?: return
            if (!cursor.moveToFirst()) {
                return
            }
            val path: String =
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
            val dateTaken: Long =
                cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN))
            if (checkScreenHot(path)) {
                handler.post(Runnable { mListener.onScreenHotSuccess(path, dateTaken) })
            }
        }
    }

    private fun checkScreenHot(path: String): Boolean {
        for (keyWord in ScreenHotsManager.KEYWORDS) {
            if (path.toLowerCase(Locale.getDefault()).contains(keyWord)) {
                return true
            }
        }
        return false
    }
}
