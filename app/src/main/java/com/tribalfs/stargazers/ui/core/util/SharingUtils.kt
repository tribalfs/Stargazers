package com.tribalfs.stargazers.ui.core.util

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.pm.PackageManager
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

object SharingUtils {

    private const val SAMSUNG_QUICK_SHARE_PACKAGE = "com.samsung.android.app.sharelive"
    private const val MIME_TYPE_TEXT = "text/plain"

    fun File.share(context: Context, title: String? = null, subject: String? = null, onShared: (() -> Unit)? = null) {
        val fileUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)

        context.createShareIntent(title, subject).apply {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            start(context, onShared)
        }
    }

    fun String.share(context: Context, title: String? = null, subject: String? = null, onShared: (() -> Unit)? = null) {
        context.createShareIntent(title, subject).apply {
            type = MIME_TYPE_TEXT
            putExtra(Intent.EXTRA_TEXT, this@share)
            start(context, onShared)
        }
    }

    private fun Context.createShareIntent(title: String?, subject: String?) =
        Intent().apply {
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            action = ACTION_SEND
            if (isSamsungQuickShareAvailable()) {
                `package` = SAMSUNG_QUICK_SHARE_PACKAGE
            }
        }

    private fun Intent.start(context: Context, onShared: (() -> Unit)?){
        try {
            context.startActivity(this)
        } catch (e: Exception) {
            // Fallback to default chooser if specific package fails
            `package` = null
            context.startActivity(Intent.createChooser(this, "Share via"))
            onShared?.invoke()
        }
    }

    fun Context.isSamsungQuickShareAvailable(): Boolean {
        return try {
            packageManager.getPackageInfo(SAMSUNG_QUICK_SHARE_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }.also {
            Log.d("SharingUtils", "isSamsungQuickShareAvailable: $it")
        }
    }

}