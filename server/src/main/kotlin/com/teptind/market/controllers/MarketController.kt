package com.teptind.market.controllers

import com.teptind.market.dto.AddStocksRequestDto
import com.teptind.market.dto.UpdateStocksCountRequestDto
import com.teptind.market.dto.UpdateStocksPriceRequestDto
import com.teptind.market.service.MarketService
import com.teptind.common.dto.BuyStockRequestDto
import com.teptind.common.dto.PaymentResponseDto
import com.teptind.common.dto.SellReportResponseDto
import com.teptind.common.dto.StocksResponseDto
import com.teptind.common.dto.UserSellStockRequestDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/market")
class MarketController(@Autowired private val service: MarketService) {
    @PostMapping("/add")
    fun addStocks(
        @RequestHeader("marketId") marketId: String,
        @RequestBody dto: AddStocksRequestDto
    ): Mono<StocksResponseDto> {
        return service.addStocks(dto)
    }

    @PostMapping("/stock/update/count")
    fun updateStocksCount(
        @RequestHeader("marketId") marketId: String,
        @RequestBody dto: UpdateStocksCountRequestDto
    ): Mono<StocksResponseDto> {
        return service.updateStocksCount(dto)
    }

    @PostMapping("/stock/update/price")
    fun updateStocksCount(
        @RequestHeader("marketId") marketId: String,
        @RequestBody dto: UpdateStocksPriceRequestDto
    ): Mono<StocksResponseDto> {
        return service.updateStocksPrice(dto)
    }

    @GetMapping("/stocks")
    fun allStocks(): Flux<StocksResponseDto> {
        return service.getAllStocks()
    }

    @PostMapping("/stocks/buy")
    fun buyStock(@RequestBody dto: BuyStockRequestDto): Mono<PaymentResponseDto> {
        return service.buyStock(dto)
    }

    @PostMapping("/stocks/sell")
    fun sellStocks(@RequestBody dto: UserSellStockRequestDto): Mono<SellReportResponseDto> {
        return service.sellStocks(dto)
    }
}