package com.justsoft.redditshareinterceptor.model

import androidx.annotation.StringRes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.FlowCollector

data class ProgressModel(
    @StringRes val statusTextResourceId: Int,
    val overallProgress: Int
)

suspend fun FlowCollector<ProgressModel>.emit(@StringRes stateId: Int, progress: Int) {
    emit(ProgressModel(stateId, progress))
}

@ExperimentalCoroutinesApi
suspend fun ProducerScope<ProgressModel>.send(@StringRes stateId: Int, progress: Int) {
    send(ProgressModel(stateId, progress))
}
