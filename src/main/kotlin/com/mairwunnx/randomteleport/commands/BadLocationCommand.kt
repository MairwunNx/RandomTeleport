package com.mairwunnx.randomteleport.commands

import com.mairwunnx.randomteleport.EntryPoint
import com.mairwunnx.randomteleport.managers.TeleportRollbackManager
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TranslationTextComponent
import org.apache.logging.log4j.LogManager

object BadLocationCommand {
    private val logger = LogManager.getLogger()

    fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal<CommandSource>("bad-location").executes(::execute))
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val isPlayer = context.source.entity is ServerPlayerEntity
        val player = context.source.asPlayer()
        val playerName = context.source.asPlayer().name.string

        if (isPlayer) {
            if (!EntryPoint.hasPermission(player, "teleport.random.rollback", 0)) {
                context.source.sendFeedback(
                    TranslationTextComponent(
                        "random_teleport.teleport.rollback_restricted"
                    ), false
                )
                return 0
            }

            val position = TeleportRollbackManager.requestPosition(playerName)

            if (position == null) {
                context.source.sendFeedback(
                    TranslationTextComponent(
                        "random_teleport.teleport.expired_or_not_was_teleported"
                    ), false
                )
            } else {
                context.source.sendFeedback(
                    TranslationTextComponent(
                        "random_teleport.teleport.teleporting_back"
                    ), false
                )
                player.teleportKeepLoaded(
                    position.x + 0.5,
                    position.y + 0.5,
                    position.z + 0.5
                )
                TeleportRollbackManager.removeEntry(playerName)
            }

            return 0
        } else {
            logger.info("Unable to use this command. Only player can execute this command.")
            return 0
        }
    }
}