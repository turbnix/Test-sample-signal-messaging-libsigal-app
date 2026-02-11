package com.softwaremill.samplesignalserver.sessions

import com.softwaremill.samplesignalserver.devices.DevicesRepository
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.PreKeyBundle
import org.springframework.transaction.annotation.Transactional

open class DefaultSessionsService(
    private val devicesRepository: DevicesRepository
) : SessionsService {

    @Transactional
    override fun createPreKeyBundle(protocolAddress: SignalProtocolAddress): PreKeyBundle {
        val device = devicesRepository.getDeviceInfo(protocolAddress)
        val preKey = if (device.preKeys.isNotEmpty()) device.preKeys.first() else null
        if (preKey != null) devicesRepository.burnPreKey(protocolAddress, preKey.id)

        return PreKeyBundle(
            2137,
            device.id,
            preKey?.id ?: -1,
            preKey?.keyPair?.publicKey,
            device.signedPreKey.id,
            device.signedPreKey.keyPair.publicKey,
            device.signedPreKey.signature,
            device.pubKey
        )
    }
}