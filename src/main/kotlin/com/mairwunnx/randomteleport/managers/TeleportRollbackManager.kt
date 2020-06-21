package com.mairwunnx.randomteleport.managers

import com.google.common.collect.HashBasedTable.create
import com.mairwunnx.randomteleport.structs.Position
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

object TeleportRollbackManager {
    private val logger = LogManager.getLogger()
    private val lastPosition = create<String, Position, ZonedDateTime>()

    fun commitPosition(playerName: String, position: Position) =
        logger.debug("Position $position committing for $playerName").also {
            purgeAll().also { removeEntry(playerName) }.run {
                lastPosition.put(playerName, position, now())
            }
        }

    fun removeEntry(playerName: String) = lastPosition.containsRow(playerName).let {
        if (it) lastPosition.rowMap().remove(playerName)
    }

    fun requestPosition(playerName: String): Position? {
        val pos by lazy { lastPosition.rowMap().getValue(playerName).keys.first() }
        logger.debug("Requesting old position for $playerName").also { purgeAll() }
        return try {
            logger.debug("Position taken for $playerName: $pos").let { pos }
        } catch (ex: NoSuchElementException) {
            null
        }
    }

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
