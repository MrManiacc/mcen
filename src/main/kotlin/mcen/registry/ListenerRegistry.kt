package mcen.registry

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf

abstract class ListenerRegistry : IRegister {
    override fun register(modId: String, modBus: IEventBus, forgeBus: IEventBus) {
        registerListeners(modBus, forgeBus)
        super.register(modId, modBus, forgeBus)
    }

    private fun registerListeners(modBus: IEventBus, forgeBus: IEventBus) {
        for (member in this::class.functions) {
            if (member.visibility != KVisibility.PUBLIC) continue
            if (member.hasAnnotation<Sub>()) {
                val side = member.findAnnotation<Sub>()?.dist
                if (side == Side.Client && FMLEnvironment.dist != Dist.CLIENT) continue
                if (side == Side.Server && FMLEnvironment.dist != Dist.DEDICATED_SERVER) continue
                for (param in member.parameters) {
                    val type = param.type.classifier as KClass<*>
                    if (type.isSubclassOf(Event::class)) {
                        val modType: Class<out Event> = type.java as Class<out Event>
                        if (type.isSubclassOf(IModBusEvent::class)) modBus.addListener(
                            EventPriority.LOWEST,
                            true,
                            modType
                        ) {
                            member.call(this, it)
                        }
                        else forgeBus.addListener(EventPriority.LOWEST, true, modType) {
                            member.call(this, it)
                        }
                        continue
                    }
                }
            }
        }
    }
}