package com.mairwunnx.randomteleport.commands

import com.mairwunnx.randomteleport.EntryPoint
import com.mairwunnx.randomteleport.configuration.TeleportStrategy.*
import com.mairwunnx.randomteleport.managers.ConfigurationManager
import com.mairwunnx.randomteleport.managers.TeleportRollbackManager
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import org.apache.logging.log4j.LogManager

object BadLocationCommand {
    private val logger = LogManager.getLogger()

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("bad-location").executes(::execute)
        )
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val isPlayer = context.source.entity is ServerPlayerEntity
        val player = context.source.player
        val playerName = context.source.player.name.string

        if (isPlayer) {
            if (!EntryPoint.hasPermission(player, 1)) {
                context.source.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.rollback_restricted"
                    ), false
                )
                return 0
            }

            val position = TeleportRollbackManager.requestPosition(playerName)

            if (position == null) {
                context.source.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.expired_or_not_was_teleported"
                    ), false
                )
            } else {
                context.source.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.teleporting_back"
                    ), false
                )

                when (ConfigurationManager.get().teleportStrategy) {
                    USUALLY_TELEPORT, KEEP_LOADED, ATTEMPT_TELEPORT -> {
                        player.teleport(
                            position.x + getCenterPosBlock(),
                            position.y + getCenterPosBlock(),
                            position.z + getCenterPosBlock()
                        )
                    }
                    SET_AND_UPDATE -> {
                        player.setPositionAnglesAndUpdate(
                            position.x + getCenterPosBlock(),
                            position.y + getCenterPosBlock(),
                            position.z + getCenterPosBlock(),
                            player.yaw,
                            player.pitch
                        )
                    }
                    SET_POSITION -> {
                        player.setPosition(
                            position.x + getCenterPosBlock(),
                            position.y + getCenterPosBlock(),
                            position.z + getCenterPosBlock()
                        )
                    }
                }

                TeleportRollbackManager.removeEntry(playerName)
            }

            return 0
        } else {
            logger.info("Unable to use this command. Only player can execute this command.")
            return 0
        }
    }

    private fun getCenterPosBlock(): Double =
        if (ConfigurationManager.get().teleportOnCenterBlock) 0.5 else 0.0
}
