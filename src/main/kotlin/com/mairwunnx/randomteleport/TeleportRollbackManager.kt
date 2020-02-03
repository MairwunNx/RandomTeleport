package com.mairwunnx.randomteleport

import com.google.common.collect.HashBasedTable
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

object TeleportRollbackManager {
    private val logger = LogManager.getLogger()
    private const val DEFAULT_TIMER = 10 // Seconds.
    private val lastPosition =
        HashBasedTable.create<String, Position, ZonedDateTime>()

    /**
     * @param playerName player nickname.
     * @param position last player position
     * (before teleporting to random location).
     */
    fun commitPosition(playerName: String, position: Position) {
        logger.debug("Position $position committing for $playerName")
        purgeAll()
        lastPosition.put(playerName, position, ZonedDateTime.now())
    }

    /**
     * @param playerName player nickname.
     * @return `Position` instance if possible
     * rollback player location, or timer not
     * expired. `Null` if timer expired or
     * not exist last position for player.
     */
    fun requestPosition(playerName: String): Position? {
        val pos by lazy {
            lastPosition.rowMap().getValue(playerName).keys.first()
        }

        logger.debug("Requesting old position for $playerName")
        purgeAll()

        return try {
            pos
        } catch (ex: NoSuchElementException) {
            null
        }
    }

    /**
     * Removes all expired timers for position
     * rollback.
     */
    @UseExperimental(ExperimentalTime::class)
    private fun purgeAll() {
        logger.debug("Purging all expired location rollback entries")
        lastPosition.rowMap().keys.removeAll {
            val pos = lastPosition.rowMap().getValue(it).keys.first()
            val commitTime = lastPosition[it, pos]
            val timeNow = ZonedDateTime.now()
            val duration = Duration.between(commitTime, timeNow)
            val passedSeconds = duration.toKotlinDuration().inSeconds

            passedSeconds >= getTimeOut()
        }
    }

    // todo: compatibility with configuration
    private fun getTimeOut(): Int {
        return DEFAULT_TIMER
    }
}
