package com.softwaremill

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.ServiceId
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.ecc.ECPublicKey
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.state.PreKeyBundle
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import java.net.URI
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ServerClient(
    private val serverURL: URI,
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
) {

    suspend fun registerUser(
        username: String, identityKeyPair: IdentityKeyPair, signedPreKeyRecord: SignedPreKeyRecord,
        preKeys: List<PreKeyRecord>
    ): UserAddressInfo? {
        val response = client.post("$serverURL/users") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterUserDTO(
                    username,
                    identityKeyPair.publicKey.serialize(),
                    signedPreKeyRecord.serialize(),
                    preKeys.map { it.serialize() })
            )
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                println("User $username registered")
                return response.body<UserAddressInfo>()
            }

            HttpStatusCode.Conflict -> {
                println("User $username already registered")
                return null
            }

            else -> {
                println("Failed to register user $username: ${response.status}")
                return null
            }
        }
    }

    suspend fun getUserInformation(receiverName: String): UserAddressInfo {
        val response = client.get("$serverURL/users/$receiverName")
        when (response.status) {
            HttpStatusCode.OK -> {
                return response.body<UserAddressInfo>()
            }

            HttpStatusCode.NotFound -> {
                println("User $receiverName not found")
                throw IllegalStateException("User $receiverName not found")
            }

            else -> {
                println("Failed to get user $receiverName information: ${response.status}")
                throw IllegalStateException("Failed to get user $receiverName information")
            }
        }
    }

    suspend fun getUserBundle(receiverName: String): UserPreKeyBundleDto {
        val response = client.get("$serverURL/users/$receiverName/bundle")
        when (response.status) {
            HttpStatusCode.OK -> {
                return response.body<UserPreKeyBundleDto>()
            }

            HttpStatusCode.NotFound -> {
                println("User $receiverName not found")
                throw IllegalStateException("User $receiverName not found")
            }

            else -> {
                println("Failed to get user $receiverName information: ${response.status}")
                throw IllegalStateException("Failed to get user $receiverName information")
            }
        }
    }

    suspend fun sendMessage(senderName: String, receiverName: String, encryptedMessage: CiphertextMessage) {
        val response = client.post("$serverURL/messages") {
            contentType(ContentType.Application.Json)
            setBody(
                SendMessageDTO(
                    senderName,
                    receiverName, encryptedMessage.serialize(), encryptedMessage.type
                )
            )
        }
        if (response.status != HttpStatusCode.OK) {
            println("Failed to send message to $receiverName: ${response.status}")
        }
    }

    suspend fun getMessages(username: String): List<MessageDTO> {
        val response = client.get("$serverURL/users/$username/messages")
        when (response.status) {
            HttpStatusCode.OK -> {
                return response.body<List<MessageDTO>>()
            }

            else -> {
                println("Failed to get messages for user $username : ${response.status}")
                throw IllegalStateException("Failed to get messages for user $username information")
            }
        }
    }

    @Serializable
    data class MessageDTO(
        val messageId: String,
        val senderUsername: String,
        val messageData: String,
        val messageType: Int
    )

    @Serializable
    data class SendMessageDTO(
        val senderUsername: String,
        val receiverUsername: String,
        val text: ByteArray,
        val messageType: Int
    )

    @Serializable
    data class RegisterUserDTO(
        val username: String,
        val identityKey: ByteArray,
        val signedPreKey: ByteArray,
        val preKeys: List<ByteArray>
    )

    @Serializable
    data class UserAddressInfo(val aci: String, val deviceId: Int) {
        fun toSignalProtocolAddress(): SignalProtocolAddress {
            val aci = ServiceId.Aci.parseFromString(aci)
            return SignalProtocolAddress(aci, deviceId)
        }
    }

    @Serializable
    data class UserPreKeyBundleDto(
        val identityKey: String,
        val signedPreKeyId: Int,
        val signedPreKey: String,
        val signedPreKeySignature: String,
        val preKeyId: Int,
        val preKey: String
    ) {
        @OptIn(ExperimentalEncodingApi::class)
        fun toPreKeyBundle(deviceId: Int): PreKeyBundle {
            return PreKeyBundle(
                2137,
                deviceId,
                preKeyId,
                ECPublicKey(Base64.decode(preKey)),
                signedPreKeyId,
                ECPublicKey(Base64.decode(signedPreKey)),
                Base64.decode(signedPreKeySignature),
                IdentityKey(Base64.decode(identityKey))
            )
        }
    }

}