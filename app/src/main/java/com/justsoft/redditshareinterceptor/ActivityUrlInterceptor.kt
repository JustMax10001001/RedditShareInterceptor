package com.justsoft.redditshareinterceptor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.checkUrlString

class ActivityUrlInterceptor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseAnalyticsHelper.getInstance(this)

        val startIntent = intent

        if (startIntent == null) {
            Log.w(ACTIVITY_LOG_TAG, "startIntent is null, finishing...")

            showParseErrorToast()
            finishActivityNoAnimation()
            return
        }

        if (startIntent.extras == null) {
            Log.w(ACTIVITY_LOG_TAG, "startIntent.extras is null, finishing...")

            showParseErrorToast()
            finishActivityNoAnimation()
            return
        }

        Log.d(
            INTENT_HANDLER_LOG_TAG,
            "Received intent action=${startIntent.action}, type=${startIntent.type}"
        )
        when (startIntent.action) {
            Intent.ACTION_SEND -> {
                if (startIntent.type != "text/plain") {
                    Log.w(
                        INTENT_HANDLER_LOG_TAG,
                        "Unexpected startIntent MIME type of ${startIntent.type}!"
                    )
                }

                val intentUrl = startIntent.extras!!.getString(Intent.EXTRA_TEXT)

                if (!checkUrlString(intentUrl)) {
                    Log.e(INTENT_HANDLER_LOG_TAG, "Invalid web url: $intentUrl")

                    showParseErrorToast()
                    finishActivityNoAnimation()
                    return
                }

                Log.i(INTENT_HANDLER_LOG_TAG, "Intent url is $intentUrl")
                val serviceIntent = constructServiceIntent()

                finishActivityNoAnimation()

                startService(serviceIntent)
            }
        }
    }

    private fun constructServiceIntent(): Intent {
        return Intent(this, UniversalProcessorForegroundService::class.java).apply {
            action = UniversalProcessorForegroundService.ACTION_PROCESS_URL
            putExtra(
                Intent.EXTRA_TEXT,
                intent?.extras?.get(Intent.EXTRA_TEXT).toString()
            )
        }
    }

    private fun showParseErrorToast() {
        Toast.makeText(
            applicationContext,
            "Could not get post URL",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun finishActivityNoAnimation() {
        finish()
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val ACTIVITY_LOG_TAG = "ActivityUrlInterceptor"
        private const val INTENT_HANDLER_LOG_TAG = "IntentHandler"
    }
}