package com.teptind.market.dto

data class UpdateStocksCountRequestDto(
    val stocksName: String,
    val count: Int = 0
)