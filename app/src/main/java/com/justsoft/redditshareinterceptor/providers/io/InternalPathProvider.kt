package com.justsoft.redditshareinterceptor.providers.io

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

interface InternalPathProvider {
    fun getInternalPath(filePath: String): String

    fun getInternalFile(filePath: String): File
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class InternalPathProviderModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: InternalPathProviderImpl): InternalPathProvider
}