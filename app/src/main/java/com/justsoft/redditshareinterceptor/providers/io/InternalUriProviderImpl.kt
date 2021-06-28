package com.justsoft.redditshareinterceptor.providers.io

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.justsoft.redditshareinterceptor.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class InternalUriProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : InternalUriProvider {
    override fun provideUriForPath(path: String): Uri =
        FileProvider.getUriForFile(context, context.getString(R.string.provider_name), File(path))
}