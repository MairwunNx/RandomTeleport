package com.mairwunnx.randomteleport

import com.mairwunnx.randomteleport.commands.BadLocationCommand
import com.mairwunnx.randomteleport.commands.RandomTeleportCommand
import com.mairwunnx.randomteleport.managers.ConfigurationManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.server.ServerStartCallback
import net.fabricmc.fabric.api.event.server.ServerStopCallback
import net.minecraft.server.network.ServerPlayerEntity
import org.apache.logging.log4j.LogManager

object EntryPoint : ModInitializer {
    private val logger = LogManager.getLogger()

    init {
        logger.info("Random Teleport mod initializing").also { ConfigurationManager.load() }
    }

    override fun onInitialize() =
        ServerStartCallback.EVENT.register(ServerStartCallback {
            logger.info("Commands registering starting for Random Teleport")
            RandomTeleportCommand.register(it.commandManager.dispatcher)
            BadLocationCommand.register(it.commandManager.dispatcher)
        }).also {
            ServerStopCallback.EVENT.register(ServerStopCallback { ConfigurationManager.save() })
        }

    fun hasPermission(
        player: ServerPlayerEntity, opLevel: Int
    ) = player.server.opPermissionLevel >= opLevel
}
