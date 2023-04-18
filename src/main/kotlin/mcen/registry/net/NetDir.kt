package mcen.registry.net

import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent

enum class NetDir {
    ToClient,
    ToServer,
    Other
}

val NetworkEvent.Context.dir: NetDir
    get() {
        return when (direction) {
            NetworkDirection.PLAY_TO_SERVER -> NetDir.ToServer
            NetworkDirection.PLAY_TO_CLIENT -> NetDir.ToClient
            NetworkDirection.LOGIN_TO_SERVER -> NetDir.Other
            NetworkDirection.LOGIN_TO_CLIENT -> NetDir.Other
        }
    }