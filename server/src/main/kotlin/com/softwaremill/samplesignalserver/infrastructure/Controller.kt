package com.softwaremill.samplesignalserver.infrastructure

import com.softwaremill.samplesignalserver.devices.DevicesService
import com.softwaremill.samplesignalserver.messages.MessagesRepository
import com.softwaremill.samplesignalserver.sessions.SessionsService
import com.softwaremill.samplesignalserver.users.MySqlUsersRepository
import org.signal.libsignal.protocol.ServiceId
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.SecureRandom
import java.util.*

@RestController
@CrossOrigin
class Controller(
    private val devicesService: DevicesService,
    private val usersRepository: MySqlUsersRepository,
    private val sessionsService: SessionsService,
    private val messagesRepository: MessagesRepository
) {

    companion object {
        private val random = SecureRandom()
    }

    @PostMapping("/users")
    fun registerUser(
        @RequestBody dto: RegisterUserDTO
    ): ResponseEntity<UserInfoDto> {
        try {
            val aci = ServiceId.Aci(UUID.randomUUID())
            val deviceId = random.nextInt(Int.MAX_VALUE)
            usersRepository.saveUser(dto.username, aci, deviceId)
            devicesService.registerDevice(
                aci,
                deviceId,
                dto.identityKey,
                dto.signedPreKey,
                dto.preKeys
            )
            return ResponseEntity.ok(UserInfoDto(aci.toServiceIdString(), deviceId))
        } catch (duplicateKeyException: DuplicateKeyException) {
            return ResponseEntity(HttpStatusCode.valueOf(409))
        }
    }

    data class RegisterUserDTO(
        val username: String,
        val identityKey: ByteArray,
        val signedPreKey: ByteArray,
        val preKeys: List<ByteArray>
    )

    data class UserInfoDto(val aci: String, val deviceId: Int)

    @GetMapping("/users/{username}")
    fun getUserInfo(
        @PathVariable username: String
    ): ResponseEntity<UserInfoDto> {
        val user = usersRepository.getUserInfo(username)
        return if (user != null) {
            ResponseEntity.ok(UserInfoDto(user.aci.toServiceIdString(), user.deviceId))
        } else {
            ResponseEntity(HttpStatusCode.valueOf(404))
        }
    }

    @GetMapping("/users/{username}/bundle")
    fun getUserPreBundle(
        @PathVariable username: String
    ): ResponseEntity<UserBundleDto> {
        val user = usersRepository.getUserInfo(username) ?: return ResponseEntity(HttpStatusCode.valueOf(404))

        val protocolAddress = SignalProtocolAddress(user.aci, user.deviceId)
        val preKeyBundle = sessionsService.createPreKeyBundle(protocolAddress)
        return ResponseEntity.ok(
            UserBundleDto(
                preKeyBundle.identityKey.serialize(),
                preKeyBundle.signedPreKeyId,
                preKeyBundle.signedPreKey.serialize(),
                preKeyBundle.signedPreKeySignature,
                preKeyBundle.preKeyId,
                preKeyBundle.preKey.serialize()
            )
        )
    }

    data class UserBundleDto(
        val identityKey: ByteArray,
        val signedPreKeyId: Int,
        val signedPreKey: ByteArray,
        val signedPreKeySignature: ByteArray,
        val preKeyId: Int,
        val preKey: ByteArray
    )


    @PostMapping("/messages")
    fun sendMessage(
        @RequestBody dto: SendMessageDTO
    ) {
        val senderInfo = usersRepository.getUserInfo(dto.senderUsername)
        val receiverInfo = usersRepository.getUserInfo(dto.receiverUsername)
        messagesRepository.saveMessage(
            SignalProtocolAddress(senderInfo!!.aci, senderInfo.deviceId),
            SignalProtocolAddress(receiverInfo!!.aci, receiverInfo.deviceId),
            dto.messageType,
            dto.text
        )
    }

    data class SendMessageDTO(
        val senderUsername: String,
        val receiverUsername: String,
        val text: ByteArray,
        val messageType: Int
    )

    @GetMapping("/users/{username}/messages")
    fun getMessages(
        @PathVariable username: String
    ): List<MessageDTO> {
        val userInfo = usersRepository.getUserInfo(username)
        val receiverAddress = SignalProtocolAddress(userInfo!!.aci, userInfo.deviceId)
        val allMessages = messagesRepository.readAllMessages(receiverAddress)
        messagesRepository.removeMessages(receiverAddress, allMessages.map { it.messageId }.toSet())
        return allMessages
            .map {
                MessageDTO(
                    it.messageId,
                    usersRepository.getUsername(it.senderAddress)!!,
                    it.messageData,
                    it.messageType
                )
            }
    }

    data class MessageDTO(
        val messageId: UUID,
        val senderUsername: String,
        val messageData: ByteArray,
        val messageType: Int
    )
}
