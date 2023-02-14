package de.deutschebahn.bahnhoflive.util

import kotlinx.coroutines.*

class CdeTimer {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private var timerJob: Job? = null

    private var intervalMilliSeconds: Long = 20000L
    private var startDelayMilliSeconds: Long = 0L
    private var mainThreadAction: (() -> Unit)?=null
    private var backgroundThreadAction: (() -> Unit)? = null

    private fun startCoroutineTimer(
        action: () -> Unit
    ) = scope.launch(
        Dispatchers.IO
    ) {
        delay(startDelayMilliSeconds)
        if (intervalMilliSeconds > 0) {
            while (true) {
                action()
                delay(intervalMilliSeconds)
            }
        } else {
            action()
        }
    }

    private fun createTimerJob() : Job {

        return startCoroutineTimer() {
            backgroundThreadAction?.let { it() }

            scope.launch(Dispatchers.Main) {
                mainThreadAction?.let { it() }
            }
        }

    }


    fun startTimer(
        mainThreadAction: (() -> Unit)?,
        intervalMilliSeconds: Long = 20000L,
        startDelayMilliSeconds: Long = 0L,
        backgroundThreadAction: (() -> Unit)? = null
    ) {

        if (timerJob != null)
            cancelTimer()

        this.mainThreadAction       = mainThreadAction
        this.intervalMilliSeconds   = intervalMilliSeconds
        this.startDelayMilliSeconds = startDelayMilliSeconds
        this.backgroundThreadAction = backgroundThreadAction

        timerJob = createTimerJob()
        timerJob?.start()

    }

    fun restartTimer() {
        cancelTimer()

        timerJob = createTimerJob()
        timerJob?.start()
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun setInterval(intervalMs:Long) {
        if(intervalMs>=500L && intervalMs<10000L)
            intervalMilliSeconds=intervalMs
    }

}