package com.justsoft.redditshareinterceptor.utils

import org.mp4parser.muxer.FileRandomAccessSourceImpl
import org.mp4parser.muxer.Movie
import org.mp4parser.muxer.builder.DefaultMp4Builder
import org.mp4parser.muxer.container.mp4.MovieCreator
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile

@JvmName("MovieCreator")
private fun build(file: File): Movie {
    return FileInputStream(file).use {
        MovieCreator.build(
            it.channel,
            FileRandomAccessSourceImpl(RandomAccessFile(file, "r")),
            file.name
        )
    }
}

fun combineVideoAndAudio(
    videoFile: File,
    audioFile: File,
    outputStream: FileOutputStream
) {
    val movie = build(videoFile)
    val audio = build(audioFile)

    val audioTrack = audio.tracks.first()
    movie.addTrack(audioTrack)

    val outContainer = DefaultMp4Builder().build(movie)
    outContainer.writeContainer(outputStream.channel)
}