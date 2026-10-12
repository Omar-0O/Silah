package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class DeviceContact(
    val name: String,
    val phone: String,
    val isGoogle: Boolean = false,
    val photoUri: String? = null
) {
    val first: String get() = name
    val second: String get() = phone
    val third: Boolean get() = isGoogle
}
