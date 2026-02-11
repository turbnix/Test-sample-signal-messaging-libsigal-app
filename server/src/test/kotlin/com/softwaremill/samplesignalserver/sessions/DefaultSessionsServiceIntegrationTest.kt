package com.softwaremill.samplesignalserver.sessions

import com.softwaremill.samplesignalserver.devices.DevicesRepository
import com.softwaremill.samplesignalserver.devices.createRandomDevice
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DefaultSessionsServiceIntegrationTest(
    @Autowired val serviceService: DefaultSessionsService,
    @Autowired val devicesRepository: DevicesRepository
) {

    @Test
    fun `should create pre key bundle and delete used key`() {
        // given
        val device = createRandomDevice()
        devicesRepository.saveDevice(device)

        // when
        val address = SignalProtocolAddress(device.serviceId, device.id)
        val preKeyBundle = serviceService.createPreKeyBundle(address)

        // then
        preKeyBundle.deviceId shouldBe device.id

        // verify key is deleted
        devicesRepository.getDeviceInfo(address).preKeys.map { it.id } shouldNotContain preKeyBundle.preKeyId
    }
}