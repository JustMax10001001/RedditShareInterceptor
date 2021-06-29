package com.justsoft.redditshareinterceptor.providers.media

import android.content.Intent
import com.justsoft.redditshareinterceptor.model.MediaPost
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

interface IntentProvider {
    fun provideInternalMediaIntent(mediaPost: MediaPost): Intent

    fun parseInternalMediaIntent(intent: Intent): MediaPost

    fun provideMediaShareIntent(mediaPost: MediaPost): Intent
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class IntentProviderModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: IntentProviderImpl): IntentProvider
}