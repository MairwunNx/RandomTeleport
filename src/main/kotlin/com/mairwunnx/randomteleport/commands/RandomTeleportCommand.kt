package com.mairwunnx.randomteleport.commands

import com.mairwunnx.projectessentials.cooldown.essentials.CommandsAliases
import com.mairwunnx.randomteleport.EntryPoint
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import org.apache.logging.log4j.LogManager

object RandomTeleportCommand {
    private val logger = LogManager.getLogger()
    private val aliases = arrayOf(
        "random-teleport", "randomteleport",
        "random-tp", "randomtp",
        "rnd-tp", "rndtp",
        "rtp", "tpr"
    )

    fun register(dispatcher: CommandDispatcher<CommandSource>) {
        registerAliases()

        val literal =
            literal<CommandSource>("random-teleport").then(
                Commands.argument(
                    "warp name", StringArgumentType.string()
                ).executes {
                    return@executes execute(it)
                }
            )

        val literalNode = dispatcher.register(literal)
        dispatcher.register(Commands.literal("randomteleport").redirect(literalNode))
        dispatcher.register(Commands.literal("random-tp").redirect(literalNode))
        dispatcher.register(Commands.literal("randomtp").redirect(literalNode))
        dispatcher.register(Commands.literal("rnd-tp").redirect(literalNode))
        dispatcher.register(Commands.literal("rndtp").redirect(literalNode))
        dispatcher.register(Commands.literal("rtp").redirect(literalNode))
        dispatcher.register(Commands.literal("tpr").redirect(literalNode))
    }

    private fun registerAliases() {
        if (!EntryPoint.cooldownInstalled) return
        CommandsAliases.aliases["random-teleport"] = aliases.toMutableList()
    }

    fun execute(context: CommandContext<CommandSource>): Int {
        return 0
    }
}
