package ch8n.project.magniffect.main.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import butterknife.OnClick
import ch8n.project.magniffect.R
import ch8n.project.magniffect.main.base.BaseActivity
import ch8n.project.magniffect.main.base.setupPermissionsWithPermissionCheck
import ch8n.project.magniffect.main.service.ShotService
import ch8n.project.magniffect.utils.AppConst
import ch8n.project.magniffect.utils.DisplayUtils
import ch8n.project.magniffect.utils.PromptUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat


class HomeActivity : BaseActivity() {

    //======== properties ==========//
    override fun setContentLayout(): Int = R.layout.activity_home

    private var mResultCode: Int = 0
    private var mResultData: Intent? = null
    private val mMediaProjectionManager: MediaProjectionManager by lazy {
        application.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    private val shotIntent: Intent by lazy {
        Intent(this, ShotService::class.java).also {
            it.action = AppConst.ACTION_SHOT
        }
    }

    //======== Android ==========//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setup() {
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mResultData?.apply {
            outState?.putInt(AppConst.STATE_RESULT_CODE, mResultCode)
            outState?.putParcelable(AppConst.STATE_RESULT_DATA, mResultData)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null == data) {
            return
        }
        if (requestCode == AppConst.REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Timber.i("User cancelled")
                Snackbar.make(
                        btn_screen_shot,
                        "Screen capture needs to be granted permission",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", { prepareToShot() })
                        .show()
                return
            }

            mResultCode = resultCode
            mResultData = data

            setUpMediaProjection()
            setUpVirtualDisplay()
            startToShot()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy()")
        tearDownMediaProjection()
    }

    //======== Actions ==========//
    @OnClick(R.id.btn_screen_shot)
    fun onCaptureClicked() {
        setupPermissionsWithPermissionCheck()
        prepareToShot()
    }

    fun prepareToShot() {
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), AppConst.REQUEST_MEDIA_PROJECTION)
    }


    companion object {
        private val notif_trigger_id = 1
    }

    //device metrics
    private val windowWidth: Int by lazy {
        DisplayUtils.getScreenWidth(this)
    }
    private val windowHeight: Int by lazy {
        DisplayUtils.getScreenHeight(this)
    }
    private val mScreenDensity: Int by lazy {
        DisplayUtils.getScreenDensity(this)
    }


    //mediaporject
    private var mediaProjection: MediaProjection? = null
    private val mImageReader: ImageReader by lazy {
        //ImageReader.newInstance(windowWidth, windowHeight, ImageFormat.JPEG , 2) //ImageFormat.RGB_565
        ImageReader.newInstance(windowWidth, windowHeight, PixelFormat.RGBA_8888, 5);
    }

    private var nameImage: String? = null
    private var virtualDisplay: VirtualDisplay? = null

    private fun setUpMediaProjection() {
        mediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData)
    }

    private fun setUpVirtualDisplay() {
        Timber.i("Setting up a VirtualDisplay: $windowWidth x $windowHeight ($mScreenDensity)")
        mImageReader.apply { }

        virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenCapture",
                windowWidth,
                windowHeight,
                mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.surface,
                null,
                null)
    }

    private fun startToShot() {
        val start = Handler()
        PromptUtils.toast(this, "Ready")
        start.postDelayed({
            PromptUtils.toast(this, "clicked")
            startCapture()
            stopScreenCapture()
        }, 1000 * 3)
    }

    private fun startCapture() {
        val dateFormat = SimpleDateFormat("yyyyMMdd-hhmmss")
        val strDate = "Screenshot_" + dateFormat.format(java.util.Date())
        val pathImage = Environment.getExternalStorageDirectory().path + "/1maginffect/recent/"
        nameImage = "$pathImage$strDate.png"
        Timber.i("image name is : $nameImage")


        val image = mImageReader.acquireLatestImage()
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        var bitmap: Bitmap? = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap?.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        image.close()

        Toast.makeText(this.applicationContext, "Saving screenshot", Toast.LENGTH_SHORT).show()

        if (bitmap != null) {
            Timber.e("bitmap create success ")
            try {
                val fileFolder = File(pathImage)
                if (!fileFolder.exists())
                    fileFolder.mkdirs()
                val file = File(nameImage)
                if (!file.exists()) {
                    Timber.e("file create success ")
                    file.createNewFile()
                }
                val out = FileOutputStream(file)

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                val media = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(file)
                media.data = contentUri
                this.sendBroadcast(media)
                Timber.i("screen image saved")
                Toast.makeText(this.applicationContext, "Screenshot saved successfully", Toast.LENGTH_SHORT).show()

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                Toast.makeText(this.applicationContext, "failed saving", Toast.LENGTH_SHORT).show()
                Timber.e(e.toString())
                e.printStackTrace()
            }

        }
    }

    private fun stopScreenCapture() {
        virtualDisplay?.apply {
            release()
            virtualDisplay = null
        }
        tearDownMediaProjection()
        Timber.i("virtual display stopped")
    }

    private fun tearDownMediaProjection() {
        mediaProjection?.apply {
            stop()
            mediaProjection = null
        }
        Timber.i("mediaProjection cleared")
    }
}
