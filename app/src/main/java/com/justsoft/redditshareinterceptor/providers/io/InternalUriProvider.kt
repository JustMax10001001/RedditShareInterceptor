package com.justsoft.redditshareinterceptor.providers.io

import android.net.Uri
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

interface InternalUriProvider {
    fun provideUriForPath(path: String): Uri
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class InternalUriProviderModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: InternalUriProviderImpl): InternalUriProvider
}