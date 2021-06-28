package com.justsoft.redditshareinterceptor.services.media

import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

interface MediaDownloadService {
    suspend fun downloadMedia(mediaList: List<MediaDownloadObject>): Flow<Double>
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MediaDownloadServiceModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: MediaDownloadServiceImpl): MediaDownloadService
}