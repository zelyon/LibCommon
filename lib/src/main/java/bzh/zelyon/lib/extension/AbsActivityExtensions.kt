package bzh.zelyon.lib.extension

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.multidex.BuildConfig
import bzh.zelyon.lib.ui.view.activity.AbsActivity
import bzh.zelyon.lib.ui.view.fragment.AbsBottomSheetFragment
import bzh.zelyon.lib.ui.view.fragment.AbsFragment
import com.google.android.material.snackbar.Snackbar
import java.io.File

fun AbsActivity.getCurrentFragment() = supportFragmentManager.findFragmentById(getFragmentContainerId()) as? AbsFragment

fun AbsActivity.showFragment(fragment: Fragment, addToBackStack: Boolean = getCurrentFragment() != null, transitionView: View? = null) {
    when (fragment) {
        is AbsFragment -> supportFragmentManager.beginTransaction().replace(getFragmentContainerId(), fragment).apply {
            if (addToBackStack) {
                addToBackStack(fragment.javaClass.name)
            }
            transitionView?.let {
                setReorderingAllowed(true).addSharedElement(transitionView, transitionView.transitionName)
            }
        }.commit()
        is AbsBottomSheetFragment -> fragment.show(supportFragmentManager, fragment.javaClass.name)
    }
}

fun AbsActivity.fullBack() {
    back(supportFragmentManager.backStackEntryCount)
}

fun AbsActivity.back(nb: Int = 1) {
    for (i in 0..nb) {
        supportFragmentManager.popBackStack()
    }
}

fun AbsActivity.showSnackbar(
    message: String,
    view: View = findViewById(android.R.id.content),
    duration: Int = Snackbar.LENGTH_LONG,
    actionMessage: String? = null,
    actionResult:() -> Unit = {}) =
    Snackbar.make(view, message, duration).apply {
        actionMessage?.let {
            setAction(actionMessage) {
                actionResult.invoke()
            }
        }
    }.show()

fun AbsActivity.goStore(applicationId: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$applicationId")))
}

fun AbsActivity.openCamera() {
    ifPermissions(Manifest.permission.CAMERA) {
        if (it) {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, File(externalCacheDir, System.currentTimeMillis().toString().plus(".png")))), 0)
        }
    }
}

fun AbsActivity.openGallery(multiple: Boolean = false) {
    ifPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        if (it) {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*").putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple), 0)
        }
    }
}

fun AbsActivity.getStatusBarHeight() = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))

fun AbsActivity.setBoolean(key: String, value: Boolean) = getPreferences(Context.MODE_PRIVATE).edit().putBoolean(key, value).apply()

fun AbsActivity.setString(key: String, value: String) = getPreferences(Context.MODE_PRIVATE).edit().putString(key, value).apply()

fun AbsActivity.setInt(key: String, value: Int) = getPreferences(Context.MODE_PRIVATE).edit().putInt(key, value).apply()

fun AbsActivity.getBoolean(key: String) = getPreferences(Context.MODE_PRIVATE).getBoolean(key, false)

fun AbsActivity.getString(key: String, value: String) = getPreferences(Context.MODE_PRIVATE).getString(key, null)

fun AbsActivity.getInt(key: String, value: Int) = getPreferences(Context.MODE_PRIVATE).getInt(key, 0)
