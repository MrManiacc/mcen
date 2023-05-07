package mcen.api.workspace

import mcen.Serial
import mcen.getStringKeyMap
import mcen.putStringKeyMap
import net.minecraft.nbt.CompoundTag

open class Folder(var name: String = "") : Serial {
    val folders: MutableMap<String, Folder> = hashMapOf()
    val files: MutableMap<String, File> = hashMapOf()

    /**
     * Adds a file or replaces the sourceCode if present
     */
    fun addFile(file: File): File {
        if (files.containsKey(file.name)) files[file.name]!!.sourceCode = file.sourceCode
        else files[file.name] = file
        return file
    }

    /**
     * Adds a folder as a child of this folder
     */
    fun addFolder(folder: Folder): Folder {
        folders[folder.name] = folder
        return folder
    }

    fun addFolder(path: String): Folder {
        if (!path.contains("/")) return addFolder(Folder(path))
        val split = path.split("/")
        var currentFolder: Folder = this
        for (i in 0 until split.size - 1) {
            currentFolder =
                currentFolder.getFolder(split[i]) ?: currentFolder.addFolder(split[i])
        }
        return currentFolder.addFolder(split.last())
    }

    fun getFolder(name: String): Folder? = folders[name]

    fun getFile(name: String): File? = files[name]

    override fun CompoundTag.serialize() {
        putString("name", name)
        putStringKeyMap("folders", folders)
        putStringKeyMap("files", files)
    }

    override fun CompoundTag.deserialize() {
        name = getString("name")
        folders.putAll(getStringKeyMap("folders"))
        files.putAll(getStringKeyMap("files"))
    }

    fun hasFile(path: String): Boolean {
        if (!path.contains("/")) return files.containsKey(path)
        val split = path.split("/")
        var currentFolder: Folder = this
        for (i in 0 until split.size - 1) {
            currentFolder =
                currentFolder.getFolder(split[i]) ?: return false
        }
        return currentFolder.hasFile(split.last())
    }

    override fun toString(): String = toString(0)

    fun toString(indent: Int): String {
        val sb = StringBuilder()
        sb.append("\t".repeat(indent)).append(name).append("/").append("\n")
        files.keys.forEach {
            sb.append("\t".repeat(indent + 1)).append(it).append(".lua").append("\n")
        }
        folders.values.forEach {
            sb.append(it.toString(indent + 1)).append("/").append("\n")
        }
        return sb.toString()
    }
}