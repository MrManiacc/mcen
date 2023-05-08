package mcen.api.workspace

import mcen.Serial
import net.minecraft.nbt.CompoundTag


class File(var name: String = "", var sourceCode: String = "") : Serial {

    var parent: Folder? = null

    val path:String get() =  "${parent?.path ?: ""}/$name"

    override fun CompoundTag.deserialize() {
        name = getString("name")
        sourceCode = getString("sourceCode")
    }

    override fun CompoundTag.serialize() {
        putString("name", name)
        putString("sourceCode", sourceCode)
    }

    companion object {
        val InvalidFile = File("unnamed*", "")
    }
}
