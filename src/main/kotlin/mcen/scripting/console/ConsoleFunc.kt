package mcen.scripting.console

import mcen.content.internal.Registry
import mcen.registry.net.ConsoleMessagePacket
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ConsoleFunc(private val blockPos: BlockPos, private val world: Level, private val level: LogLevel) : VarArgFunction() {
    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("hh:mm:ss")
    override fun invoke(args: Varargs?): Varargs {
        if (args == null || args.narg() == 0) return NIL
        if (args.narg() == 1) {
            val message = "${simpleDateFormat.format(Date(System.currentTimeMillis()))} - ${args.arg1()}"
            Registry.Net.sendToClientsWithBlockLoaded(ConsoleMessagePacket(message, blockPos, world.dimension(), level), blockPos, world)
            return NIL
        }
        var message = args.arg1().toString()
        var pattern = Pattern.compile("\\{(\\d+)}")
        var matcher = pattern.matcher(message)
        val arguments = args.subargs(2)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val targetIndex = matcher.group(1).toInt()// Adjust index to account for the base string argument
            val replacement = arguments.arg(targetIndex + 1).toString()
            message = message.replaceRange(start, end, replacement)
            matcher = pattern.matcher(message)
        }

        pattern = Pattern.compile("\\{}")
        matcher = pattern.matcher(message)
        var i = 1 // Adjust index to account for the base string argument
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val replacement = arguments.arg(i++).toString()
            message = message.replaceRange(start, end, replacement)
            matcher = pattern.matcher(message)
        }

        val output = "${simpleDateFormat.format(Date(System.currentTimeMillis()))} - $message"
        Registry.Net.sendToClientsWithBlockLoaded(ConsoleMessagePacket(output, blockPos, world.dimension(), level), blockPos, world)
        return NIL
    }

}