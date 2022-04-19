package com.teptind.market.mapper

import com.teptind.common.dto.StocksResponseDto
import com.teptind.market.domain.Stocks

object StocksResponseMapper {
    fun mapToStocksResponse(stocks: Stocks): StocksResponseDto = StocksResponseDto(
        stocks.stocksName,
        stocks.marketPlace,
        stocks.sellPrice.toString(),
        stocks.count
    )
}