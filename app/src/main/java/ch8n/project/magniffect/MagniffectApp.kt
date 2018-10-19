package ch8n.project.magniffect

import android.app.Application
import android.content.Intent
import android.media.projection.MediaProjectionManager
import timber.log.Timber

class MagniffectApp : Application() {

    private var result: Int = 0
    private var intent: Intent? = null
    private var mMediaProjectionManager: MediaProjectionManager? = null

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    fun initTimber() = Timber.plant(Timber.DebugTree())

    fun getIntent(): Intent? = intent

    fun getResult(): Int = result

    fun setResult(result1: Int) { this.result = result1 }

    fun setIntent(intent1: Intent) { this.intent = intent1 }

    fun setMediaProjectionManager(mMediaProjectionManager: MediaProjectionManager){

    }

}