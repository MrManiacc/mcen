package mcen.api.workspace

import mcen.*
import net.minecraft.nbt.CompoundTag

/**
 * A virtual environment that will be stored per block entity instance.
 */
class Workspace : Folder("root") {

    /**
     * Reads a file from the virtual path
     */
    fun readFile(path: String): File {
        if (!path.contains("/")) return getFile(path) ?: return File.InvalidFile
        val split = path.split("/")
        var currentFolder: Folder = this
        for (i in 0 until split.size - 1) {
            currentFolder =
                currentFolder.getFolder(split[i]) ?: return File.InvalidFile
        }
        return currentFolder.getFile(split.last()) ?: return File.InvalidFile
    }


    /**
     * Writes a file to the virtual path, creating the folders if they don't exist
     */
    fun writeFile(path: String, source: String): File {
        if (!path.contains("/")) return addFile(File(path, source))
        val split = path.split("/")
        var currentFolder: Folder = this
        for (i in 0 until split.size - 1) {
            currentFolder =
                currentFolder.getFolder(split[i]) ?: addFolder(Folder(split[i]))
        }
        return currentFolder.addFile(File(split.last(), source))
    }

    override fun toString(): String = toString(0)


}