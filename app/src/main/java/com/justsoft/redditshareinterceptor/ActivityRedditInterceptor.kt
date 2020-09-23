package com.justsoft.redditshareinterceptor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ActivityRedditInterceptor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                Log.d("IntentHandler", "Received intent action=ACTION_SEND, type=" + intent?.type)
                if (intent?.type == "text/plain") {
                    Log.d("IntentHandler", "Raw value is " + intent?.extras?.get(Intent.EXTRA_TEXT))
                    val serviceIntent = Intent(this, RedditProcessorService::class.java).apply {
                        action = ACTION_PROCESS_REDDIT_URL
                        putExtra(
                            Intent.EXTRA_TEXT,
                            intent?.extras?.get(Intent.EXTRA_TEXT).toString()
                        )
                    }

                    //startService(serviceIntent)
                    RedditProcessorService.enqueueWork(applicationContext, serviceIntent)
                    finish()
                    overridePendingTransition(0, 0)

                }
            }
        }
    }
}