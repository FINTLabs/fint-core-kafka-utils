package no.fintlabs.core.kafka.utils

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FintCoreKafkaUtilsApplication

fun main(args: Array<String>) {
	runApplication<FintCoreKafkaUtilsApplication>(*args)
}
