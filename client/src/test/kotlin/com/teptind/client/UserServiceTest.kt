package com.teptind.client

import com.teptind.client.domain.User
import com.teptind.client.dto.AddMoneyRequestDto
import com.teptind.client.dto.RegisterUserRequestDto
import com.teptind.common.dto.BaseResponse
import com.teptind.common.exceptions.StocksIllegalRequestException
import com.teptind.common.exceptions.UserIsAlreadyRegisteredException
import com.teptind.common.exceptions.UserNotFoundException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

internal class UserServiceTest: AbstractTestContainersIntegrationTest() {


    @Test
    fun testRegistration() {
        val dto = RegisterUserRequestDto("USER1")
        val response = userController.registerUser(dto)
        Assertions.assertNotNull(response)
        assertEquals("USER1", response.block()?.login)
    }

    @Test
    fun testRegistrationUserAlreadyExist() {
        val dto = RegisterUserRequestDto("USER1")
        userController.registerUser(dto)
        val secondResponse = userController.registerUser(dto)
        Assertions.assertNotNull(secondResponse)
        Assertions.assertThrows(UserIsAlreadyRegisteredException::class.java) { secondResponse.block() }
    }

    @Test
    fun testAddMoneySuccess() {
        userRepository.save(User("USER1", BigDecimal.ZERO, LocalDateTime.now())).subscribe()
        val dto = AddMoneyRequestDto("USER1", BigDecimal(10))
        val response: Mono<BaseResponse> = userController.addMoneyToAccount(dto)
        Assertions.assertNotNull(response)
        assertTrue(response.block()!!.success)
        assertEquals(userRepository.findByLogin("USER1").block()?.balance, BigDecimal(10))
    }

    @Test
    fun testAddMoneyUserNotExist() {
        val dto = AddMoneyRequestDto("USER1", BigDecimal(10))
        val response: Mono<BaseResponse> = userController.addMoneyToAccount(dto)
        Assertions.assertNotNull(response)
        Assertions.assertThrows(UserNotFoundException::class.java) { response.block() }
    }

    @Test
    fun testAddMoneyUserIncorrectSum() {
        val dto = AddMoneyRequestDto("USER1", BigDecimal(-1))
        Assertions.assertThrows(StocksIllegalRequestException::class.java) { userController.addMoneyToAccount(dto) }
    }
}