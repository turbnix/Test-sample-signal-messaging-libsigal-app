package com.softwaremill.samplesignalserver.messages

import org.signal.libsignal.protocol.ServiceId.Aci
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class MySqlMessagesRepository(
    private val jdbcTemplate: JdbcTemplate
) : MessagesRepository {

    override fun saveMessage(
        sender: SignalProtocolAddress,
        receiver: SignalProtocolAddress,
        messageType: Int,
        message: ByteArray
    ) {
        val sql = """
            INSERT INTO messages (message_id, sender_device_id, sender_service_id, receiver_device_id, receiver_service_id, message, message_type)
            VALUES (:messageId, :senderDeviceId, :senderServiceId, :receiverDeviceId, :receiverServiceId, :messageData, :messageType)
        """.trimIndent()
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameters = mapOf(
            "messageId" to UUID.randomUUID().toString(),
            "receiverDeviceId" to receiver.deviceId,
            "receiverServiceId" to receiver.serviceId?.toServiceIdBinary(),
            "senderDeviceId" to sender.deviceId,
            "senderServiceId" to sender.serviceId?.toServiceIdBinary(),
            "messageData" to message,
            "messageType" to messageType
        )
        namedJdbcTemplate.update(sql, parameters)
    }

    override fun removeMessages(receiver: SignalProtocolAddress, messageIds: Set<UUID>) {
        if (messageIds.isEmpty()) return
        val sql = """
            DELETE FROM messages
            WHERE receiver_device_id = :receiverDeviceId AND receiver_service_id = :receiverServiceId AND message_id IN (:messageIds)
        """.trimIndent()
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameters = mapOf(
            "receiverDeviceId" to receiver.deviceId,
            "receiverServiceId" to receiver.serviceId?.toServiceIdBinary(),
            "messageIds" to messageIds.map { it.toString() }
        )
        namedJdbcTemplate.update(sql, parameters)
    }

    override fun readAllMessages(signalProtocolAddress: SignalProtocolAddress): List<Message> {
        val sql = """
            SELECT message_id, sender_service_id, sender_device_id, message, message_type
            FROM messages
            WHERE receiver_device_id = :deviceId AND receiver_service_id = :serviceId
        """.trimIndent()
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameters = mapOf(
            "deviceId" to signalProtocolAddress.deviceId,
            "serviceId" to signalProtocolAddress.serviceId?.toServiceIdBinary()
        )
        return namedJdbcTemplate.query(sql, parameters) { rs, _ ->
            val messageType = rs.getInt("message_type")
            val message = rs.getBytes("message")
            val senderAddress = SignalProtocolAddress(
                Aci.parseFromBinary(rs.getBytes("sender_service_id")),
                rs.getInt("sender_device_id")
            )
            val messageId = UUID.fromString(rs.getString("message_id"))
            when (messageType) {
                CiphertextMessage.PREKEY_TYPE -> Message(
                    messageId,
                    senderAddress,
                    PreKeySignalMessage(message).serialize()!!,
                    messageType
                )

                else -> Message(messageId, senderAddress, SignalMessage(message).serialize()!!, messageType)
            }
        }
    }
}