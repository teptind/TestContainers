package com.teptind.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StocksClientApplication

fun main(args: Array<String>) {
    runApplication<StocksClientApplication>(*args)
}