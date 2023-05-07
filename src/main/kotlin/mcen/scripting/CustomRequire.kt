package mcen.api.workspace

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction

class CustomRequire(private val workspace: Workspace, private val globals: Globals) : OneArgFunction() {
    override fun call(moduleName: LuaValue): LuaValue {
        val modulePath = moduleName.checkjstring().replace('.', '/')
        val file = workspace.readFile(modulePath)

        if (file != File.InvalidFile) {
            val chunk = globals.load(file.sourceCode, file.name)
            return chunk.call()
        }

        return error("Module not found: $modulePath")
    }
}