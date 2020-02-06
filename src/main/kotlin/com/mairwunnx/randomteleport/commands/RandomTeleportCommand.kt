@file:Suppress("DuplicatedCode")

package com.mairwunnx.randomteleport.commands

import com.mairwunnx.randomteleport.EntryPoint
import com.mairwunnx.randomteleport.Position
import com.mairwunnx.randomteleport.configuration.TeleportStrategy
import com.mairwunnx.randomteleport.managers.ConfigurationManager
import com.mairwunnx.randomteleport.managers.TeleportRollbackManager
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.block.BedrockBlock
import net.minecraft.block.BlockState
import net.minecraft.block.MagmaBlock
import net.minecraft.block.Material
import net.minecraft.command.arguments.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.ClickEvent
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import org.apache.logging.log4j.LogManager
import java.util.*

object RandomTeleportCommand {
    private val logger = LogManager.getLogger()
    private val random = Random()
    private val minRange
        get() = ConfigurationManager.get().minRandomTeleportRadius
    private val aliases = arrayOf(
        "random-teleport", "randomteleport",
        "random-tp", "randomtp",
        "rnd-tp", "rndtp",
        "rtp", "tpr"
    )

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val literal = CommandManager.literal("random-teleport").then(
            CommandManager.argument("player", EntityArgumentType.player()).executes(::execute)
        ).then(
            CommandManager.argument("players", EntityArgumentType.players()).executes(::execute)
        )

        val literalNode = dispatcher.register(literal.executes(::execute))
        aliases.forEach {
            if (it != "random-teleport") {
                dispatcher.register(
                    CommandManager.literal(it).executes(::execute).redirect(literalNode)
                )
            }
        }
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val isPlayer = context.source.entity is ServerPlayerEntity
        val player = context.source.player
        val playerName = context.source.player.name.string
        val target by lazy {
            EntityArgumentType.getPlayer(context, "player")
        }
        val targets by lazy {
            EntityArgumentType.getPlayers(context, "players")
        }

