package es.in2.wallet.user.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.user.model.dto.AppUserRequestDTO
import es.in2.wallet.user.model.dto.KeycloakUserDTO
import es.in2.wallet.user.service.KeycloakService
import es.in2.wallet.user.utils.ApplicationUtils
import es.in2.wallet.user.utils.CONTENT_TYPE
import es.in2.wallet.user.utils.CONTENT_TYPE_URL_ENCODED_FORM
import jakarta.transaction.Transactional
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class KeycloakServiceImpl(
        @Value("\${keycloak.url}") private val keycloakUrl : String,
        @Value("\${keycloak.realm}") private val keycloakRealm : String,
        @Value("\${keycloak.admin-username}") private val adminUsername : String,
        @Value("\${keycloak.admin-password}") private val adminPassword : String,
        @Value("\${keycloak.grant-type}") private val grantType : String,
        @Value("\${keycloak.client-secret}") private val clientSecret : String,
        @Value("\${keycloak.client-id}") private val clientId : String,
    private val applicationUtils: ApplicationUtils


): KeycloakService
{
    private val log: Logger = LogManager.getLogger(KeycloakServiceImpl::class.java)

    override fun registerUserInKeycloak(appUserRequestDTO: AppUserRequestDTO): String {
        val credential = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = appUserRequestDTO.password
            isTemporary = false
        }
        val credentials = listOf(credential)
        val user = KeycloakUserDTO(
                username = appUserRequestDTO.username,
                email = appUserRequestDTO.email,
                id = null,
                credentials = credentials,
                enabled = true)
        return createUserInKeycloak(user)!!
    }

    private fun getKeycloakAdminToken(): String {
        val url = "$keycloakUrl/realms/$keycloakRealm/protocol/openid-connect/token"
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val formData = mapOf(
            "grant_type" to grantType,
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "username" to adminUsername,
            "password" to adminPassword
        )
        val body = applicationUtils.buildUrlEncodedFormDataRequestBody(formDataMap = formData)
        val response = applicationUtils.postRequest(url = url, headers = headers, body = body)
        log.info("Access token: $response")
        val jsonObject = ObjectMapper().readValue(response, Map::class.java) as Map<String, Any>
        return jsonObject["access_token"].toString()
    }

    override fun getKeycloakUserToken(username: String,password: String): String {
        val url = "$keycloakUrl/realms/$keycloakRealm/protocol/openid-connect/token"
        val headers = listOf(CONTENT_TYPE to CONTENT_TYPE_URL_ENCODED_FORM)
        val formData = mapOf(
                "grant_type" to grantType,
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "username" to username,
                "password" to password
        )
        val body = applicationUtils.buildUrlEncodedFormDataRequestBody(formDataMap = formData)
        try {
            val response = applicationUtils.postRequest(url = url, headers = headers, body = body)
            log.info("Access token: $response")
            val jsonObject = ObjectMapper().readValue(response, Map::class.java) as Map<String, Any>
            return jsonObject["access_token"].toString()
        }catch (e : Exception){
            throw IllegalArgumentException("Wrong user or password.")
        }
    }


    @Transactional
    override fun createUserInKeycloak(userData: KeycloakUserDTO) : String?{
        val token = getKeycloakAdminToken()
        val keycloak = getKeycloakClient(token = token)

        val user = toUserRepresentation(userData = userData)

        val response = keycloak.realm(keycloakRealm).users().create(user)
        keycloak.close()
        log.info("Response ${response.status}")
        val responseBody = response.readEntity(String::class.java)
        if (response.status < 200 || response.status > 299) {
            throw Exception("Response status ${response.status}, user not created because $responseBody")
        }
        return getKeycloakIdByUsername(token,user.username)

    }
    override fun getUserIdWithContextAuthentication(): String {
        log.info("AppUserServiceImpl.getUserWithContextAuthentication()")
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val token = authentication as JwtAuthenticationToken
        return token.name
    }

    private fun toUserRepresentation(userData: KeycloakUserDTO): UserRepresentation {
        val user = UserRepresentation()

        user.username = userData.username
        user.email = userData.email
        user.isEnabled = userData.enabled
        user.credentials = userData.credentials

        return user
    }

    private fun toKeycloakUser(user: UserRepresentation): KeycloakUserDTO {
        return KeycloakUserDTO(
            username = user.username,
            email = user.email,
            id = user.id,
                credentials = user.credentials,
                enabled = true
        )
    }

    // The service account associated with the client needs to be allowed to view realm users otherwise returns 403 forbidden
    // https://stackoverflow.com/questions/66452108/keycloak-get-users-returns-403-forbidden
    override fun getKeycloakUsers(token: String): List<KeycloakUserDTO> {
        val keycloak = getKeycloakClient(token = token)
        val users: UsersResource? = keycloak.realm(keycloakRealm).users()
        val result = mutableListOf<KeycloakUserDTO>()
        if (users != null){
            val userList = users.list()
            userList.forEach{user ->
                val userData = toKeycloakUser(user = user)
                log.info(userData)
                result.add(userData)
            }
            keycloak.close()
        }
        return result
    }

    override fun getKeycloakIdByUsername(token: String, username: String): String {
        val userResource: UserResource = getUserResource(realmResource = getKeycloakRealm(token=token), username = username)
        val userId = userResource.toRepresentation().id
        log.debug(userId)
        return userId
    }

    override fun getKeycloakUserById(token: String, id: String): KeycloakUserDTO {
        val userResource: UserResource = getUserResourceById(realmResource = getKeycloakRealm(token=token), id = id)
        return toKeycloakUser(user = userResource.toRepresentation())
    }


    override fun updateUser(token: String, username: String, userData: KeycloakUserDTO) {
        val userResource: UserResource = getUserResource(realmResource = getKeycloakRealm(token = token), username = username)
        val userRepresentation: UserRepresentation = userResource.toRepresentation()
        userRepresentation.email = userData.email
        userResource.update(userRepresentation)
    }

    private fun getKeycloakRealm(token: String): RealmResource{
        return getKeycloakClient(token = token).realm(keycloakRealm)
    }

    private fun getUserResourceById(realmResource: RealmResource, id: String): UserResource {
        val usersResource: UsersResource = realmResource.users()
        return usersResource[id]
    }

    private fun getUserResource(realmResource: RealmResource, username: String): UserResource {
        val usersResource: UsersResource = realmResource.users()
        val users = usersResource.search(username)
        if (users != null && users.count() == 1) {
            val user: UserRepresentation = users[0]
            return usersResource[user.id]
        } else {
            throw Exception("User $username not found")
        }
    }

    override fun deleteKeycloakUser(token: String, username: String) {
        val userResource: UserResource = getUserResource(realmResource = getKeycloakRealm(token=token), username = username)
        userResource.remove()
    }

    override fun deleteKeycloakUserById(token: String, id: String) {
        val userResource: UserResource = getUserResourceById(realmResource = getKeycloakRealm(token = token), id = id)
        userResource.remove()
    }

    fun getKeycloakClient(token: String): Keycloak{
        return KeycloakBuilder.builder()
            .serverUrl(keycloakUrl)
            .realm(keycloakRealm)
            .authorization("Bearer $token")
            .build()
    }

}