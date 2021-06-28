package com.justsoft.redditshareinterceptor.providers.media

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

interface MediaPathProvider {
    fun getUriForMediaType(mediaType: MediaContentType, galleryIndex: Int = 0): Uri

    fun getFileForMediaType(mediaType: MediaContentType, galleryIndex: Int = 0): File
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MediaUriProviderModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: MediaPathProviderImpl): MediaPathProvider
}