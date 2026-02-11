package com.softwaremill.samplesignalserver.devices

import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ServiceId.Aci
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord

class DevicesService(
    private val devicesRepository: DevicesRepository
) {

    fun registerDevice(
        aci: Aci,
        deviceId: Int,
        identityKey: ByteArray,
        signedPreKey: ByteArray,
        preKeys: List<ByteArray>
    ) {
        val device = Device(
            deviceId,
            aci,
            IdentityKey(identityKey),
            SignedPreKeyRecord(signedPreKey),
            preKeys.map { PreKeyRecord(it) }
        )
        // this imitates remote server side
        devicesRepository.saveDevice(device)
    }
}