package com.softwaremill.samplesignalserver.devices

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MySqlDevicesRepositoryIntegrationTest(
    @Autowired private val mySqlDevicesRepository: MySqlDevicesRepository
) {

    @Test
    fun `should save and read device`() {
        val device = createRandomDevice()

        mySqlDevicesRepository.saveDevice(device)

        // then
        val readDevice = mySqlDevicesRepository.getDeviceInfo(SignalProtocolAddress(device.serviceId, device.id))

        readDevice.id shouldBe device.id
        readDevice.serviceId shouldBe device.serviceId

        readDevice.signedPreKey.apply {
            id shouldBe device.signedPreKey.id
            signature shouldBe device.signedPreKey.signature
            timestamp shouldBe device.signedPreKey.timestamp
            keyPair.publicKey shouldBe device.signedPreKey.keyPair.publicKey
        }

        readDevice.preKeys.zip(device.preKeys).forEach { (first, second) ->
            first.id shouldBe second.id
            first.keyPair.publicKey shouldBe second.keyPair.publicKey
        }
    }
}