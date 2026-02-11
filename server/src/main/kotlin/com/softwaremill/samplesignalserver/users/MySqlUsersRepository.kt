package com.softwaremill.samplesignalserver.users

import org.signal.libsignal.protocol.ServiceId
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional

open class MySqlUsersRepository(
    private val jdbcTemplate: JdbcTemplate
) : UsersRepository {

    @Transactional
    override fun saveUser(username: String, aci: ServiceId.Aci, deviceId: Int) {
        val sql = """
            INSERT INTO users (username, service_id, device_id)
            VALUES (?, ?, ?)
        """.trimIndent()
        jdbcTemplate.update(sql, username, aci.toServiceIdBinary(), deviceId)
    }

    @Transactional
    override fun getUserInfo(username: String): UsersRepository.User? {
        val sql = """
            SELECT *
            FROM users
            WHERE username = :username
        """.trimIndent()
        val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        return namedParameterJdbcTemplate.query(sql, mapOf("username" to username)) { rs, _ ->
            val username = rs.getString("username")
            val serviceId = ServiceId.Aci.parseFromBinary(rs.getBytes("service_id"))
            val deviceId = rs.getInt("device_id")
            UsersRepository.User(username, serviceId, deviceId)
        }.firstOrNull()
    }

    override fun getUsername(signalProtocolAddress: SignalProtocolAddress): String? {
        val sql = """
            SELECT *
            FROM users
            WHERE service_id = :serviceId AND device_id = :deviceId
        """.trimIndent()
        val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = mapOf(
            "serviceId" to signalProtocolAddress.serviceId.toServiceIdBinary(),
            "deviceId" to signalProtocolAddress.deviceId
        )
        return namedParameterJdbcTemplate.query(sql, parameters) { rs, _ ->
            rs.getString("username")
        }.firstOrNull()

    }
}