package com.mairwunnx.randomteleport.managers

import com.google.common.collect.HashBasedTable
import com.mairwunnx.randomteleport.structs.Position
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

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
        purgeAll().also { removeEntry(playerName) }
        lastPosition.put(playerName, position, now())
    }

    fun removeEntry(playerName: String) {
        if (lastPosition.containsRow(playerName)) lastPosition.rowMap().remove(playerName)
    }

    /**
     * @param playerName player nickname.
     * @return `Position` instance if possible
     * rollback player location, or timer not
     * expired. `Null` if timer expired or
     * not exist last position for player.
     */
    fun requestPosition(playerName: String): Position? {
        val pos by lazy { lastPosition.rowMap().getValue(playerName).keys.first() }
        logger.debug("Requesting old position for $playerName").also { purgeAll() }
        return try {
            logger.debug("Position taken for $playerName: $pos").let { pos }
        } catch (ex: NoSuchElementException) {
            null
        }
    }

    /**
     * Removes all expired timers for position rollback.
     */
    private fun purgeAll() {
        logger.debug("Purging all expired location rollback entries")
        lastPosition.rowMap().keys.removeAll {
            val pos = lastPosition.rowMap().getValue(it).keys.first()
            if (Duration.between(lastPosition[it, pos], now()).seconds >= getTimeOut()) {
                logger.debug("Expired entry removed for $it").let { true }
            }
            return@removeAll false
        }
    }

    private fun getTimeOut() = ConfigurationManager.get().locationRollBackTimer
}
