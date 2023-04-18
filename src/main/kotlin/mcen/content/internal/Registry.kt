package mcen.content.internal

import mcen.content.ControllerScreen
import mcen.content.ControllerBlock
import mcen.content.ControllerTile
import mcen.gui.RenderScreen
import mcen.registry.*
import mcen.registry.Registry
import mcen.registry.net.*
import mcen.runOnServer
import mcen.scripting.console.LogLevel
import mcen.tile
import net.minecraft.client.Minecraft
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.forge.runWhenOn
import java.util.*

object Registry : ListenerRegistry() {
    override fun register(modId: String, modBus: IEventBus, forgeBus: IEventBus) {
        super.register(modId, modBus, forgeBus)
        runWhenOn(Dist.CLIENT) {
            Minecraft.getInstance().tell {
                RenderScreen.Renderer.initialize()
            }
        }
    }

    /**
     * Handles our network registrations
     */
    object Net : NetworkRegistry() {
        val OpenMenu by register(0) { OpenInventory() }
        val CompileSource by register(1) { CompileSource() }
        val ConsoleMessage by register(2) { ConsoleMessagePacket() }
        val ConsoleError by register(3) { ConsoleErrorPacket() }
    }

    /**
     * ========================Tiles registry========================
     */
    object Tiles : Registry<BlockEntityType<*>>(ForgeRegistries.BLOCK_ENTITY_TYPES) {
        val Controller by register("controller") { tile(Blocks.Controller) { ControllerTile(it.first, it.second) } }
    }

    /**
     * ========================Items registry========================
     */
    object Items : Registry<Item>(ForgeRegistries.ITEMS) {
        //        val Tab: CreativeModeTab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 10).title(Component.literal("Mcen")).build()
        val Controller: Item by register("controller") { BlockItem(Blocks.Controller, Item.Properties().fireResistant().stacksTo(1)) }
    }

    object Blocks : Registry<Block>(ForgeRegistries.BLOCKS) {
        val Controller by register("controller") { ControllerBlock(BlockBehaviour.Properties.of(Material.STONE)) }
    }


    object Scripts : ListenerRegistry() {
        @Sub
        fun onLoadComplete(event: ServerStartedEvent) {
            runOnServer {
                Net.CompileSource.serverListener { compileSource, _ ->
                    val level = ServerLifecycleHooks.getCurrentServer().getLevel(compileSource.level) ?: return@serverListener false
                    val entity = level.getBlockEntity(compileSource.blockPos)
                    return@serverListener if (entity is ControllerTile) {
                        entity.compileSource(compileSource.source)
                        true
                    } else false
                }
            }
        }
    }


    object Console : ListenerRegistry() {
        private val messages: MutableMap<WorldPos, MutableList<Pair<LogLevel, String>>> = hashMapOf()
        private val errors: MutableMap<WorldPos, MutableMap<Int, String>> = hashMapOf()
        private val callbacks: MutableMap<WorldPos, () -> Unit> = hashMapOf()

        @Sub(Side.Client)
        fun onLoadComplete(event: FMLLoadCompleteEvent) {
            Net.ConsoleMessage.clientListener { packet, _ ->
                val worldPos = WorldPos(packet.blockPos, packet.world)
                val messagesMap = messages.getOrPut(worldPos) { arrayListOf() }
                messagesMap.add(Pair(packet.logLevel, packet.message))
                if (callbacks.containsKey(worldPos)) callbacks[worldPos]!!.invoke()

                true
            }

            Net.ConsoleError.clientListener { packet, _ ->
                val worldPos = WorldPos(packet.blockPos, packet.world)
                val messagesMap = errors.getOrPut(worldPos) { hashMapOf() }
                messagesMap[packet.line] = packet.message
                if (callbacks.containsKey(worldPos)) callbacks[worldPos]!!.invoke()
                true
            }
        }

        fun addCallback(worldPos: WorldPos, onMessage: () -> Unit) {
            callbacks[worldPos] = onMessage
        }

        fun removeCallback(worldPos: WorldPos) {
            callbacks.remove(worldPos)
        }

        /**
         * Gets the messages for the given world position
         */
        fun getMessages(worldPos: WorldPos): MutableList<Pair<LogLevel, String>> = messages.getOrPut(worldPos) { arrayListOf() }
        fun getErrors(worldPos: WorldPos): MutableMap<Int, String> = errors.getOrPut(worldPos) { hashMapOf() }

    }
}