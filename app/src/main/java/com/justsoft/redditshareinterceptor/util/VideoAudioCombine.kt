package com.justsoft.redditshareinterceptor.util

import android.net.Uri
import androidx.core.net.toFile
import org.mp4parser.muxer.builder.DefaultMp4Builder
import org.mp4parser.muxer.container.mp4.MovieCreator
import java.io.FileOutputStream

fun combineVideoAndAudio(
    videoUri: Uri,
    audioUri: Uri,
    outputStream: FileOutputStream
) {
    val videoContainerPath = videoUri.toFile().absolutePath
    val movie = MovieCreator.build(videoContainerPath)

    val audioContainerPath = audioUri.toFile().absolutePath
    val audio = MovieCreator.build(audioContainerPath)

    val audioTrack = audio.tracks.first()
    movie.addTrack(audioTrack)

    val outContainer = DefaultMp4Builder().build(movie)
    outContainer.writeContainer(outputStream.channel)
}