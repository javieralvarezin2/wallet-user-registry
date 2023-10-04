package es.in2.wallet.user.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AppUserRequestDTO(
        @JsonProperty("username")val username: String,
        @JsonProperty("email")val email: String,
        @JsonProperty("password")val password: String,
)