package com.justsoft.redditshareinterceptor.model

import androidx.annotation.StringRes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.FlowCollector

data class ProcessingProgress(
    @StringRes val statusTextResourceId: Int,
    val overallProgress: Int
)

suspend fun FlowCollector<ProcessingProgress>.emit(stateId: Int, progress: Int) {
    emit(ProcessingProgress(stateId, progress))
}

@ExperimentalCoroutinesApi
suspend fun ProducerScope<ProcessingProgress>.send(stateId: Int, progress: Int) {
    send(ProcessingProgress(stateId, progress))
}
