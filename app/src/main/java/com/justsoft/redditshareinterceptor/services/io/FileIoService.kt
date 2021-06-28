package com.justsoft.redditshareinterceptor.services.io

import android.net.Uri
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.OutputStream
import javax.inject.Singleton

interface FileIoService {
    fun openOutputStream(fileUri: Uri): OutputStream

    fun openOutputStream(internalFilePath: String): OutputStream
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FileIoServiceModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: AndroidFileIoService): FileIoService
}