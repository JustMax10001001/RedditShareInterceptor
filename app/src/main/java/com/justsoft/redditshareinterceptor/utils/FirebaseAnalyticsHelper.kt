package com.justsoft.redditshareinterceptor.utils

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsHelper {

    companion object {

        var mFirebaseAnalytics: FirebaseAnalytics? = null

        fun initializeInstance(context: Context) {
            if (mFirebaseAnalytics == null) {
                synchronized(FirebaseAnalyticsHelper::class.java) {
                    if (mFirebaseAnalytics == null) {
                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
                    }
                }
            }
        }

        fun getAnalytics(): FirebaseAnalytics {
            if (mFirebaseAnalytics == null)
                throw IllegalStateException("Please, call FirebaseAnalyticsHelper.initializeInstance(Context) first!")
            return mFirebaseAnalytics!!
        }
    }
}