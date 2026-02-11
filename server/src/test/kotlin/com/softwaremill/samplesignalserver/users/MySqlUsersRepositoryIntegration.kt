package com.softwaremill.samplesignalserver.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.signal.libsignal.protocol.ServiceId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
class MySqlUsersRepositoryIntegration(
    @Autowired private val usersRepository: UsersRepository
) {

    @Test
    @Transactional
    fun shouldSaveAndRetrieveUser() {
        // given
        val aci = ServiceId.Aci(UUID.randomUUID())
        val username = "johnp2"

        // when
        usersRepository.saveUser(username, aci)

        // then
        assertEquals(aci, usersRepository.getUserAci(username))
    }
}