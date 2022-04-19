package com.teptind.client.dto

import com.teptind.common.dto.BuyStockRequestDto

data class BuyStockUserRequestDto(
    val login: String,
    val id: Long,
    val request: BuyStockRequestDto
)