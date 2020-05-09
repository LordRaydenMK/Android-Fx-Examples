package io.github.lordraydenmk.android_fx

import android.app.Application
import timber.log.Timber

class AndroidFxApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}