package com.softwaremill.samplesignalserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SampleSignalServerApplication

fun main(args: Array<String>) {
    runApplication<SampleSignalServerApplication>(*args)
}
