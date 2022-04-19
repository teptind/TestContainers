package com.teptind.client.service

import com.teptind.client.domain.User
import com.teptind.client.domain.UserStocks
import com.teptind.client.repository.UserRepository
import com.teptind.client.repository.UserStocksRepository
import com.teptind.common.dto.BaseResponse
import com.teptind.common.dto.StocksResponseDto
import com.teptind.common.dto.UserSellStockRequestDto
import com.teptind.client.dto.AddMoneyRequestDto
import com.teptind.client.dto.BuyStockUserRequestDto
import com.teptind.client.dto.RegisterUserRequestDto
import com.teptind.client.dto.UserInfoDto
import com.teptind.client.dto.UserStocksResponseDto
import com.teptind.common.exceptions.NotEnoughMoneyException
import com.teptind.common.exceptions.NotEnoughStocksException
import com.teptind.common.exceptions.StocksIllegalRequestException
import com.teptind.common.exceptions.UserIsAlreadyRegisteredException
import com.teptind.common.exceptions.UserNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime


@Service
class UserService(
    @Autowired
    private val userRepository: UserRepository,
    @Autowired
    private val userStocksRepository: UserStocksRepository,
    @Autowired
    private val marketCleint: StocksMarketApiService
) {

    fun registerUser(dto: RegisterUserRequestDto): Mono<UserInfoDto> {
        return try {
            userRepository.save<User>(User(dto.login, BigDecimal.ZERO, LocalDateTime.now())).map { user ->
                UserInfoDto(user.login, user.registered)
            }
        } catch (e: Exception) {
            throw UserIsAlreadyRegisteredException("User with provided login ${dto.login} already exist")
        }
    }

    fun addMoney(dto: AddMoneyRequestDto): Mono<BaseResponse> {
        if (dto.money.signum() <= 0) {
            throw StocksIllegalRequestException("Replenishment amount can't be zero or negative")
        }
        return userRepository.findByLogin(dto.login)
            .switchIfEmpty(
                Mono.error(
                    UserNotFoundException("User with login ${dto.login} not exist. Please check your request")
                )
            )
            .doOnNext { user ->
                userRepository.save(user.copy(balance = user.balance.add(dto.money))).subscribe()
            }
            .map { BaseResponse(true, null) }
    }

    fun getUserByLogin(login: String): Mono<User> {
        return userRepository.findByLogin(login)
    }

    @Transactional
    fun buyStocks(dto: BuyStockUserRequestDto): Mono<UserStocks> {
        return userRepository.findByLogin(dto.login)
            .switchIfEmpty(
                Mono.error(UserNotFoundException("User with login ${dto.login} not exist. Please check your request"))
            )
            .flatMap { userAcc ->
                val totalPrice: BigDecimal =
                    dto.request.orderPrice.multiply(BigDecimal(dto.request.count))
                if (userAcc.balance < totalPrice) {
                    return@flatMap Mono.error(NotEnoughMoneyException("Not enough money to perform operation"))
                }
                userRepository.save(userAcc.copy(balance = userAcc.balance.subtract(totalPrice))).subscribe()
                Mono.just(userAcc)
            }.flatMap { marketCleint.buyStocks(dto.request) }
            .flatMap { paymentResponse ->
                userStocksRepository
                    .findUserStocksByLoginAndStocksName(dto.login, dto.request.stocksName)
                    .map { UserStocks(paymentResponse.stockId, dto.id, dto.request.count, LocalDateTime.now()) }
                    .map { stocks -> stocks.also { userStocksRepository.save(it).subscribe() } }
            }
    }

    fun getUserStocks(uid: Long): Flux<UserStocksResponseDto> {
        return userStocksRepository.findUserStocks(uid)
            .map { userStocks ->
                UserStocksResponseDto(
                    userStocks.stock,
                    userStocks.marketPlace,
                    userStocks.sellPrice,
                    userStocks.count
                )
            }
    }

    fun getAvailableStocks(): Flux<StocksResponseDto> = marketCleint.getAllStocks()

    @Transactional
    fun sellStocks(dto: UserSellStockRequestDto): Mono<BaseResponse> {
        return userRepository.findByLogin(dto.login)
            .switchIfEmpty(
                Mono.error(
                    UserNotFoundException("User with login ${dto.login} not exist. Please check your request")
                )
            )
            .flatMap { user ->
                userStocksRepository.findUserStocksByLoginAndStocksName(dto.login, dto.stocksName)
                    .switchIfEmpty(Mono.error(StocksIllegalRequestException("Illegal request")))
                    .flatMap { userStocks ->
                        if (userStocks.count < dto.count) {
                            Mono.error(NotEnoughStocksException("Not enough stocks"))
                        } else {
                            userStocks.count = userStocks.count - dto.count
                            userStocks.updated = LocalDateTime.now()
                            userStocksRepository.save(userStocks).subscribe()
                            marketCleint.sellStocks(dto)
                                .flatMap { sellReportResponseDto ->
                                    val newUser = user.copy(
                                        balance = user.balance.add(
                                            sellReportResponseDto.soldPrice.multiply(BigDecimal(sellReportResponseDto.soldCount))
                                        )
                                    )
                                    userRepository.save(newUser).block()
                                    Mono.just(BaseResponse(true, null))
                                }
                            Mono.just(BaseResponse(true, null))
                        }
                    }
            }
    }
}