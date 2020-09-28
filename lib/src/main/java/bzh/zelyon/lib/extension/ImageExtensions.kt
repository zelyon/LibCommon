package bzh.zelyon.lib.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File

fun Context.loadImage(any: Any? = null, actionResult:() -> Unit = {})= Glide.with(this)
    .safeLoad(any)
    .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            actionResult.invoke()
            return false
        }
        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            actionResult.invoke()
            return false
        }
    })
    .preload()

fun Context.getImageAsBitmap(any: Any): Bitmap = Glide.with(this)
    .asBitmap()
    .apply {
        when (any) {
            is String -> load(any)
            is File -> load(any)
            is Uri -> load(any)
            is Int -> load(any)
            is Drawable -> load(any)
            is Bitmap -> load(any)
            is ByteArray -> load(any)
            else -> load(any)
        }
    }
    .submit()
    .get()

fun Context.getImageAsDrawable(any: Any): Drawable = Glide.with(this)
    .asDrawable()
    .apply {
        when (any) {
            is String -> load(any)
            is File -> load(any)
            is Uri -> load(any)
            is Int -> load(any)
            is Drawable -> load(any)
            is Bitmap -> load(any)
            is ByteArray -> load(any)
            else -> load(any)
        }
    }
    .submit()
    .get()

fun Context.getImageAsFile(any: Any): File = Glide.with(this)
    .asFile()
    .apply {
        when (any) {
            is String -> load(any)
            is File -> load(any)
            is Uri -> load(any)
            is Int -> load(any)
            is Drawable -> load(any)
            is Bitmap -> load(any)
            is ByteArray -> load(any)
            else -> load(any)
        }
    }
    .submit()
    .get()

fun ImageView.setImage(any: Any, placeholder: Drawable? = null) = Glide.with(this)
    .safeLoad(any)
    .placeholder(placeholder)
    .error(placeholder)
    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    .into(this)

private fun RequestManager.safeLoad(any: Any? = null) = when (any) {
    is String -> load(any)
    is File -> load(any)
    is Uri -> load(any)
    is Int -> load(any)
    is Drawable -> load(any)
    is Bitmap -> load(any)
    is ByteArray -> load(any)
    else -> load(any)
}