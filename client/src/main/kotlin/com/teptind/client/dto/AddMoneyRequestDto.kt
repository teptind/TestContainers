package com.teptind.client.dto

import java.math.BigDecimal

data class AddMoneyRequestDto(
    val login: String,
    val money: BigDecimal
)