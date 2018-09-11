package de.kramhal.callagent

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
open class CallAgent

fun main(args: Array<String>) {
    SpringApplication.run(CallAgent::class.java,*args)
}