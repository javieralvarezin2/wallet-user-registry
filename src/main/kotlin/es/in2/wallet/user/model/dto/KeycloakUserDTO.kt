package es.in2.wallet.user.model.dto

import org.keycloak.representations.idm.CredentialRepresentation

data class KeycloakUserDTO (
    val username: String,
    val email: String,
    val id: String?,
    val credentials : List<CredentialRepresentation>,
    val enabled : Boolean
)