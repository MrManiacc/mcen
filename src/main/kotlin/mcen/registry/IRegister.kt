package mcen.registry

import net.minecraftforge.eventbus.api.IEventBus
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import kotlin.reflect.full.isSubclassOf

interface IRegister {
    fun register(modId: String, modBus: IEventBus, forgeBus: IEventBus) {
        registerAll(modId, modBus, forgeBus)
    }
}


inline fun <reified T : IRegister> T.registerAll(
    modID: String,
    modBus: IEventBus,
    forgeBus: IEventBus
) {
    for (child in this::class.nestedClasses) {
        if (child.isSubclassOf(IRegister::class)) {
            val instance = child.objectInstance ?: continue
            if (instance is IRegister) {
                instance.register(modID, modBus, forgeBus)
            }
        }
    }
}