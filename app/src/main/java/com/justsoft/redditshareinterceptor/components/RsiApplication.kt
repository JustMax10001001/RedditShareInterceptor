package com.justsoft.redditshareinterceptor.components

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.utils.FirebaseAnalyticsHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RsiApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        initializeFirebaseAnalytics()

        sApplicationContext = applicationContext
    }

    private fun initializeFirebaseAnalytics() {
        FirebaseApp.initializeApp(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseAnalyticsHelper.initializeInstance(this)
    }

    companion object {
        var sApplicationContext: Context? = null
    }
}