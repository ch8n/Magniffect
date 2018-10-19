package ch8n.project.magniffect.main.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import ch8n.project.magniffect.MagniffectApp
import ch8n.project.magniffect.utils.DisplayUtils
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat

class ShotService : Service() {

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

    //notification
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    //mediaporject
    private val mMediaProjectionManager1: MediaProjectionManager by lazy {
        application.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    private var mediaProjection: MediaProjection? = null
    private val mImageReader: ImageReader by lazy {
        ImageReader.newInstance(windowWidth, windowHeight, ImageFormat.JPEG, 4) //ImageFormat.RGB_565
    }

    var mResultCode: Int = 0
    var mResultData: Intent? = null
    private var nameImage: String? = null
    private var virtualDisplay: VirtualDisplay? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setUpMediaProjection()
        setUpVirtualDisplay()
        startToShot()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setUpMediaProjection() {
        mResultData = (application as MagniffectApp).getIntent() // from app
        mResultCode = (application as MagniffectApp).getResult() // from app
        mediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData)
    }

    private fun setUpVirtualDisplay() {
        Timber.i("Setting up a VirtualDisplay: $windowWidth x $windowHeight ($mScreenDensity)")
        virtualDisplay = mediaProjection?.createVirtualDisplay("ScreenCapture",
                windowWidth, windowHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null)
    }

    private fun startToShot() {
        notificationManager.cancel(notif_trigger_id)
        val start = Handler()
        start.postDelayed({
            startCapture()
            stopScreenCapture()
        }, 800)

    }

    private fun startCapture() {
        val dateFormat = SimpleDateFormat("yyyyMMdd-hhmmss")
        val strDate = "Screenshot_" + dateFormat.format(java.util.Date())
        val pathImage = Environment.getExternalStorageDirectory().path + "/maginffect/recent/"
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
        Timber.i("virtual display stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy()")
        tearDownMediaProjection()
    }

    private fun tearDownMediaProjection() {
        mediaProjection?.apply {
            stop()
            mediaProjection = null
        }
        Timber.i("mediaProjection cleared")
    }
}