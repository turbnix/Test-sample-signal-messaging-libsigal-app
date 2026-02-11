package com.softwaremill.samplesignalserver.devices

import org.signal.libsignal.protocol.SignalProtocolAddress

interface DevicesRepository {
    fun saveDevice(device: Device)
    fun getDeviceInfo(address: SignalProtocolAddress): Device
    fun burnPreKey(address: SignalProtocolAddress, id: Int)
}