package com.justsoft.redditshareinterceptor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper

class ActivityUrlInterceptor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseAnalyticsHelper.getInstance(this)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                Log.d("IntentHandler", "Received intent action=ACTION_SEND, type=" + intent?.type)
                if (intent?.type == "text/plain") {
                    Log.d("IntentHandler", "Raw value is " + intent?.extras?.get(Intent.EXTRA_TEXT))
                    val serviceIntent =
                        Intent(this, UniversalProcessorForegroundService::class.java).apply {
                            action = UniversalProcessorForegroundService.ACTION_PROCESS_URL
                            putExtra(
                                Intent.EXTRA_TEXT,
                                intent?.extras?.get(Intent.EXTRA_TEXT).toString()
                            )
                        }

                    finish()
                    overridePendingTransition(0, 0)

                    startService(serviceIntent)
                }
            }
        }
    }
}