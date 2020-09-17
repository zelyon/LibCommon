package bzh.zelyon.lib.extension

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Patterns
import android.util.TypedValue
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL

fun Context.dpToPx(int: Int) = int * resources.displayMetrics.density

fun Context.pxToDp(int: Int) = int / resources.displayMetrics.density

fun Context.vibrate() {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (isOreo()) {
        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(1000)
    }
}

fun Context.drawableResToDrawable(@DrawableRes drawableRes: Int, @ColorRes colorRes: Int? = null): Drawable {
    val result = ContextCompat.getDrawable(this, drawableRes) ?: ColorDrawable()
    colorRes?.let {
        result.mutate().setColorFilter(colorResToColorInt(colorRes), PorterDuff.Mode.SRC_IN)
    }
    return result
}

fun Context.colorResToColorInt(@ColorRes colorRes: Int, alpha: Float? = null): Int {
    var result = ContextCompat.getColor(this, colorRes)
    alpha?.let {
        result = ColorUtils.setAlphaComponent(result, 255*alpha.toInt())
    }
    return result
}

fun Context.getResIdFromAndroidAttr(androidId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(androidId, typedValue, true)
    return typedValue.resourceId
}

fun Context.getBitmapFromCameraUri(uri: Uri): Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

fun Context.getLocalFileFromGalleryUri(uri: Uri, optionalName: String? = null): File? {
    val isFile = uri.scheme == ContentResolver.SCHEME_FILE
    val isDocumentUri = DocumentsContract.isDocumentUri(this, uri)
    val isExternalStorage = isDocumentUri && uri.authority == "com.android.externalstorage.documents"
    val isDownloadStorage = isDocumentUri && uri.authority == "com.android.providers.downloads.documents"
    val isImageStorage = isDocumentUri && uri.authority == "com.android.providers.media.documents"
    val isContent = uri.scheme == ContentResolver.SCHEME_CONTENT
    val isGooglePhotosNew = isContent && uri.authority == "com.google.android.apps.photos.contentprovider"
    val isGooglePhotos = isContent && uri.authority == "com.google.android.apps.photos.content"
    val isDrive = isContent && uri.authority == "com.google.android.apps.docs.storage"
    val isContentStorage = isContent && uri.path?.contains("/storage") ?: false

    val path = when {
        isFile -> uri.path
        isExternalStorage -> Environment.getExternalStorageDirectory().toString() + "/" + DocumentsContract.getDocumentId(uri).split(":")[1]
        isDownloadStorage -> getPathFromCursor(ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), DocumentsContract.getDocumentId(uri).toLong()), arrayOf(MediaStore.Images.Media.DATA))
        isImageStorage -> getPathFromCursor(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media.DATA), MediaStore.Images.Media._ID + "=?", DocumentsContract.getDocumentId(uri).split(":")[1])
        isGooglePhotos -> uri.lastPathSegment
        isContentStorage -> uri.path?.substring(uri.path?.indexOf("/storage") ?: 0)
        isDrive || isGooglePhotosNew || isContent -> getPathFromCursor(uri, arrayOf(MediaStore.Images.Media.DATA))
        else -> null
    }

    var name = System.currentTimeMillis().toString().plus(".").plus(getExtension(if (path != null) Uri.parse(path) else uri))
    contentResolver.query(uri, null, null, null, null)?.use {
        if (it.moveToFirst()) {
            it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))?.let { displayName ->
                name = displayName
            }
        }
        it.close()
    }

    val outputFile = createNewFile(optionalName ?: name)
    when {
        path != null -> FileInputStream(File(path))
        Patterns.WEB_URL.matcher(uri.toString()).matches() && URLUtil.isValidUrl(uri.toString()) -> URL(uri.toString()).openStream()
        else -> contentResolver.openInputStream(uri)
    }?.use { inputStream ->
        FileOutputStream(outputFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return if (outputFile.exists()) outputFile else null
}

fun Context.getPathFromCursor(uri: Uri, projection: Array<String>? = null, selection: String? = null, vararg selectionArgs: String): String? {
    try {
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            it.close()
        }
    } catch (e: Exception) {}
    return null
}

fun Context.createNewFile(name: String): File {
    val file = File(externalCacheDir, name)
    return if (file.exists()) createNewFile("_$name") else file
}

fun Context.getExtension(uri: Uri) = if (uri.scheme == ContentResolver.SCHEME_CONTENT) MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri)) else MimeTypeMap.getFileExtensionFromUrl(uri.path)