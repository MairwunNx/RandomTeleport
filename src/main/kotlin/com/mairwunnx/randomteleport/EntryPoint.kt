package com.mairwunnx.randomteleport

import com.mairwunnx.projectessentials.permissions.permissions.PermissionsAPI
import com.mairwunnx.randomteleport.commands.RandomTeleportCommand
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import org.apache.logging.log4j.LogManager

@Suppress("unused")
@Mod("random_teleport")
class EntryPoint {
    private val logger = LogManager.getLogger()

    init {
        logger.info("Random Teleport mod initializing")
        MinecraftForge.EVENT_BUS.register(this)
    }

    private fun loadAdditionalModules() {
        try {
            Class.forName(permissionAPIClassPath)
            permissionsInstalled = true
        } catch (_: ClassNotFoundException) {
            // ignored
        }

        try {
            Class.forName(cooldownAPIClassPath)
            cooldownInstalled = true
        } catch (_: ClassNotFoundException) {
            // ignored
        }
    }

    @SubscribeEvent
    fun onServerStarting(it: FMLServerStartingEvent) {
        logger.info("Commands registering starting for Random Teleport.")
        RandomTeleportCommand.register(it.commandDispatcher)
    }

    companion object {
        private const val permissionAPIClassPath =
            "com.mairwunnx.projectessentials.permissions.permissions.PermissionsAPI"
        private const val cooldownAPIClassPath =
            "com.mairwunnx.projectessentials.cooldown.essentials.CooldownAPI"

        private var permissionsInstalled: Boolean = false
        var cooldownInstalled: Boolean = false

        internal fun hasPermission(
            player: ServerPlayerEntity,
            node: String,
            opLevel: Int
        ): Boolean = if (permissionsInstalled) {
            PermissionsAPI.hasPermission(player.name.string, node)
        } else {
            player.server.opPermissionLevel >= opLevel
        }
    }
}
