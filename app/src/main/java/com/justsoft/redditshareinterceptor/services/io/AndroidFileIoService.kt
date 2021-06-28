package com.justsoft.redditshareinterceptor.services.io

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.justsoft.redditshareinterceptor.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

internal class AndroidFileIoService @Inject constructor(@ApplicationContext private val context: Context) :
    FileIoService {

    override fun openOutputStream(fileUri: Uri): OutputStream =
        context.contentResolver.openOutputStream(fileUri)!!

    override fun openOutputStream(internalFilePath: String): OutputStream =
        openOutputStream(getInternalFileUri(internalFilePath))

    private fun getInternalFileUri(file: String): Uri = with(context) {
        FileProvider.getUriForFile(
            this,
            getString(R.string.provider_name),
            File(filesDir, file)
        )
    }
}