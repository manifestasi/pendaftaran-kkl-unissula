package com.manifestasi.mykklunissula.util

sealed class Resource<out T> {
    data class Success<out T>(val data: T?) : Resource<T>()
    data class Error(val exception: Throwable) : Resource<Nothing>()
    data class ErrorMessage(val message: String): Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
