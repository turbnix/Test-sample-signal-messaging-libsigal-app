@file:OptIn(ExperimentalEncodingApi::class)

package com.softwaremill

import com.softwaremill.ServerClient.UserAddressInfo
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.SessionBuilder
import org.signal.libsignal.protocol.SessionCipher
import org.signal.libsignal.protocol.message.CiphertextMessage.PREKEY_TYPE
import org.signal.libsignal.protocol.message.CiphertextMessage.WHISPER_TYPE
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore
import java.net.URI
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

suspend fun main() {
    println("Enter your username:")
    val username = readln()
    val serverClient = ServerClient(serverURL = URI("http://localhost:8080"))
    val identityKeyPair = IdentityKeyPair.generate()
    val protocolStore = InMemorySignalProtocolStore(identityKeyPair, 2137)

    val preKeys = generatePreKeys(1, 100)
    preKeys.forEach { protocolStore.storePreKey(it.id, it) }

    val signedPreKey = generateSignedPreKey(identityKeyPair, 0)
    protocolStore.storeSignedPreKey(signedPreKey.id, signedPreKey)

    val deviceDTO = serverClient.registerUser(username, identityKeyPair, signedPreKey, preKeys) ?: return
    val otherUsers = mutableMapOf<String, UserAddressInfo>()

    while (true) {
        println("Enter the username of the person you want to send a message to, or leave empty to read the inbox:")
        val receiverName = readln()
        if (receiverName.isNotEmpty()) {
            val receiverInformation = getAndSaveUserInformation(serverClient, receiverName, otherUsers)
            if (!protocolStore.containsSession(receiverInformation.toSignalProtocolAddress())) {
                val userBundle = serverClient.getUserBundle(receiverName)
                SessionBuilder(
                    protocolStore,
                    receiverInformation.toSignalProtocolAddress()
                ).process(userBundle.toPreKeyBundle(deviceDTO.deviceId))
            }
            println("Enter the message:")
            val message = readln()
            val cipher = SessionCipher(protocolStore, receiverInformation.toSignalProtocolAddress())
            val ciphertextMessage = cipher.encrypt(message.toByteArray())
            serverClient.sendMessage(username, receiverName, ciphertextMessage)
        }
        getAndPrintMessages(serverClient, username, otherUsers, protocolStore)
    }
}

private suspend fun getAndPrintMessages(
    serverClient: ServerClient,
    username: String,
    otherUsers: MutableMap<String, UserAddressInfo>,
    protocolStore: InMemorySignalProtocolStore
) {
    serverClient.getMessages(username).map {
        if (it.senderUsername in otherUsers) {
            return@map getDecryptedMessage(protocolStore, otherUsers, it)
        } else {
            getAndSaveUserInformation(serverClient, it.senderUsername, otherUsers)
            return@map getDecryptedMessage(protocolStore, otherUsers, it)
        }
    }.forEach {
        println("Received from (${it.first}): ${String(it.second, StandardCharsets.UTF_8)}")
    }
}

private suspend fun getAndSaveUserInformation(
    serverClient: ServerClient, receiverName: String, otherUsers: MutableMap<String, UserAddressInfo>
): UserAddressInfo {
    val receiverInformation = serverClient.getUserInformation(receiverName)
    otherUsers[receiverName] = receiverInformation
    return receiverInformation
}

private fun getDecryptedMessage(
    protocolStore: InMemorySignalProtocolStore,
    otherUsers: MutableMap<String, UserAddressInfo>,
    it: ServerClient.MessageDTO
): Pair<String, ByteArray> {
    val cipher = SessionCipher(protocolStore, otherUsers[it.senderUsername]!!.toSignalProtocolAddress())
    when (it.messageType) {
        PREKEY_TYPE -> {
            val decryptedMessage = cipher.decrypt(PreKeySignalMessage(Base64.decode(it.messageData)))
            return Pair(it.senderUsername, decryptedMessage)
        }

        WHISPER_TYPE -> {
            val decryptedMessage = cipher.decrypt(SignalMessage(Base64.decode(it.messageData)))
            return Pair(it.senderUsername, decryptedMessage)
        }

        else -> {
            throw IllegalStateException("Unknown message type: ${it.messageType}")
        }
    }
}