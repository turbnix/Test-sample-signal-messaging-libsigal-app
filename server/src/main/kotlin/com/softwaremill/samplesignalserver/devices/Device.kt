package com.softwaremill.samplesignalserver.devices

import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ServiceId
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord

data class Device(
    val id: Int,
    val serviceId: ServiceId,
    val pubKey: IdentityKey,
    val signedPreKey: SignedPreKeyRecord,
    val preKeys: List<PreKeyRecord>
) {

    fun getSignalProtocolAddress() = SignalProtocolAddress(serviceId, id)
}
