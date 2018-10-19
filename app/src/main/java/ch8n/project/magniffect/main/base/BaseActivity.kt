package ch8n.project.magniffect.main.base

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import ch8n.project.magniffect.R
import ch8n.project.magniffect.utils.PromptUtils
import ch8n.project.magniffect.utils.PromptUtils.negativeButton
import ch8n.project.magniffect.utils.PromptUtils.positiveButton
import permissions.dispatcher.*
import timber.log.Timber

@RuntimePermissions
abstract class BaseActivity : AppCompatActivity() {

    abstract fun setContentLayout(): Int
    private val contentLayout: Int get() = setContentLayout()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentLayout)
        ButterKnife.bind(this)
        setup()
    }

    abstract fun setup()

    //======== Permissions base ==========//
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun setupPermissions() { Timber.e("Permission Applied") }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForStorage(request: PermissionRequest) {
        PromptUtils.alertDialog(this, R.style.AlertDialog_AppCompat_Light) {
            setTitle("Permission")
            setMessage("Storage read and write permission?")
            positiveButton("Retry") {
                it.dismiss()
                request.proceed()
            }
            negativeButton() {
                //do negative actions
                it.dismiss()
            }
        }.show()
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStoragePermissionDenied() {
        PromptUtils.alertDialog(this, R.style.AlertDialog_AppCompat_Light) {
            setTitle("Denied")
            setMessage("Cannot Continue without permission")
            negativeButton() {
                //do negative actions
                it.dismiss()
            }
        }.show()
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onStorageNeverAskAgain() {
        PromptUtils.alertDialog(this, R.style.AlertDialog_AppCompat_Light) {
            setTitle("Go to Settings")
            setMessage("You denied permanently, go to settings and apply")
            negativeButton("Settings") {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:" + packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                })
                it.dismiss()
            }
        }.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
}
