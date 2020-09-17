package bzh.zelyon.lib.ui.view.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import bzh.zelyon.lib.extension.getCurrentFragment

abstract class AbsActivity: AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 4
    }

    private var intentResult:(Int, Intent) -> Unit = { _, _ -> }
    private var permissionsResult:(Boolean) -> Unit = { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        intent?.let {
            handleIntent(it)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            handleIntent(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            data?.let {
                intentResult.invoke(resultCode, data)
                intentResult = { _, _ -> }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionsResult.invoke(true)
                permissionsResult = {}
            } else {
                permissionsResult.invoke(false)
            }
        }
    }

    override fun onBackPressed() {
        if (getCurrentFragment()?.onBackPressed() == true) {
            super.onBackPressed()
        }
    }

    abstract fun getLayoutId(): Int

    abstract fun getFragmentContainerId(): Int

    open fun handleIntent(intent: Intent) {}

    fun startIntentWithResult(intent: Intent, intentResult:(Int, Intent) -> Unit) {
        this.intentResult = intentResult
        startActivityForResult(intent, REQUEST_CODE)
    }

    fun ifPermissions(vararg permissions: String, permissionsResult:(Boolean) -> Unit) {
        if (permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            permissionsResult.invoke(true)
        } else {
            this.permissionsResult = permissionsResult
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }
    }
}