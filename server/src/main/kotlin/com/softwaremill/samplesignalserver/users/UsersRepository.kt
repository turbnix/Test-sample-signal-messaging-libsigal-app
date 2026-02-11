package com.softwaremill.samplesignalserver.users

import org.signal.libsignal.protocol.ServiceId
import org.signal.libsignal.protocol.SignalProtocolAddress

interface UsersRepository {
    fun saveUser(username: String, aci: ServiceId.Aci, deviceId: Int)
    fun getUserInfo(username: String): User?
    fun getUsername(signalProtocolAddress: SignalProtocolAddress): String?

    data class User(val username: String, val aci: ServiceId.Aci, val deviceId: Int)
}