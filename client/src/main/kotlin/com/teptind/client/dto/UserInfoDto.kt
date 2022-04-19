package com.teptind.client.dto

import java.time.LocalDateTime

data class UserInfoDto(
    val login: String,
    val createTime: LocalDateTime
)