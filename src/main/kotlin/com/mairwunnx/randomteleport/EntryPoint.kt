package com.mairwunnx.randomteleport

import com.mairwunnx.randomteleport.commands.BadLocationCommand
import com.mairwunnx.randomteleport.commands.RandomTeleportCommand
import com.mairwunnx.randomteleport.managers.ConfigurationManager
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent
import org.apache.logging.log4j.LogManager

@Suppress("unused")
@Mod("random_teleport")
class EntryPoint {
    private val logger = LogManager.getLogger()

    init {
        logger.info("Random Teleport mod initializing")
        ConfigurationManager.load()
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onServerStarting(it: FMLServerStartingEvent) {
        logger.info("Commands registering starting for Random Teleport")
        RandomTeleportCommand.register(it.commandDispatcher)
        BadLocationCommand.register(it.commandDispatcher)
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onServerStopping(it: FMLServerStoppingEvent) {
        ConfigurationManager.save()
    }

    companion object {
        internal fun hasPermission(
            player: ServerPlayerEntity,
            opLevel: Int
        ): Boolean = player.server.opPermissionLevel >= opLevel
    }
}
