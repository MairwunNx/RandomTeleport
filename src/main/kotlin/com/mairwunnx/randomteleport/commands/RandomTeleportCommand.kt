package com.mairwunnx.randomteleport.commands

import com.mairwunnx.projectessentials.cooldown.essentials.CommandsAliases
import com.mairwunnx.randomteleport.EntryPoint
import com.mairwunnx.randomteleport.Position
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
        dispatcher.register(
            Commands.literal("randomteleport").executes(::execute).redirect(literalNode)
        )
        dispatcher.register(
            Commands.literal("random-tp").executes(::execute).redirect(literalNode)
        )
        dispatcher.register(
            Commands.literal("randomtp").executes(::execute).redirect(literalNode)
        )
        dispatcher.register(
            Commands.literal("rnd-tp").executes(::execute).redirect(literalNode)
        )
        dispatcher.register(
            Commands.literal("rndtp").executes(::execute).redirect(literalNode)
        )
        dispatcher.register(
            Commands.literal("rtp").executes(::execute).redirect(literalNode)
        )
        dispatcher.register(
            Commands.literal("tpr").executes(::execute).redirect(literalNode)
        )
    }

    private fun registerAliases() {
        if (!EntryPoint.cooldownInstalled) return
        CommandsAliases.aliases["random-teleport"] = aliases.toMutableList()
    }

    private fun execute(context: CommandContext<CommandSource>): Int {
        teleportRandomly(context.source.asPlayer(), 1)
        return 0
    }

    private fun teleportRandomly(
        player: ServerPlayerEntity, depth: Int
    ) {
        val position = Position(player.position.x, player.position.y, player.position.z)
        val world = player.serverWorld
        var newPosition: Position? = null
        var locationFound = false

        repeat(depth) {
            val anewPosition = getRandomPosition(position)
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
            // send message success teleported
        } else {
            // send message failed to teleport :(
        }
    }

    private fun getRandomPosition(position: Position): Position {
        val randomForX = random.nextInt(10000).let {
            return@let if (it < minRange) minRange + it else it
        }
        val randomForZ = random.nextInt(10000).let {
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
