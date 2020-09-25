package com.justsoft.redditshareinterceptor.model

import androidx.annotation.StringRes

class ProcessingProgress(
    @StringRes val statusTextResourceId: Int,
    val overallProgress: Int
)
