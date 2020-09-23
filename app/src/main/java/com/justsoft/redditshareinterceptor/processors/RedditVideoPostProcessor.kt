package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.*
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URLEncoder

class RedditVideoPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("v.redd.it")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType {
        val htmlDoc = Jsoup
            .connect(RIPSAVE_GETLINK_URL)
            .data(mapOf("url" to redditPost.url))
            .get()
        val tableColumn = htmlDoc
            .getElementsByClass("table-col")[0]         // get table column, then check if there is element with value "Video with Audio"
        val downloadTables = tableColumn.getElementsByClass("downloadTable")

        val contentType =
            if (downloadTables.size == 1) MediaContentType.GIF else MediaContentType.VIDEO

        val table = getBestTable(downloadTables, tableColumn)
        val tableRows = table
            .getElementsByTag("tbody")[0]
            .getElementsByTag("tr")

        val links = if (contentType == MediaContentType.GIF)
            getLinks(tableRows)
        else
            generateVideoLinks(getLinks(tableRows), requestHelper)

        savedState.putBoolean(BUNDLE_IS_GIF, contentType == MediaContentType.GIF)
        savedState.putStringArray(BUNDLE_URLS, links.toArray(arrayOf<String>()))

        return contentType
    }

    override fun downloadMediaMatchingMediaSpec(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper,
        mediaSpec: MediaSpec,
        destinationDescriptorGenerator: (MediaContentType, Int) -> ParcelFileDescriptor
    ): MediaList {
        val urls = savedState.getStringArray(BUNDLE_URLS)
            ?: throw IllegalStateException("savedState does not contain $BUNDLE_URLS key")
        val isGif = savedState.getBoolean(BUNDLE_IS_GIF)
        val bestFile = generateMediaList(isGif, urls, requestHelper).getMostSuitableMedia(mediaSpec)

        requestHelper.downloadFile(
            bestFile[0].downloadUrl,
            destinationDescriptorGenerator(
                if (isGif) MediaContentType.GIF else MediaContentType.VIDEO,
                0
            )
        )
        return bestFile
    }

    private fun generateMediaList(
        isGif: Boolean,
        directUrls: Array<String>,
        requestHelper: RequestHelper
    ): MediaList {
        val contentType = if (isGif) MediaContentType.GIF else MediaContentType.VIDEO
        val list = mediaListOf(contentType)
        runBlocking(Dispatchers.IO) {
            for (url in directUrls)
                launch {
                    list.add(
                        MediaModel(url, contentType, requestHelper.getContentLength(url))
                    )
                }
        }
        return list
    }

    private fun generateVideoLinks(
        genlinkUrls: Collection<String>,
        requestHelper: RequestHelper
    ): ArrayList<String> {
        val links = ArrayList<String>()
        runBlocking(Dispatchers.IO) {
            for (genlinkUrl in genlinkUrls)
                launch {
                    val linkJson = requestHelper
                        .readHttpJsonResponse("$RIPSAVE_LINK$genlinkUrl")
                        .getJSONObject("data")
                    @Suppress("BlockingMethodInNonBlockingContext")
                    links.add(
                        StringBuilder()
                            .append("${RIPSAVE_LINK}/download")
                            .append("?s=${linkJson.getString("s")}")
                            .append("&f=${linkJson.getString("f")}")
                            .append("&t=${URLEncoder.encode(linkJson.getString("t"), "utf-8")}")
                            .toString()
                    )
                }
        }
        return links
    }

    private fun getLinks(tableRows: Elements): ArrayList<String> {
        val downloadMap = mutableMapOf<Int, String>()
        tableRows.forEach {
            val tds = it.getElementsByTag("td")
            downloadMap[getResolutionFromTd(tds[0])] = tds[2]
                .getElementsByTag("a")[0]
                .attr("href")
        }
        return ArrayList(downloadMap.values)
    }

    private fun getResolutionFromTd(td: Element): Int =
        td.text().replace("HD", "").trim().toInt()

    private fun getBestTable(downloadTables: Elements, tableColumn: Element): Element {
        return if (downloadTables.size == 1)
            downloadTables[0]
        else
            tableColumn
                .getElementsContainingText("Video with Audio")[0]   // current tag: <i>
                .parent()       // <h5>
                .parent()       // <div>
                .getElementsByTag("table")[0]

    }

    companion object {
        const val RIPSAVE_LINK = "https://ripsave.com"
        const val RIPSAVE_GETLINK_URL = "https://ripsave.com/getlink"

        const val BUNDLE_IS_GIF = "isGif"
        const val BUNDLE_URLS = "urls"
    }
}

