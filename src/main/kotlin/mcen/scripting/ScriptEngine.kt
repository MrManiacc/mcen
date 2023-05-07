package mcen.scripting

import mcen.api.workspace.CustomRequire
import mcen.api.workspace.Workspace
import mcen.content.internal.Registry
import mcen.registry.net.ConsoleErrorPacket
import mcen.scripting.block.CheckBlockFunc
import mcen.scripting.block.GetBlockFunc
import mcen.scripting.console.ConsoleFunc
import mcen.scripting.console.LogLevel
import mcen.scripting.energy.GetEnergyFunc
import mcen.scripting.energy.MoveEnergyFunc
import mcen.scripting.energy.GetOwnEnergyFunc
import mcen.scripting.items.*
import mcen.scripting.position.OffsetPositionFunc
import mcen.scripting.position.SelfPositionFunc
import mcen.scripting.redstone.GetRedstoneFunc
import mcen.scripting.redstone.SetRedstoneFunc
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform

class ScriptEngine() {
    private var updateFunction: LuaValue? = null
    private var tick = 0

    /**
     * Keeps track of our functions defined under the Items table.
     */
    class Items(world: Level, blockPos: BlockPos) : LuaTable() {
        init {
            set("of", GetItemsFunc(world))
            set("is", CheckItemFunc())
            set("move", MoveItemsFunc(world))
            set("movef", MoveItemsFilteredFunc(world))
            set("self", GetOwnItemsFunc(world, blockPos))
        }
    }


    /**
     * Keeps track of our functions defined under the Items table.
     */
    class Blocks(world: Level, blockPos: BlockPos) : LuaTable() {
        init {
            set("of", GetBlockFunc(world))
            set("is", CheckBlockFunc())
        }
    }

    /**
     * Keeps track of our functions defined under the Entity table.
     */
    class Entities(level: Level) : LuaTable() {
        init {
            set("items", mcen.scripting.entities.GetItemsFunc(level))
        }
    }

    /**
     * This allows us to get a redstone value from a given position as well as set a redstone value.
     */
    class Redstone(world: Level) : LuaTable() {
        init {
            set("get", GetRedstoneFunc(world))
            set("set", SetRedstoneFunc(world))
        }
    }

    /**
     * Allows messages to be sent to the client console at runtime. Console messages are logged so you can see messages
     * that are sent while the user interface is not visible.
     */
    class Console(level: Level, pos: BlockPos) : LuaTable() {

        init {
            set("info", ConsoleFunc(pos, level, LogLevel.Info))
            set("debug", ConsoleFunc(pos, level, LogLevel.Debug))
            set("warn", ConsoleFunc(pos, level, LogLevel.Warn))
            set("error", ConsoleFunc(pos, level, LogLevel.Error))
        }
    }

    /**
     * This allows us to get an [IEnergyStorage] from a given position as well as move energy to another [IEnergyStorage].
     * You can retrieve your own energy storage from this self function.
     */
    class Energy(world: Level, blockPos: BlockPos) : LuaTable() {

        init {
            set("of", GetEnergyFunc(world))
            set("self", GetOwnEnergyFunc(world, blockPos))
            set("move", MoveEnergyFunc(world))
        }
    }

    class Position(world: Level, blockPos: BlockPos) : LuaTable() {

        init {
            set("self", SelfPositionFunc(blockPos))
            set("offset", OffsetPositionFunc(blockPos))
        }

    }

    /**
     * Compiles the script program for the given source and provides the default global environment.
     * This function will save the instance of the update function if it exists
     */
    fun compile(blockPos: BlockPos, world: Level, script: String, workspace: Workspace): Boolean {
        updateFunction = null
        val globals = JsePlatform.standardGlobals()
        globals.set("Items", Items(world, blockPos))
        globals.set("Blocks", Blocks(world, blockPos))
        globals.set("Energy", Energy(world, blockPos))
        globals.set("Redstone", Redstone(world))
        globals.set("Console", Console(world, blockPos))
        globals.set("Entity", Entities(world))
        globals.set("Position", Position(world, blockPos))
        globals.set("require", CustomRequire(workspace, globals))
        val chunk = globals.load(script)
        return try {
            chunk.call()
            updateFunction = globals.get("update")
            true
        } catch (luaError: LuaError) {
            handleRuntimeErrors(luaError, blockPos, world)
            false
        }
    }


    /**
     * Executes the update function with an optional tick value
     */
    fun update(blockPos: BlockPos, level: Level): Boolean {
        return try {
            if (updateFunction != null && updateFunction?.isnil() == false) updateFunction?.call(LuaValue.valueOf(tick))
            if (tick++ >= 20) tick = 0
            true
        } catch (luaError: LuaError) {
            handleRuntimeErrors(luaError, blockPos, level)
            false
        }
    }

    /**
     * This will send a runtime error to any entity that has the block loaded to be displayed within the editor.
     */
    private fun handleRuntimeErrors(luaError: LuaError, blockPos: BlockPos, level: Level) {
        val lines = luaError.message?.split("\n") ?: error("unknown lua error: ${luaError.message}")
        val last = lines.last().trim()
        if (last.startsWith(":")) {
            val lineNum = last.substringAfter(":").substringBefore(" ").trim().toIntOrNull() ?: 0
            val message = last.substringAfter(" ")
            Registry.Net.sendToClientsWithBlockLoaded(
                ConsoleErrorPacket(message, blockPos, level.dimension(), lineNum),
                blockPos,
                level
            )
        }
    }


}