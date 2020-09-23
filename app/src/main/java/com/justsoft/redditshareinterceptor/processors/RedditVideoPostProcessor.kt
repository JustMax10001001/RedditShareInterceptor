package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
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
        /*val htmlDoc = Jsoup.parse(
            requestHelper.readHttpJsonResponse(
                RIPSAVE_GETLINK_URL,
                mutableMapOf("url" to redditPost.url)
            ).getString("data")
        )*/
        val htmlDoc = Jsoup
            .connect(RIPSAVE_GETLINK_URL)
            .data(
                mapOf(
                    "url" to redditPost.url
                )
            )
            .get()
        val tableColumn = htmlDoc
            .getElementsByClass("table-col")[0]         // get table column, then check if there is element with value "Video with Audio"
        val downloadTables = tableColumn.getElementsByClass("downloadTable")

        val contentType = if (downloadTables.size == 1) MediaContentType.GIF else MediaContentType.VIDEO

        val table = getBestTable(downloadTables, tableColumn)
        val tableRows = table
            .getElementsByTag("tbody")[0]
            .getElementsByTag("tr")

        val bestLink = if (contentType == MediaContentType.GIF)
            getBestLink(tableRows)
        else
            genVideoLink(getBestLink(tableRows), requestHelper)

        savedState.putBoolean(BUNDLE_IS_GIF, contentType == MediaContentType.GIF)
        savedState.putString(BUNDLE_BEST_URL, bestLink)

        return contentType
    }

    private fun genVideoLink(genlinkUrL: String, requestHelper: RequestHelper): String {
        val linkJson = requestHelper
            .readHttpJsonResponse("$RIPSAVE_LINK$genlinkUrL")
            .getJSONObject("data")
        return StringBuilder()
            .append("${RIPSAVE_LINK}/download")
            .append("?s=${linkJson.getString("s")}")
            .append("&f=${linkJson.getString("f")}")
            .append("&t=${URLEncoder.encode(linkJson.getString("t"), "utf-8")}")
            .toString()
    }

    private fun getBestLink(tableRows: Elements): String {
        val downloadMap = mutableMapOf<Int, String>()
        tableRows.forEach {
            val tds = it.getElementsByTag("td")
            downloadMap[getResolutionFromTd(tds[0])] = tds[2]
                .getElementsByTag("a")[0]
                .attr("href")
        }
        return downloadMap[downloadMap.maxOfOrNull { it.key }]!!
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

    override fun getAllPossibleMediaDownloads(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaList = savedState.getString(BUNDLE_BEST_URL)!!

    companion object {
        const val RIPSAVE_LINK = "https://ripsave.com"
        const val RIPSAVE_GETLINK_URL = "https://ripsave.com/getlink"

        const val BUNDLE_IS_GIF = "isGif"
        const val BUNDLE_BEST_URL = "bestUrl"
    }
}