package com.justsoft.redditshareinterceptor.model

import org.json.JSONObject

class RedditPost(
    val postData: JSONObject
) {
    val url: String
        get() = postData.getString("url")

    val title: String
        get() = postData.getString("title")

    val selftext: String
        get() = postData.getString("selftext")

    val subreddit: String
        get() = postData.getString("subreddit_name_prefixed")

    val mediaDashUrl: String
        get() = postData
            .getJSONObject("media")
            .getJSONObject("reddit_video")
            .getString("dash_url")

    override fun toString(): String {
        return postData.toString()
    }
}