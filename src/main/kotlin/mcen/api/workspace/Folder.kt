package mcen.api.workspace

import mcen.Serial
import mcen.getStringKeyMap
import mcen.putStringKeyMap
import net.minecraft.nbt.CompoundTag
import java.util.concurrent.ConcurrentHashMap

open class Folder(var name: String = "") : Serial {
    val folders: MutableMap<String, Folder> = ConcurrentHashMap()
    val files: MutableMap<String, File> = ConcurrentHashMap()
    var parent: Folder? = null
        private set
    val isRoot: Boolean get() = parent == null
    val path :String get() = if (isRoot)  name
    else if(parent!!.isRoot) parent!!.path + name else "${parent!!.path}/$name"

    /**
     * Adds a file or replaces the sourceCode if present
     */
    fun addFile(file: File): File {
        var f = if(files.containsKey(file.name)) files[file.name]!! else file
        if(f.name.startsWith("/")) f = File(f.name.substring(1), f.sourceCode)
        f.sourceCode = file.sourceCode
        files[file.name] = f
        f.parent = this
        return f
    }

    /**
     * Adds a folder as a child of this folder
     */
    fun addFolder(folder: Folder): Folder {
        folders[folder.name] = folder
        folder.parent = this
        return folder
    }

    fun removeFolder(folder: Folder) {
        folders.remove(folder.name)
    }

    fun addFolder(path: String): Folder {
        if(path == "/") return this
        val path = if(path.startsWith("/")) path.substring(1) else path
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
        folders.clear()
        files.clear()
        folders.putAll(getStringKeyMap("folders"))
        files.putAll(getStringKeyMap("files"))
        folders.forEach{
            it.value.parent = this@Folder
        }
        files.forEach {
            it.value.parent = this@Folder
        }
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

    fun removeFile(file: File) {
        files.remove(file.name)
    }
}