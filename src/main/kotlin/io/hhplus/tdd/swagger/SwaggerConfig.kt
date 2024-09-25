package io.hhplus.tdd.swagger

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Value("\${spring.application.name}")
    private lateinit var projectName: String

    @Bean
    fun openApi(): OpenAPI {

        val local = Server()
        local.url = "http://localhost:8080/"
        local.description = "로컬"

        return OpenAPI().info(
            Info()
                .title("$projectName Rest API Documentation")
                .description("$projectName API 명세")
        )
            .servers(listOf(
                local))
    }

}