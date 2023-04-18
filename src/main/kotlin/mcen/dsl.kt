package mcen

import com.mojang.blaze3d.systems.RenderSystem
import mcen.registry.Registry
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.util.LogicalSidedProvider
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.util.thread.SidedThreadGroups
import net.minecraftforge.server.ServerLifecycleHooks
import java.util.*
import java.util.concurrent.CompletableFuture

import mcen.api.api.graph.type.Type
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.IItemHandler

/**
 * Gets a resource location based upon the give string
 */
internal val String.resLoc: ResourceLocation
    get() = ResourceLocation(ModEntry.Id, this)


fun AbstractContainerScreen<*>.isHovered(
    pX: Int,
    pY: Int,
    pWidth: Int,
    pHeight: Int,
    mouseX: Double,
    mouseY: Double
): Boolean {
    val pMouseX = mouseX - this.guiLeft
    val pMouseY = mouseY - this.guiTop
    return pMouseX >= (pX - 1).toDouble() && pMouseX < (pX + pWidth + 1).toDouble() && pMouseY >= (pY - 1).toDouble() && pMouseY < (pY + pHeight + 1).toDouble()
}

/**This boolean checks to see if the current program is on the physical client or not**/
internal val physicalClient: Boolean
    get() = FMLEnvironment.dist == Dist.CLIENT

/**This boolean checks to see if the current program is on the physical server or not**/
internal val physicalServer: Boolean
    get() = FMLEnvironment.dist == Dist.DEDICATED_SERVER

/**This boolean checks to see if the current thread group is thew logical client**/
internal val logicalClient: Boolean
    get() {
        if (physicalServer) return false //This is so we don't end up calling [Minecraft] calls from the client
        if (Thread.currentThread().threadGroup == SidedThreadGroups.CLIENT) return true
        try {
            if (RenderSystem.isOnRenderThread()) return true
        } catch (notFound: ClassNotFoundException) {
            return false //We're not on the client if there's a class not found execetion
        }
        return false
    }


/**This boolean checks to see if the current thread group is thew logical client**/
internal val logicalServer: Boolean
    get() = Thread.currentThread().threadGroup == SidedThreadGroups.SERVER

/**This boolean checks to see if the current thread group is thew logical client**/
internal val logicalRender: Boolean
    get() = RenderSystem.isOnRenderThread()

/**
 * This block of code will execute only if we're on the physical client
 */
internal fun whenClient(logical: Boolean = true, block: () -> Unit) {
    if (logical && logicalClient) block()
    else if (!logical && physicalClient) block()
}

/**
 * This block of code will execute only if we're on the physical client
 */
internal fun whenServer(logical: Boolean = true, block: () -> Unit) {
    if (logical && logicalServer) block()
    else if (!logical && physicalServer) block()
}

/**
 * This will run the given block on the logical side
 */
internal fun runOn(side: LogicalSide, block: () -> Unit): CompletableFuture<Void> {
    val executor = LogicalSidedProvider.WORKQUEUE.get(side)
    return if (!executor.isSameThread)
        executor.submit(block) // Use the internal method so thread check isn't done twice
    else {
        block()
        CompletableFuture.completedFuture(null)
    }
}

/**
 * This run the given chunk of code on the client
 */
internal fun runOnClient(block: () -> Unit): CompletableFuture<Void> {
    return runOn(LogicalSide.CLIENT, block)
}

/**
 * This will run the render method
 */
internal fun runOnRender(block: () -> Unit) {
    if (logicalRender)
        block()
    else
        RenderSystem.recordRenderCall(block)
}

/**
 * This run the given chunk of code on the server
 */
internal fun runOnServer(block: () -> Unit): CompletableFuture<Void> {
    return runOn(LogicalSide.SERVER, block)
}

/**
 * Gets the player based on the uuid, ONLY WORKS ON SERVER
 */
val UUID.asPlayer: Optional<ServerPlayer>
    get() = Optional.ofNullable(ServerLifecycleHooks.getCurrentServer().playerList.getPlayer(this) as ServerPlayer)

inline fun <reified T : BlockEntity> Registry<BlockEntityType<*>>.tile(
    block: Block,
    crossinline supplier: (Pair<BlockPos, BlockState>) -> T
): BlockEntityType<T> {
    return BlockEntityType.Builder.of({ pos, state -> supplier(pos to state) }, block).build(null)
}
/**
 * This will raytrace the given distance for the given player
 */
fun Player.rayTrace(distance: Double = 75.0): BlockHitResult {
    val rayTraceResult = pick(distance, 0f, false) as BlockHitResult
    var xm = rayTraceResult.location.x
    var ym = rayTraceResult.location.y
    var zm = rayTraceResult.location.z
    var pos = BlockPos(xm, ym, zm)
    val block = level.getBlockState(pos)
    if (block.isAir) {
        if (rayTraceResult.direction == Direction.SOUTH)
            zm--
        if (rayTraceResult.direction == Direction.EAST)
            xm--
        if (rayTraceResult.direction == Direction.UP)
            ym--
    }
    pos = BlockPos(xm, ym, zm)
    return BlockHitResult(rayTraceResult.location, rayTraceResult.direction, pos, false)
}





