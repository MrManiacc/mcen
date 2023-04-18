package mcen

import mcen.content.internal.Registry
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.*

@Mod(ModEntry.Id)
object ModEntry {
    const val Id: String = "mcen"

    init {
        Registry.register(Id, MOD_BUS, FORGE_BUS)
    }
}