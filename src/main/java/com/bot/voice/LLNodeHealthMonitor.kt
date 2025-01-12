package com.bot.voice

import dev.arbjerg.lavalink.client.LavalinkNode
import dev.arbjerg.lavalink.client.event.EmittedEvent
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent
import dev.arbjerg.lavalink.client.event.TrackStartEvent

class LLNodeHealthMonitor(private val node: LavalinkNode) {
    private val windowSizeInSeconds: Long = 1800
    private val events = ArrayDeque<LLNodeHealthEvent>()  // This will store the events in a circular manner.
    private var attemptCount = 0
    private var failureCount = 0

    fun recordEvent(event: EmittedEvent) {
        val error = when (event) {
            is TrackStartEvent -> {
                false
            }

            is TrackExceptionEvent -> {
                true
            }

            else -> {
                return
            }
        }
        val healthEvent = LLNodeHealthEvent(System.currentTimeMillis(), error)

        cleanOutdatedData()

        // Add the new event
        events.addLast(healthEvent)
        if (healthEvent.error) {
            failureCount++
        } else {
            attemptCount++
        }
    }

    // Function to calculate success/failure ratio
    fun getHealth(): NodeHealth {
        cleanOutdatedData()
        return NodeHealth.getFromErrorRate(failureCount.toDouble() / attemptCount.toDouble(), attemptCount)
    }

    private fun cleanOutdatedData() {
        // Remove outdated events (older than the window)
        while (events.isNotEmpty() && events.first().timestamp <= System.currentTimeMillis() - windowSizeInSeconds * 1000) {
            val expiredEvent = events.removeFirst()
            if (expiredEvent.error) {
                failureCount--
            } else {
                attemptCount--
            }
        }
    }

    internal data class LLNodeHealthEvent(val timestamp: Long, val error: Boolean)
}

enum class NodeHealth(val min: Double, val max: Double, val metricId: Int) {
    HEALTHY(0.0, 0.1, 1),
    DEGRADED(0.1, 0.35, 2),
    UNHEALTHY(0.35, 1.0, 3),
    UNKNOWN(999.9, 999.9, 0);

    companion object {
        fun getFromErrorRate(errorRate: Double, attemptCount: Int): NodeHealth {
            // Give low traffic nodes some benefit of the doubt to get some data on health
            if (attemptCount <= 10) {
                return UNKNOWN
            }
            return when (errorRate) {
                in HEALTHY.min..HEALTHY.max -> HEALTHY
                in DEGRADED.min..DEGRADED.max -> DEGRADED
                in UNHEALTHY.min..UNHEALTHY.max -> UNHEALTHY
                else -> UNKNOWN // If the error rate is outside of valid ranges or invalid
            }
        }
    }
}