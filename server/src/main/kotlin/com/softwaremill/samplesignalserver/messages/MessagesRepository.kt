package com.softwaremill.samplesignalserver.messages

import org.signal.libsignal.protocol.SignalProtocolAddress
import java.util.*

interface MessagesRepository {
    fun saveMessage(
        sender: SignalProtocolAddress,
        receiver: SignalProtocolAddress,
        messageType: Int,
        message: ByteArray
    )

    fun readAllMessages(signalProtocolAddress: SignalProtocolAddress): List<Message>
    fun removeMessages(receiver: SignalProtocolAddress, messageIds: Set<UUID>)
}