package com.justsoft.redditshareinterceptor.util

class Stopwatch {

    private var startTime: Long = 0

    private var stopTime: Long = -1

    var timerRunning: Boolean = false
        private set

    private fun currTime(): Long = System.currentTimeMillis()

    fun start() {
        reset()

        startTime = currTime()
        timerRunning = true
    }

    fun stopAndGetTimeElapsed(): Long {
        stop()
        return timeElapsed()
    }

    fun stop() {
        stopTime = currTime()
        timerRunning = false
    }

    fun restartAndGetTimeElapsed(): Long {
        val timeElapsed = timeElapsed()
        start()
        return timeElapsed
    }

    fun reset() {
        stopTime = -1

        timerRunning = false
    }

    fun timeElapsed(): Long {
        return if (timerRunning)
            currTime() - startTime
        else
            stopTime - startTime
    }
}