package com.mairwunnx.randomteleport.managers

import com.google.common.collect.HashBasedTable
import com.mairwunnx.randomteleport.Position
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

object TeleportRollbackManager {
    private val logger = LogManager.getLogger()
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
        removeEntry(playerName)
        lastPosition.put(playerName, position, ZonedDateTime.now())
    }

    fun removeEntry(playerName: String) {
        if (lastPosition.containsRow(playerName)) {
            lastPosition.rowMap().remove(playerName)
        }
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
            logger.debug("Position taken for $playerName: $pos")
            pos
        } catch (ex: NoSuchElementException) {
            null
        }
    }

    /**
     * Removes all expired timers for position
     * rollback.
     */
    @OptIn(ExperimentalTime::class)
    private fun purgeAll() {
        logger.debug("Purging all expired location rollback entries")
        lastPosition.rowMap().keys.removeAll {
            val pos = lastPosition.rowMap().getValue(it).keys.first()
            val commitTime = lastPosition[it, pos]
            val timeNow = ZonedDateTime.now()
            val duration = Duration.between(commitTime, timeNow)
            val passedSeconds = duration.toKotlinDuration().inSeconds

            if (passedSeconds >= getTimeOut()) {
                logger.debug("Expired entry removed for $it")
                return@removeAll true
            }
            return@removeAll false
        }
    }

    private fun getTimeOut(): Int = ConfigurationManager.get().locationRollBackTimer
}
