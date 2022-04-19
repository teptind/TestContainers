package com.teptind.common.dto


data class StocksResponseDto(
    val stocksName: String,
    val marketPlace: String,
    val stocksPrice: String,
    val count: Int = 0
)