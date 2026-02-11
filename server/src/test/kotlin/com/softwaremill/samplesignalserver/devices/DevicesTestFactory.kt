package com.softwaremill.samplesignalserver.devices

import com.softwaremill.samplesignalserver.crypto.generatePreKeys
import com.softwaremill.samplesignalserver.crypto.generateSignedPreKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.ServiceId.Aci
import java.security.SecureRandom
import java.util.*

private val random = SecureRandom()

fun createRandomDevice(): Device {
    val serviceId = Aci(UUID.randomUUID())
    val deviceId = random.nextInt(Int.MAX_VALUE)
    val keyPair = IdentityKeyPair.generate()
    val signedPreKey = generateSignedPreKey(keyPair, random.nextInt(Int.MAX_VALUE))
    return Device(
        id = deviceId, serviceId = serviceId, pubKey = keyPair.publicKey, signedPreKey = signedPreKey,
        preKeys = generatePreKeys(signedPreKey.id + 1, 100)
    )
}