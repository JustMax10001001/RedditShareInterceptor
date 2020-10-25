package com.justsoft.redditshareinterceptor

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.android.volley.toolbox.Volley
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.util.VolleyRequestHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.OutputStream


class RedditPostHandlerIntegrationTest {

    private lateinit var postHandler: UniversalUrlProcessor

    private val volleyRequestHelper: VolleyRequestHelper by lazy {
        VolleyRequestHelper(
            Volley.newRequestQueue(InstrumentationRegistry.getInstrumentation().targetContext)
        )
    }

    @Before
    fun init() {
        val requestHelper = volleyRequestHelper
        val context = InstrumentationRegistry.getInstrumentation().context
        postHandler = UniversalUrlProcessor(
            requestHelper,
            { _, _ -> getInternalFileUri("test.test")},
            this::openStreamForUri
        )
        postHandler.finished { assert(it.processingSuccessful) }
    }

    private fun openStreamForUri(uri: Uri): OutputStream =
        InstrumentationRegistry.getInstrumentation().context.contentResolver.openOutputStream(uri)!!


    private fun getInternalFileUri(file: String): Uri {
        return FileProvider.getUriForFile(
            InstrumentationRegistry.getInstrumentation().context,
            InstrumentationRegistry.getInstrumentation().context.getString(R.string.provider_name),
            File(InstrumentationRegistry.getInstrumentation().context.filesDir, file)
        )
    }

    private fun getInternalFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            InstrumentationRegistry.getInstrumentation().context,
            "com.justsoft.redditshareinterceptor.beta.provider",
            file
        )
    }

    @Test
    fun postHandleTest_RedditVideo() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val targetFile = File(context.filesDir, "test.mp4")
        val targetFileUri = getInternalFileUri(targetFile)

        targetFile.delete()

        val postHandler = UniversalUrlProcessor(
            volleyRequestHelper,
            { _, _ -> targetFileUri },
            this::openStreamForUri
        )
        postHandler.handleUrl("https://www.reddit.com/r/okbuddyretard/comments/io5m2n/_/") { }

        assert(targetFile.exists())
        assert(targetFile.length() > 0)
    }

    @Test
    fun postHandleTest_RedditImage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val targetFile = File(context.filesDir, "test.jpg")
        val targetFileUri = getInternalFileUri(targetFile)

        targetFile.delete()

        val postHandler = UniversalUrlProcessor(
            volleyRequestHelper,
            { _, _ -> targetFileUri },
            this::openStreamForUri
        )

        postHandler.handleUrl("https://www.reddit.com/r/techsupportgore/comments/ilrwy8/gaming_laptop_overheating_very_much_work_in/") { }

        assert(targetFile.exists())
        assert(targetFile.length() > 0)
    }

    @Test
    fun postHandleText_RedditGallery() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val targetFiles = mutableListOf<File>()
        val targetFileUris = mutableListOf<Uri>()

        val postHandler = UniversalUrlProcessor(
            volleyRequestHelper,
            { contentType, index ->
                assertEquals(MediaContentType.GALLERY, contentType)

                targetFiles.add(File(context.filesDir, "test_$index.jpg"))
                targetFiles[index].delete()
                targetFileUris.add(getInternalFileUri(targetFiles[index]))

                targetFileUris[index]
            }, { openStreamForUri(it) })

        postHandler.handleUrl("https://www.reddit.com/r/announcements/comments/hrrh23/") { }

        for (file in targetFiles) {
            assert(file.exists())
            assert(file.length() > 0)
        }
    }
}