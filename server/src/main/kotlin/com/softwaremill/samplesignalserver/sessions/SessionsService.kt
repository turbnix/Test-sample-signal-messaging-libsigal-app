package com.softwaremill.samplesignalserver.sessions

import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.PreKeyBundle

interface SessionsService {
    fun createPreKeyBundle(protocolAddress: SignalProtocolAddress): PreKeyBundle
}