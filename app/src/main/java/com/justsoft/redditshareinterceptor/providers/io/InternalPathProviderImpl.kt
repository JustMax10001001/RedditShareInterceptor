package com.justsoft.redditshareinterceptor.providers.io

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class InternalPathProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : InternalPathProvider {
    override fun getInternalPath(filePath: String): String = getInternalFile(filePath).absolutePath

    override fun getInternalFile(filePath: String): File = File(context.filesDir, filePath)
}