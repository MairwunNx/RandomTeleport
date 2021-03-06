@file:Suppress("DuplicatedCode")

package com.mairwunnx.randomteleport.commands

import com.mairwunnx.randomteleport.EntryPoint
import com.mairwunnx.randomteleport.configuration.TeleportStrategy.*
import com.mairwunnx.randomteleport.managers.ConfigurationManager.get
import com.mairwunnx.randomteleport.managers.TeleportRollbackManager
import com.mairwunnx.randomteleport.structs.Position
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
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
import net.minecraft.world.BlockView
import org.apache.logging.log4j.LogManager
import java.util.*

object RandomTeleportCommand {
    private val logger = LogManager.getLogger()
    private val random = Random()
    private val minRange get() = get().minRandomTeleportRadius
    private val aliases = arrayOf(
        "random-teleport", "randomteleport", "random-tp",
        "randomtp", "rnd-tp", "rndtp", "rtp", "tpr"
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
        val target by lazy { EntityArgumentType.getPlayer(context, "player") }
        val targets by lazy { EntityArgumentType.getPlayers(context, "players") }

        if (isPlayer) {
            if (EntryPoint.hasPermission(player, get().opLevelForRandomTeleport)) {
                if (targetExist(context)) {
                    if (EntryPoint.hasPermission(player, 3)) {
                        teleportRandomly(target, true, playerName).let { return 0 }
                    } else {
                        context.source.sendFeedback(
                            TranslatableText("random_teleport.teleport_other.restricted"), false
                        ).let { return 0 }
                    }
                }

                if (targetsExist(context)) {
                    if (EntryPoint.hasPermission(player, 4)) {
                        targets.forEach {
                            teleportRandomly(it, true, playerName)
                        }.let { return 0 }
                    } else {
                        context.source.sendFeedback(
                            TranslatableText(
                                "random_teleport.teleport_other_multiple.restricted"
                            ), false
                        ).let { return 0 }
                    }
                }
                teleportRandomly(player).let { return 0 }
            } else {
                context.source.sendFeedback(
                    TranslatableText("random_teleport.teleport.restricted"), false
                ).let { return 0 }
            }
        } else {
            if (targetExist(context)) {
                teleportRandomly(target, true, "server").let { return 0 }
            }
            if (targetsExist(context)) {
                targets.forEach {
                    teleportRandomly(it, true, "server")
                }.let { return 0 }
            }
            logger.info("Only player can execute this command without argument.").let { return 0 }
        }
    }

    private fun targetExist(context: CommandContext<ServerCommandSource>) = try {
        EntityArgumentType.getPlayer(context, "player").let { true }
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun targetsExist(context: CommandContext<ServerCommandSource>) = try {
        EntityArgumentType.getPlayers(context, "players").let { true }
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun teleportRandomly(
        player: ServerPlayerEntity, byOther: Boolean = false, otherName: String = ""
    ) {
        val position = Position(player.blockPos.x, player.blockPos.y, player.blockPos.z)
        val world = player.serverWorld
        var newPosition: Position? = null
        var locationFound = false
        val justInCaseComponent = TranslatableText("random_teleport.teleport.just_in_case")
        justInCaseComponent.style = justInCaseComponent.style.withClickEvent(
            ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bad-location")
        )

        if (byOther) {
            player.commandSource.sendFeedback(
                TranslatableText("random_teleport.teleport.teleporting_by_other", otherName), false
            )
        } else {
            player.commandSource.sendFeedback(
                TranslatableText("random_teleport.teleport.teleporting"), false
            )
        }

        repeat(get().defaultAttempts) {
            val anewPosition = getRandomPosition(position, get().defaultRadius)
            val pair = isSafeLocation(world, anewPosition)
            if (pair.first) {
                newPosition = Position(
                    anewPosition.x, pair.second, anewPosition.z
                ).also { locationFound = true }
                return@repeat
            }
        }

        if (newPosition != null && locationFound) {
            TeleportRollbackManager.commitPosition(player.name.string, position)

            when (get().teleportStrategy) {
                USUALLY_TELEPORT, KEEP_LOADED, ATTEMPT_TELEPORT -> player.teleport(
                    newPosition!!.x + getCenterPosBlock(),
                    newPosition!!.y + getCenterPosBlock(),
                    newPosition!!.z + getCenterPosBlock()
                )
                SET_AND_UPDATE -> player.updatePositionAndAngles(
                    newPosition!!.x + getCenterPosBlock(),
                    newPosition!!.y + getCenterPosBlock(),
                    newPosition!!.z + getCenterPosBlock(),
                    player.yaw, player.pitch
                )
                SET_POSITION -> player.updatePosition(
                    newPosition!!.x + getCenterPosBlock(),
                    newPosition!!.y + getCenterPosBlock(),
                    newPosition!!.z + getCenterPosBlock()
                )
            }

            if (byOther) {
                player.commandSource.sendFeedback(
                    TranslatableText("random_teleport.teleport.success_by_other", otherName), false
                )
            } else {
                player.commandSource.sendFeedback(
                    TranslatableText("random_teleport.teleport.success"), false
                )
            }
            player.commandSource.sendFeedback(justInCaseComponent, false)
        } else {
            TeleportRollbackManager.removeEntry(player.name.string)
            if (byOther) {
                player.commandSource.sendFeedback(
                    TranslatableText("random_teleport.teleport.failed_by_other", otherName), false
                )
            } else {
                player.commandSource.sendFeedback(
                    TranslatableText("random_teleport.teleport.failed"), false
                )
            }
        }
    }

    private fun getCenterPosBlock() = if (get().teleportOnCenterBlock) 0.5 else 0.0

    private fun getRandomPosition(position: Position, radius: Int): Position {
        val randomForX = random.nextInt(radius).let { if (it < minRange) minRange + it else it }
        val randomForZ = random.nextInt(radius).let { if (it < minRange) minRange + it else it }
        val randomX = if (randomForX % 2 == 0) position.x + randomForX else position.x - randomForX
        val randomZ = if (randomForZ % 2 == 0) position.z + randomForZ else position.z - randomForZ
        return Position(randomX, 256, randomZ)
    }

    private fun isSafeLocation(world: ServerWorld, position: Position): Pair<Boolean, Int> {
        if (!world.dimension.isShrunk) {
            val heightTop = getTopY(world, BlockPos(position.x, 257, position.z))
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
                        !material.isLiquid
                                && material != Material.FIRE
                                && blockState.block !is MagmaBlock
                                && !blockState.block.`is`(Blocks.BEDROCK),
                        blockPos.y + 1
                    )
                }
                blockPos = blockPos.up()
            }
            return Pair(false, 0)
        }
    }

    private fun getTopY(blockView: BlockView, pos: BlockPos): Int {
        var blockPos = pos
        do {
            if (blockPos.y <= 0) return 257
            blockPos = blockPos.down()
        } while (
            when {
                blockView.getBlockState(blockPos).isAir -> true
                else -> blockView.getBlockState(blockPos).material == Material.LEAVES && !get().canTeleportOnTrees
            }
        )
        return blockPos.y
    }
}
