package com.softwaremill.samplesignalserver.messages

import org.signal.libsignal.protocol.SignalProtocolAddress
import java.util.*

data class Message(
    val messageId: UUID,
    val senderAddress: SignalProtocolAddress,
    val messageData: ByteArray,
    val messageType: Int
)
