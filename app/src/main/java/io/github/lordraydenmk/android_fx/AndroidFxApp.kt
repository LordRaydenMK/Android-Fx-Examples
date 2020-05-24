package io.github.lordraydenmk.android_fx

import android.app.Application
import android.content.Context
import io.github.lordraydenmk.android_fx.example10.ProductionRepositoryCache
import io.github.lordraydenmk.android_fx.example10.RepositoryCache
import timber.log.Timber

class AndroidFxApp : Application() {

    val repositoryCache: RepositoryCache = ProductionRepositoryCache.create()
        .unsafeRunSync()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    companion object {

        fun app(context: Context): AndroidFxApp = context.applicationContext as AndroidFxApp
    }
}