        if (isPlayer) {
            if (EntryPoint.hasPermission(player, 1)) {
                if (targetExist(context)) {
                    if (EntryPoint.hasPermission(player, 3)) {
                        teleportRandomly(target, true, playerName)
                        return 0
                    } else {
                        context.source.sendFeedback(
                            TranslatableText(
                                "random_teleport.teleport_other.restricted"
                            ), false
                        )
                        return 0
                    }
                }

                if (targetsExist(context)) {
                    if (EntryPoint.hasPermission(player, 4)) {
                        targets.forEach {
                            teleportRandomly(it, true, playerName)
                        }
                        return 0
                    } else {
                        context.source.sendFeedback(
                            TranslatableText(
                                "random_teleport.teleport_other_multiple.restricted"
                            ), false
                        )
                        return 0
                    }
                }

                teleportRandomly(player)
                return 0
            } else {
                context.source.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.restricted"
                    ), false
                )
                return 0
            }
        } else {
            if (targetExist(context)) {
                teleportRandomly(target, true, "server")
                return 0
            }

            if (targetsExist(context)) {
                targets.forEach {
                    teleportRandomly(it, true, "server")
                }
                return 0
            }

            logger.info("Only player can execute `/random-teleport` command without argument.")
            return 0
        }
    }

    private fun targetExist(context: CommandContext<ServerCommandSource>): Boolean = try {
        EntityArgumentType.getPlayer(context, "player")
        true
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun targetsExist(context: CommandContext<ServerCommandSource>): Boolean = try {
        EntityArgumentType.getPlayers(context, "players")
        true
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun teleportRandomly(
        player: ServerPlayerEntity,
        byOther: Boolean = false,
        otherName: String = ""
    ) {
        val position = Position(player.blockPos.x, player.blockPos.y, player.blockPos.z)
        val world = player.serverWorld
        var newPosition: Position? = null
        var locationFound = false
        val justInCaseComponent = TranslatableText(
            "random_teleport.teleport.just_in_case"
        )
        justInCaseComponent.style.clickEvent = ClickEvent(
            ClickEvent.Action.RUN_COMMAND,
            "/bad-location"
        )

        if (byOther) {
            player.commandSource.sendFeedback(
                TranslatableText(
                    "random_teleport.teleport.teleporting_by_other",
                    otherName
                ), false
            )
        } else {
            player.commandSource.sendFeedback(
                TranslatableText(
                    "random_teleport.teleport.teleporting"
                ), false
            )
        }

        repeat(ConfigurationManager.get().defaultAttempts) {
            val anewPosition = getRandomPosition(
                position, ConfigurationManager.get().defaultRadius
            )
            val pair = isSafeLocation(world, anewPosition)
            if (pair.first) {
                locationFound = true
                newPosition = Position(anewPosition.x, pair.second, anewPosition.z)
                return@repeat
            }
        }

        if (newPosition != null && locationFound) {
            TeleportRollbackManager.commitPosition(player.name.string, position)

            when (ConfigurationManager.get().teleportStrategy) {
                TeleportStrategy.USUALLY_TELEPORT -> {
                    player.teleport(
                        newPosition!!.x + getCenterPosBlock(),
                        newPosition!!.y + getCenterPosBlock(),
                        newPosition!!.z + getCenterPosBlock()
                    )
                }
                TeleportStrategy.SET_AND_UPDATE -> {
                    player.setPositionAnglesAndUpdate(
                        newPosition!!.x + getCenterPosBlock(),
                        newPosition!!.y + getCenterPosBlock(),
                        newPosition!!.z + getCenterPosBlock(),
                        player.yaw,
                        player.pitch
                    )
                }
                TeleportStrategy.SET_POSITION -> {
                    player.setPosition(
                        newPosition!!.x + getCenterPosBlock(),
                        newPosition!!.y + getCenterPosBlock(),
                        newPosition!!.z + getCenterPosBlock()
                    )
                }
            }

            if (byOther) {
                player.commandSource.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.success_by_other",
                        otherName
                    ), false
                )
            } else {
                player.commandSource.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.success"
                    ), false
                )
            }

            player.commandSource.sendFeedback(
                justInCaseComponent, false
            )
        } else {
            TeleportRollbackManager.removeEntry(player.name.string)
            if (byOther) {
                player.commandSource.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.failed_by_other",
                        otherName
                    ), false
                )
            } else {
                player.commandSource.sendFeedback(
                    TranslatableText(
                        "random_teleport.teleport.failed"
                    ), false
                )
            }
        }
    }

    private fun getCenterPosBlock(): Double =
        if (ConfigurationManager.get().teleportOnCenterBlock) 0.5 else 0.0

    private fun getRandomPosition(position: Position, radius: Int): Position {
        val randomForX = random.nextInt(radius).let {
            return@let if (it < minRange) minRange + it else it
        }
        val randomForZ = random.nextInt(radius).let {
            return@let if (it < minRange) minRange + it else it
        }

        val randomX = if (randomForX % 2 == 0) {
            position.x + randomForX
        } else {
            position.x - randomForX
        }
        val randomZ = if (randomForZ % 2 == 0) {
            position.z + randomForZ
        } else {
            position.z - randomForZ
        }

        return Position(randomX, 256, randomZ)
    }

    private fun isSafeLocation(world: ServerWorld, position: Position): Pair<Boolean, Int> {
        if (!world.dimension.isNether) {
            val heightTop = world
                .getChunk(BlockPos(position.x, position.y, position.z))
                .getHeightmap(
                    if (ConfigurationManager.get().canTeleportOnTrees) {
                        Heightmap.Type.MOTION_BLOCKING
                    } else {
                        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES
                    }
                ).get(position.x, position.z)

            val blockPos = BlockPos(position.x, heightTop, position.z)
            val blockState: BlockState = world.getBlockState(blockPos)

            if (!blockState.isAir) {
                val material = blockState.material
                return Pair(!material.isLiquid && material != Material.FIRE, blockPos.y + 1)
            }
            return Pair(false, 0)
        } else {
            var blockPos = BlockPos(position.x, 10, position.z)

            while (blockPos.y < 100) {
                val blockState: BlockState = world.getBlockState(blockPos)
                if (!blockState.isAir &&
                    world.getBlockState(blockPos.up()).isAir &&
                    world.getBlockState(blockPos.up().up()).isAir
                ) {
                    val material = blockState.material
                    return Pair(
                        !material.isLiquid &&
                                material != Material.FIRE &&
                                blockState.block !is MagmaBlock &&
                                blockState.block !is BedrockBlock,
                        blockPos.y + 1
                    )
                }

                blockPos = blockPos.up()
            }
            return Pair(false, 0)
        }
    }
}
