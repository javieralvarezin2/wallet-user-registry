package es.in2.wallet.user.model.dto

data class AppUserRequestDTO(
    val username: String,
    val email: String,
    val password: String,
)