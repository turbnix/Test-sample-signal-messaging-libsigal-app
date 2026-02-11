package com.softwaremill

import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.ecc.Curve
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.signal.libsignal.protocol.util.Medium

fun generatePreKeys(start: Int, count: Int): List<PreKeyRecord> {
    val keys = mutableListOf<PreKeyRecord>()
    for (i in 0..<count) {
        keys += PreKeyRecord(((start + i) % (Medium.MAX_VALUE - 1)) + 1, Curve.generateKeyPair())
    }
    return keys
}

fun generateSignedPreKey(identityKeyPair: IdentityKeyPair, signedPreKeyId: Int): SignedPreKeyRecord {
    val keyPair = Curve.generateKeyPair()
    val signature = Curve.calculateSignature(identityKeyPair.privateKey, keyPair.publicKey.serialize())

    return SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature)
}