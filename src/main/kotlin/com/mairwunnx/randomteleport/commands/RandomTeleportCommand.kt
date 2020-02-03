@file:Suppress("DuplicatedCode")

package com.mairwunnx.randomteleport.commands

import com.mairwunnx.projectessentials.cooldown.essentials.CommandsAliases
import com.mairwunnx.randomteleport.EntryPoint
import com.mairwunnx.randomteleport.Position
import com.mairwunnx.randomteleport.TeleportRollbackManager
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.context.CommandContext
import net.minecraft.block.BedrockBlock
import net.minecraft.block.BlockState
import net.minecraft.block.MagmaBlock
import net.minecraft.block.material.Material
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.EntityArgument
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.Tuple
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.world.gen.Heightmap
import net.minecraft.world.server.ServerWorld
import org.apache.logging.log4j.LogManager
import java.util.*

// todo: canSpawnOnTrees implement.
// todo: max must be controlled by configuration.
object RandomTeleportCommand {
    private val logger = LogManager.getLogger()
    private val random = Random()
    private val minRange = 32
    private val aliases = arrayOf(
        "random-teleport", "randomteleport",
        "random-tp", "randomtp",
        "rnd-tp", "rndtp",
        "rtp", "tpr"
    )

    fun register(dispatcher: CommandDispatcher<CommandSource>) {
        registerAliases()

        val literal =
            literal<CommandSource>("random-teleport")
                .then(
                    Commands.argument(
                        "player", EntityArgument.player()
                    ).then(
                        Commands.argument(
                            "radius", IntegerArgumentType.integer(10, 10_000)
                        ).executes(::execute).then(
                            Commands.argument(
                                "depth", IntegerArgumentType.integer(1, 10)
                            ).executes(::execute)
                        )
                    ).executes(::execute)
                ).then(
                    Commands.argument(
                        "players", EntityArgument.players()
                    ).then(
                        Commands.argument(
                            "radius", IntegerArgumentType.integer(10, 10_000)
                        ).executes(::execute).then(
                            Commands.argument(
                                "depth", IntegerArgumentType.integer(1, 10)
                            ).executes(::execute)
                        )
                    ).executes(::execute)
                ).then(
                    Commands.argument(
                        "radius", IntegerArgumentType.integer(10, 10_000)
                    ).executes(::execute).then(
                        Commands.argument(
                            "depth", IntegerArgumentType.integer(1, 10)
                        ).executes(::execute)
                    )
                )

        val literalNode = dispatcher.register(literal.executes(::execute))
        aliases.forEach {
            if (it != "random-teleport") {
                dispatcher.register(
                    Commands.literal(it).executes(::execute).redirect(literalNode)
                )
            }
        }
    }

    private fun registerAliases() {
        if (!EntryPoint.cooldownInstalled) return
        CommandsAliases.aliases["random-teleport"] = aliases.toMutableList()
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        val isPlayer = context.source.entity is ServerPlayerEntity
        val player = context.source.asPlayer()
        val playerName = context.source.asPlayer().name.string
        val target by lazy {
            EntityArgument.getPlayer(context, "player")
        }
        val targets by lazy {
            EntityArgument.getPlayers(context, "players")
        }
        val radius by lazy {
            IntegerArgumentType.getInteger(context, "radius")
        }
        val depth by lazy {
            IntegerArgumentType.getInteger(context, "depth")
        }

        if (isPlayer) {
            if (EntryPoint.hasPermission(player, "teleport.random", 1)) {
                if (targetExist(context)) {
                    if (EntryPoint.hasPermission(player, "teleport.random.other", 3)) {
                        teleportRandomly(
                            target,
                            if (depthExist(context)) depth else 1,
                            if (radiusExist(context)) radius else 10000,
                            true,
                            playerName
                        )
                        return 0
                    } else {
                        context.source.sendFeedback(
                            TranslationTextComponent(
                                "random_teleport.teleport_other.restricted"
                            ), false
                        )
                        return 0
                    }
                }

                if (targetsExist(context)) {
                    if (EntryPoint.hasPermission(
                            player, "teleport.random.other.multiple", 4
                        )
                    ) {
                        targets.forEach {
                            teleportRandomly(
                                it,
                                if (depthExist(context)) depth else 1,
                                if (radiusExist(context)) radius else 10000,
                                true,
                                playerName
                            )
                        }
                        return 0
                    } else {
                        context.source.sendFeedback(
                            TranslationTextComponent(
                                "random_teleport.teleport_other_multiple.restricted"
                            ), false
                        )
                        return 0
                    }
                }

                teleportRandomly(
                    player,
                    if (depthExist(context)) depth else 1,
                    if (radiusExist(context)) radius else 10000
                )
                return 0
            } else {
                context.source.sendFeedback(
                    TranslationTextComponent(
                        "random_teleport.teleport.restricted"
                    ), false
                )
                return 0
            }
        } else {
            if (targetExist(context)) {
                teleportRandomly(
                    target,
                    if (depthExist(context)) depth else 1,
                    if (radiusExist(context)) radius else 10000,
                    true, "server"
                )
                return 0
            }

            if (targetsExist(context)) {
                targets.forEach {
                    teleportRandomly(
                        it,
                        if (depthExist(context)) depth else 1,
                        if (radiusExist(context)) radius else 10000,
                        true, "server"
                    )
                }
                return 0
            }

            logger.info("Only player can execute `/random-teleport` command without argument.")
            return 0
        }
    }