interface Serial : INBTSerializable<CompoundTag> {
    fun CompoundTag.serialize() = Unit
    fun CompoundTag.deserialize() = Unit

    override fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.serialize()
        return tag
    }

    override fun deserializeNBT(nbt: CompoundTag) = nbt.deserialize()
}

fun IItemHandler.serialize(): CompoundTag {
    val nbtTagList = ListTag()
    for (i in 0 until slots) {
        val stack = getStackInSlot(i)
        if (!stack.isEmpty) {
            val itemTag = CompoundTag()
            itemTag.putInt("Slot", i)
            stack.save(itemTag)
            nbtTagList.add(itemTag)
        }
    }
    val nbt = CompoundTag()
    nbt.put("Items", nbtTagList)
    nbt.putInt("Size", slots)
    return nbt
}

fun IItemHandler.deserialize(tag: CompoundTag) {
    val size = tag.getInt("Size")
    val tagList: ListTag = tag.getList("Items", Tag.TAG_COMPOUND.toInt())
    assert(size == this.slots && tagList.size == size)
    for (i in 0 until tagList.size) {
        val itemTags = tagList.getCompound(i)
        val slot = itemTags.getInt("Slot")
        if (slot in 0 until slots) {
            //Remove the item from the slot
            extractItem(slot, 64, false)
            insertItem(slot, ItemStack.of(itemTags), false)
        }
    }
}

fun CompoundTag.putPrimitive(name: String, value: Any?) = when (value) {
    is Boolean -> {
        putEnum("${name}_type", Type.BOOLEAN)
        putBoolean("${name}_value", value)
    }
    is Int -> {
        putEnum("${name}_type", Type.INT)
        putInt("${name}_value", value)
    }
    is Float -> {
        putEnum("${name}_type", Type.FLOAT)
        putFloat("${name}_value", value)
    }
    is String -> {
        putEnum("${name}_type", Type.STRING)
        putString("${name}_value", value)
    }
//    is BlockPos -> {
//        putEnum("${name}_type", Type.BLOCK_POS)
//        putBlockPos("${name}_value", value)
//    }
//    is Direction -> {
//        putEnum("${name}_type", Type.BLOCK_FACE)
//        putEnum("${name}_value", value)
//    }
//    else -> putEnum("${name}_type", Type.NULL)//Unsupported
    else -> {}
}

fun CompoundTag.getPrimitive(name: String): Any? {
    return when (getEnum<Type>("${name}_type")) {
        Type.BOOLEAN -> getBoolean("${name}_value")
        Type.INT -> getInt("${name}_value")
        Type.FLOAT -> getFloat("${name}_value")
        Type.STRING -> getString("${name}_value")
//        Type.BLOCK_POS -> getBlockPos("${name}_value")
//        Type.BLOCK_FACE -> getEnum<Direction>("${name}_value")
        else -> null //Null
    }
}


/**This will write the given list into the compound. **/
inline fun <reified T : INBTSerializable<CompoundTag>> CompoundTag.putList(name: String, list: List<T>): CompoundTag {
    val tag = CompoundTag()
    this.putInt("${name}_list_size", list.size)
    list.forEachIndexed { i, value ->
        tag.put("v_$i", value.serializeNBT())
    }
    this.put("${name}_list", tag)
    return this
}

/**This will read a list of the given type**/
inline fun <reified T : INBTSerializable<CompoundTag>> CompoundTag.getList(name: String): List<T> {
    val tag = this.getCompound("${name}_list") ?: return emptyList()
    val size = this.getInt("${name}_list_size")
    val list = ArrayList<T>()
    for (i in 0 until size) {
        val value = tag.getCompound("v_$i") ?: continue
        if (value !is T) continue
        value.deserializeNBT(value)
        list.add(value)
    }
    return list
}

/**This will write the given list into the compound. **/
inline fun <reified T : INBTSerializable<CompoundTag>> CompoundTag.putDeepList(
    name: String,
    list: List<T>
): CompoundTag {
    val tag = CompoundTag()
    this.putInt("${name}_list_size", list.size)
    list.forEachIndexed { i, value ->
        tag.putClass("c_$i", value::class.java)
        tag.put("v_$i", value.serializeNBT())
    }
    this.put("${name}_list", tag)
    return this
}

/**This will read a list of the given type**/
inline fun <reified T : INBTSerializable<CompoundTag>> CompoundTag.getDeepList(name: String): List<T> {
    val list = ArrayList<T>()
    getDeepList(name, list)
    return list
}

/**This will read a list of the given type**/
inline fun <reified T : INBTSerializable<CompoundTag>> CompoundTag.getDeepList(name: String, list: MutableList<T>) {
    val tag = this.getCompound("${name}_list") ?: return
    val size = this.getInt("${name}_list_size")
    for (i in 0 until size) {
        val value = tag.getCompound("v_$i") ?: continue
        val clazz = tag.getClass("c_$i")
        if (T::class.java.isAssignableFrom(clazz)) list.add((clazz.newInstance() as T).apply { deserializeNBT(value) })
    }
}

