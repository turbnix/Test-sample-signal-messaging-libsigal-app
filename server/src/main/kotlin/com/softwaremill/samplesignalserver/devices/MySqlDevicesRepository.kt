package com.softwaremill.samplesignalserver.devices

import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ServiceId
import org.signal.libsignal.protocol.ServiceId.Aci
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement
import java.sql.ResultSet

open class MySqlDevicesRepository(private val jdbcTemplate: JdbcTemplate) : DevicesRepository {

    @Transactional
    override fun saveDevice(device: Device) {
        insertDevice(device)
        insertPreKeys(device)
    }

    @Transactional
    override fun getDeviceInfo(address: SignalProtocolAddress): Device {
        val sql = """
            SELECT d.device_id, d.service_id, d.public_key, d.signed_pre_key, dk.pre_key
            FROM devices d
            JOIN device_keys dk ON d.device_id = dk.device_id AND d.service_id = dk.service_id
            WHERE d.device_id = :deviceId AND d.service_id = :serviceId
        """.trimIndent()
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameters = mapOf(
            "deviceId" to address.deviceId,
            "serviceId" to address.serviceId?.toServiceIdBinary()
        )
        val rowMapper = DeviceRowMapper()
        namedJdbcTemplate.query(sql, parameters, rowMapper)
        return rowMapper.asDevice()
    }

    override fun burnPreKey(address: SignalProtocolAddress, id: Int) {
        val sql = """
            DELETE FROM device_keys WHERE device_id = :deviceId AND service_id = :serviceId AND id = :preKeyId
        """.trimIndent()
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameters = mapOf(
            "deviceId" to address.deviceId,
            "serviceId" to address.serviceId?.toServiceIdBinary(),
            "preKeyId" to id
        )
        namedJdbcTemplate.update(sql, parameters)
    }

    private class DeviceRowMapper : RowMapper<Device> {
        private var deviceId: Int? = null
        private var serviceId: ServiceId? = null
        private var publicKey: IdentityKey? = null
        private var signedPreKey: SignedPreKeyRecord? = null
        private var preKeys = mutableListOf<PreKeyRecord>()

        override fun mapRow(rs: ResultSet, rowNum: Int): Device? {
            if (deviceId == null) {
                deviceId = rs.getInt("d.device_id")
            }
            if (serviceId == null) {
                serviceId = Aci.parseFromBinary(rs.getBytes("d.service_id"))
            }
            if (publicKey == null) {
                publicKey = IdentityKey(rs.getBytes("d.public_key"))
            }
            if (signedPreKey == null) {
                signedPreKey = SignedPreKeyRecord(rs.getBytes("d.signed_pre_key"))
            }
            preKeys += PreKeyRecord(rs.getBytes("dk.pre_key"))
            return null
        }

        fun asDevice(): Device {
            return Device(
                id = requireNotNull(deviceId),
                serviceId = requireNotNull(serviceId),
                pubKey = requireNotNull(publicKey),
                signedPreKey = requireNotNull(signedPreKey),
                preKeys = preKeys
            )
        }
    }

    private fun insertPreKeys(device: Device) {
        val sql = """
            INSERT INTO device_keys VALUES (?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(
            sql, object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    ps.setInt(1, device.id)
                    ps.setBytes(2, device.serviceId.toServiceIdBinary())
                    val preKey = device.preKeys[i]
                    ps.setInt(3, preKey.id)
                    ps.setBytes(4, preKey.serialize())
                }

                override fun getBatchSize(): Int {
                    return device.preKeys.size
                }
            })
    }

    private fun insertDevice(device: Device) {
        val sql = """
                INSERT INTO devices VALUES
                (:deviceId, :serviceId, :publicKey, :signedPreKey);
            """.trimIndent()

        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        namedJdbcTemplate.update(
            sql, mapOf(
                "deviceId" to device.id,
                "serviceId" to device.serviceId.toServiceIdBinary(),
                "publicKey" to device.pubKey.serialize(),
                "signedPreKey" to device.signedPreKey.serialize()
            )
        )
    }
}