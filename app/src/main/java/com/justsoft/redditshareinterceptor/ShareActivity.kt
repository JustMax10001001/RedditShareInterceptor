package com.justsoft.redditshareinterceptor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File


class ShareActivity : AppCompatActivity() {

    private var selectedUri: Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ACTION_PICK_MEDIA -> {
                    findViewById<TextView>(R.id.textViewSelectedUri).text =
                        getString(R.string.selected_file).format(data?.data?.toString() ?: "none")
                    selectedUri = data?.data!!
                }

            }
        }
    }

    fun pickFileOnClick(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (Build.VERSION.SDK_INT >= 19) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        } else {
            intent.type = "image/* video/*"
        }
        startActivityForResult(intent, ACTION_PICK_MEDIA)
    }

    fun shareOnClick(view: View) {
        val reqFile = File(filesDir, "img4.jpg")
        Log.d("ShA", reqFile.absolutePath)
        var fileUri: Uri? = null

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            //type = "image/*"

            fileUri = try {
                FileProvider.getUriForFile(
                    this@ShareActivity,
                    "com.justsoft.redditshareinterceptor.provider",
                    reqFile
                )
            } catch (e: IllegalArgumentException) {
                Log.e(
                    "File Selector",
                    "The selected file can't be shared: $reqFile", e
                )
                null
            }
            Log.d("ShA", fileUri.toString())

            if (fileUri != null) {
                Log.d("ShA", contentResolver.getType(fileUri!!) ?: "null")

                setDataAndType(fileUri, "image/*")
                putExtra(Intent.EXTRA_STREAM, fileUri)

            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    companion object {
        private const val ACTION_PICK_MEDIA = 1000
    }
}