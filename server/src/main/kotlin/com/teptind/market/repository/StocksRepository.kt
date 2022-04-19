package com.teptind.market.repository

import com.teptind.market.domain.Stocks
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface StocksRepository : ReactiveCrudRepository<Stocks, Long> {
    fun findStocksByStocksName(stocksName: String): Mono<Stocks>
}