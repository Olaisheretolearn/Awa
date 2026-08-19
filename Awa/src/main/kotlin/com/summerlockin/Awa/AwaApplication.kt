package com.summerlockin.Awa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AwaApplication

fun main(args: Array<String>) {
	runApplication<AwaApplication>(*args)
}
