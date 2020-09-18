package bzh.zelyon.lib.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import java.io.File

fun Context.loadImage(any: Any? = null) = Glide.with(this).load(any, null).preload()

fun ImageView.setImage(any: Any, placeholder: Drawable? = null) = Glide.with(this).load(any, placeholder).into(this)

private fun RequestManager.load(any: Any? = null, placeholder: Drawable? = null) = when (any) {
    is String -> load(any)
    is File -> load(any)
    is Uri -> load(any)
    is Int -> load(any)
    is Drawable -> load(any)
    is Bitmap -> load(any)
    is ByteArray -> load(any)
    else -> load(any)
}.let {
    it.placeholder(placeholder)
    it.error(placeholder)
}