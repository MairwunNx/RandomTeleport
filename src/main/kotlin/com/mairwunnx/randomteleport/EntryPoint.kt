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
        logger.info("Random Teleport mod initializing")
        ConfigurationManager.load()
    }

    override fun onInitialize() {
        ServerStartCallback.EVENT.register(ServerStartCallback {
            logger.info("Commands registering starting for Random Teleport")
            RandomTeleportCommand.register(it.commandManager.dispatcher)
            BadLocationCommand.register(it.commandManager.dispatcher)
        })

        ServerStopCallback.EVENT.register(ServerStopCallback {
            ConfigurationManager.save()
        })
    }

    internal fun hasPermission(
        player: ServerPlayerEntity,
        opLevel: Int
    ): Boolean = player.server.opPermissionLevel >= opLevel
}
