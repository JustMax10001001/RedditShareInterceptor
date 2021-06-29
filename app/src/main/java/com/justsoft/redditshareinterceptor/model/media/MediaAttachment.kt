package com.justsoft.redditshareinterceptor.model.media

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaAttachment(
    val mediaUri: Uri,
    val mediaContentType: MediaContentType,
    val description: String = "",
) : Parcelable