/**
 * This will write a uuid
 */
fun CompoundTag.putClass(name: String, clazz: Class<*>) {
    this.putString(name, clazz.name)
}

/**
 * This will get a class with the given super type of T
 */
fun CompoundTag.getClass(name: String): Class<*> {
    val clsName = this.getString(name)
    return Class.forName(clsName)
}

/**
 * This will write a uuid
 */
@Deprecated(
    message = "This method already exists in the compound class, I was unaware and am too lazy to remove calls to this at this time. :)"
)
fun CompoundTag.putUUID(name: String, uuid: UUID): CompoundTag {
    this.putLongArray(name, longArrayOf(uuid.mostSignificantBits, uuid.leastSignificantBits))
    return this
}

/**
 * This will read a uuid
 */
/**
 * This will write a uuid
 */
@Deprecated(
    message = "This method already exists in the compound class, I was unaware and am too lazy to remove calls to this at this time. :)"
)
fun CompoundTag.getUUID(name: String): UUID? {
    val array = this.getLongArray(name)
    if (array.size != 2) return null
    return UUID(array[0], array[1])
}

/**
 * This will write a uuid
 */
fun CompoundTag.putBlockPos(name: String, pos: BlockPos): CompoundTag {
    this.putIntArray(name, intArrayOf(pos.x, pos.y, pos.z))
    return this
}

/**
 * This will read a uuid
 */
fun CompoundTag.getBlockPos(name: String): BlockPos {
    val array = this.getIntArray(name)
    if (array.size != 3) return BlockPos.ZERO
    return BlockPos(array[0], array[1], array[2])
}

/**
 * This will put a float array iin the compound tag
 */
fun CompoundTag.putFloatArray(name: String, floatArray: FloatArray) {
    val tag = CompoundTag()
    tag.putInt("size", floatArray.size)
    for (i in floatArray.indices)
        tag.putFloat("f_$i", floatArray[i])
    this.put(name, tag)
}

/**
 * This will read the float array
 */
fun CompoundTag.getFloatArray(name: String): FloatArray {
    val tag = this.getCompound(name)
    val size = tag.getInt("size")
    val array = FloatArray(size)
    for (i in array.indices)
        array[i] = tag.getFloat("f_$i")
    return array
}

/**
 * This will write a uuid
 */
inline fun <reified T : Enum<*>> CompoundTag.putEnum(name: String, enum: T): CompoundTag {
    this.putInt(name, enum.ordinal)
    return this
}

/**
 * This will read a uuid
 */
inline fun <reified T : Enum<*>> CompoundTag.getEnum(name: String): T {
    return T::class.java.enumConstants[this.getInt(name)]
}

///**Puts a vector2 value**/
//fun CompoundTag.putVec2(name: String, vec: Vector2f): CompoundTag {
//    this.putFloatArray("${name}_vec2", floatArrayOf(vec.x, vec.y))
//    return this
//}

/**Puts a vector3 value**/
fun CompoundTag.putVec3(name: String, vec: Vector3f): CompoundTag {
    this.putFloatArray("${name}_vec3", floatArrayOf(vec.x(), vec.y(), vec.z()))
    return this
}

/**Puts a vector3 value**/
fun CompoundTag.putVec4(name: String, vec: Vector4f): CompoundTag {
    this.putFloatArray("${name}_vec4", floatArrayOf(vec.x(), vec.y(), vec.z(), vec.w()))
    return this
}

///**Gets a vector2f with the given name**/
//fun CompoundTag.getVec2(name: String): Vector2f {
//    val array = this.getFloatArray("${name}_vec2")
//    assert(array.size == 2) { "Attempted to read vec2 from float array, but found invalid size of ${array.size}" }
//    return Vector2f(array[0], array[1])
//}

/**Gets a vector2f with the given name**/
fun CompoundTag.getVec3(name: String): Vector3f {
    val array = this.getFloatArray("${name}_vec3")
    assert(array.size == 3) { "Attempted to read vec3 from float array, but found invalid size of ${array.size}" }
    return Vector3f(array[0], array[1], array[2])
}

/**Gets a vector2f with the given name**/
fun CompoundTag.getVec4(name: String): Vector4f {
    val array = this.getFloatArray("${name}_vec4")
    assert(array.size == 4) { "Attempted to read vec3 from float array, but found invalid size of ${array.size}" }
    return Vector4f(array[0], array[1], array[2], array[3])
}
//
///**Writes a packet buffer's class by simply putting the name**/
//fun PacketBuffer.writeClass(clazz: Class<*>): PacketBuffer = this.writeString(clazz.name)
//
///**This will attempt to read the class with the reified type and cast it to the given type**/
//fun PacketBuffer.readClass(): Class<*>? {
//    val name = this.readString()
//    return try {
//        Class.forName(name)
//    } catch (notFound: ClassNotFoundException) {
//        println("Failed to find class by name $name")
//        null
//    }
//}