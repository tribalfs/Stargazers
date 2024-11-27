//package com.tribalfs.stargazers.ui.core.util
//
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.media.Image
//import android.net.Uri
//import android.util.Log
//import android.webkit.MimeTypeMap
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.content.ContextCompat.startActivity
//import androidx.core.content.FileProvider
//import com.tribalfs.stargazers.data.model.Stargazer
//import java.io.File
//
//fun File.sendOnQuickShare(context: Context) {
//    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
//
//    val shareIntent = Intent(Intent.ACTION_SEND).apply {
//        type = getMimeType()
//        putExtra(Intent.EXTRA_STREAM, fileUri)
//        putExtra(Intent.EXTRA_TITLE, "Share QR Code")
//        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//    }
//
//    if (isNearbyShareAvailable(context)){
//        shareIntent.apply {
//            `package` = "com.google.android.gms" // Google Nearby Share
//
//        }
//        startActivity(intent)
//    }
//
//    val pm = context.packageManager
//    val resInfoList = pm.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
//
//    var shareAppPackageName = ""
//
//    val quickShareAvailable = resInfoList.any { it.activityInfo.packageName.startsWith("com.google.android.gms") }
//    if (quickShareAvailable) {
//        shareAppPackageName = "com.google.android.gms"
//    } else {
//        val samsungShareAvailable = resInfoList.any { it.activityInfo.packageName == "com.samsung.android.app.sharelive" }
//        if (samsungShareAvailable) {
//            shareAppPackageName = "com.samsung.android.app.sharelive"
//        } else {
//            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
//        }
//    }
//
//    shareIntent.setPackage(shareAppPackageName)
//    try {
//        context.startActivity(shareIntent)
//    } catch (e: Exception) {
//        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
//        Log.e("ShareError", "Error launching share intent: ${e.message}")
//    }
//}
//
//fun File.getMimeType(): String {
//    val extension = extension.lowercase()
//    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
//    return mimeType ?: when (extension) {
//        "jpg", "jpeg", "png", "gif", "bmp" -> "image/*"
//        "mp4", "avi", "mkv", "webm" -> "video/*"
//        "mp3", "wav", "flac" -> "audio/*"
//        "pdf" -> "application/pdf"
//        "txt" -> "text/plain"
//        else -> "*/*"
//    }
//}
//
//fun isSamsungQuickShareAvailable(context: Context): Boolean {
//    return try {
//        context.packageManager.getPackageInfo("com.samsung.android.app.sharelive", 0)
//        true
//    } catch (e: PackageManager.NameNotFoundException) {
//        false
//    }
//}
//
//fun isNearbyShareAvailable(context: Context): Boolean {
//    return try {
//        context.packageManager.getPackageInfo("com.google.android.gms", 0)
//        true
//    } catch (e: PackageManager.NameNotFoundException) {
//        false
//    }
//}