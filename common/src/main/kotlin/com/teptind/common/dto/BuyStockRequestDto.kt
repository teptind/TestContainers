package com.teptind.common.dto

import java.math.BigDecimal

data class BuyStockRequestDto(
    val stocksName: String,
    val orderPrice: BigDecimal,
    val count : Int = 0
)