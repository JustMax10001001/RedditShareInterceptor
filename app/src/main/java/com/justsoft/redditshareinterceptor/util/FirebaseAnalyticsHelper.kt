package com.justsoft.redditshareinterceptor.util

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsHelper {

    companion object {

        var mFirebaseAnalytics: FirebaseAnalytics? = null

        fun getInstance(context: Context): FirebaseAnalytics {
            if (mFirebaseAnalytics == null) {
                synchronized(FirebaseAnalyticsHelper::class.java) {
                    if (mFirebaseAnalytics == null) {
                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
                    }
                }
            }
            return mFirebaseAnalytics!!
        }

        fun getInstance(): FirebaseAnalytics {
            if (mFirebaseAnalytics == null)
                throw IllegalStateException("Please, call FirebaseAnalyticsHelper.getInstance(context) first!")
            return mFirebaseAnalytics!!
        }
    }
}