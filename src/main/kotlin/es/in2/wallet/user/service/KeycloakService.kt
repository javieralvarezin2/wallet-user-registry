package es.in2.wallet.user.service

import es.in2.wallet.user.model.dto.AppUserRequestDTO
import es.in2.wallet.user.model.dto.KeycloakUserDTO

interface KeycloakService {
    fun registerUserInKeycloak(appUserRequestDTO: AppUserRequestDTO): String
    fun getKeycloakUserToken(username: String,password: String): String
    fun createUserInKeycloak(userData: KeycloakUserDTO): String?

    fun getKeycloakUsers(token: String): List<KeycloakUserDTO>
    fun getUserIdWithContextAuthentication(): String

    fun getKeycloakIdByUsername(token: String, username: String): String

    fun getKeycloakUserById(token: String, id: String): KeycloakUserDTO?

    fun updateUser(token: String, username: String, userData: KeycloakUserDTO)

    fun deleteKeycloakUser(token: String, username: String)

    fun deleteKeycloakUserById(token: String, id: String)
}
