package com.softwaremill.samplesignalserver.infrastructure

import com.softwaremill.samplesignalserver.devices.DevicesRepository
import com.softwaremill.samplesignalserver.devices.DevicesService
import com.softwaremill.samplesignalserver.devices.MySqlDevicesRepository
import com.softwaremill.samplesignalserver.messages.MessagesRepository
import com.softwaremill.samplesignalserver.messages.MySqlMessagesRepository
import com.softwaremill.samplesignalserver.sessions.DefaultSessionsService
import com.softwaremill.samplesignalserver.users.MySqlUsersRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class BeansConfiguration {

    @Bean
    fun mySqlDevicesRepository(jdbcTemplate: JdbcTemplate): MySqlDevicesRepository {
        return MySqlDevicesRepository(jdbcTemplate)
    }

    @Bean
    fun defaultSessionsService(devicesRepository: DevicesRepository): DefaultSessionsService {
        return DefaultSessionsService(devicesRepository)
    }

    @Bean
    fun mySqlMessageRepository(jdbcTemplate: JdbcTemplate): MessagesRepository {
        return MySqlMessagesRepository(jdbcTemplate)
    }

    @Bean
    fun devicesService(devicesRepository: DevicesRepository): DevicesService {
        return DevicesService(devicesRepository)
    }

    @Bean
    fun usersRepository(jdbcTemplate: JdbcTemplate): MySqlUsersRepository {
        return MySqlUsersRepository(jdbcTemplate)
    }
}