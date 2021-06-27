package com.justsoft.redditshareinterceptor.processors

import java.util.*

fun String.isIReddit(): Boolean =
    this.contains("i.redd.it")

fun String.isIImgur(): Boolean =
    this.contains("i.imgur.com")

fun String.hasGifExtension(): Boolean = this.toLowerCase(Locale.ROOT).trim().let { url ->
    url.endsWith(".gif") || url.endsWith(".gifv")
}

fun String.isGallery(): Boolean =
    this.contains("reddit.com/gallery")

fun String.isVReddit(): Boolean =
    this.contains("v.redd.it")