package mcen.api.graph.type

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction


enum class Type(val typeName: String, val typeClass: Class<*>) {
    BOOLEAN("Boolean", Boolean::class.javaObjectType),
    INT("Int", Int::class.javaObjectType),
    BLOCK_POS("BlockPos", BlockPos::class.javaObjectType),
    BLOCK_FACE("BlockFace", Direction::class.javaObjectType),
    FLOAT("Float", Float::class.javaObjectType),
    STRING("String", String::class.javaObjectType),
    ANY("Any", Object::class.javaObjectType);
}