    private fun targetExist(context: CommandContext<CommandSource>): Boolean = try {
        EntityArgument.getPlayer(context, "player")
        true
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun targetsExist(context: CommandContext<CommandSource>): Boolean = try {
        EntityArgument.getPlayers(context, "players")
        true
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun radiusExist(context: CommandContext<CommandSource>): Boolean = try {
        IntegerArgumentType.getInteger(context, "radius")
        true
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun depthExist(context: CommandContext<CommandSource>): Boolean = try {
        IntegerArgumentType.getInteger(context, "depth")
        true
    } catch (ex: IllegalArgumentException) {
        false
    }

    private fun teleportRandomly(
        player: ServerPlayerEntity,
        depth: Int,
        radius: Int,
        byOther: Boolean = false,
        otherName: String = ""
    ) {
        val position = Position(player.position.x, player.position.y, player.position.z)
        val world = player.serverWorld
        var newPosition: Position? = null
        var locationFound = false
        val justInCaseComponent = TranslationTextComponent(
            "random_teleport.teleport.just_in_case"
        )
        justInCaseComponent.style.clickEvent = ClickEvent(
            ClickEvent.Action.RUN_COMMAND,
            "/bad-location"
        )

        TeleportRollbackManager.commitPosition(player.name.string, position)

        if (byOther) {
            player.commandSource.sendFeedback(
                TranslationTextComponent(
                    "random_teleport.teleport.teleporting_by_other",
                    otherName
                ), false
            )
        } else {
            player.commandSource.sendFeedback(
                TranslationTextComponent(
                    "random_teleport.teleport.teleporting"
                ), false
            )
        }

        repeat(depth) {
            val anewPosition = getRandomPosition(position, radius)
            val tuple = isSafeLocation(world, anewPosition)
            if (tuple.a) {
                locationFound = true
                newPosition = Position(anewPosition.x, tuple.b, anewPosition.z)
                return@repeat
            }
        }

        if (newPosition != null && locationFound) {
            player.teleportKeepLoaded(
                newPosition!!.x.toDouble(),
                newPosition!!.y.toDouble(),
                newPosition!!.z.toDouble()
            )
            if (byOther) {
                player.commandSource.sendFeedback(
                    TranslationTextComponent(
                        "random_teleport.teleport.success_by_other",
                        otherName
                    ), false
                )
            } else {
                player.commandSource.sendFeedback(
                    TranslationTextComponent(
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
                    TranslationTextComponent(
                        "random_teleport.teleport.failed_by_other",
                        otherName
                    ), false
                )
            } else {
                player.commandSource.sendFeedback(
                    TranslationTextComponent(
                        "random_teleport.teleport.failed"
                    ), false
                )
            }
        }
    }

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

    private fun isSafeLocation(world: ServerWorld, position: Position): Tuple<Boolean, Int> {
        if (!world.dimension.isNether) {
            val heightTop = world
                .getChunkAt(BlockPos(position.x, position.y, position.z))
                .getTopBlockY(
                    Heightmap.Type.MOTION_BLOCKING,
                    position.x,
                    position.z
                )

            val blockPos = BlockPos(position.x, heightTop, position.z)
            val blockState: BlockState = world.getBlockState(blockPos)

            if (!blockState.isAir(world, blockPos)) {
                val material = blockState.material
                return Tuple(!material.isLiquid && material != Material.FIRE, blockPos.y + 1)
            }
            return Tuple(false, 0)
        } else {
            var blockPos = BlockPos(position.x, 10, position.z)

            while (blockPos.y < 100) {
                val blockState: BlockState = world.getBlockState(blockPos)
                if (!blockState.isAir(world, blockPos) &&
                    world.getBlockState(blockPos.up()).isAir(world, blockPos.up()) &&
                    world.getBlockState(blockPos.up().up()).isAir(world, blockPos.up().up())
                ) {
                    val material = blockState.material
                    return Tuple(
                        !material.isLiquid &&
                                material != Material.FIRE &&
                                blockState.block !is MagmaBlock &&
                                blockState.block !is BedrockBlock,
                        blockPos.y + 1
                    )
                }

                blockPos = blockPos.up()
            }
            return Tuple(false, 0)
        }
    }
}
