package com.teptind.common.dto

data class BaseResponse(val success: Boolean, val errorDto: ErrorDto? = null)