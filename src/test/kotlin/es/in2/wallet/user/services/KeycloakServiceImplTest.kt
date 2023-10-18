package es.in2.wallet.services

import es.in2.wallet.user.model.dto.KeycloakUserDTO
import es.in2.wallet.user.service.impl.KeycloakServiceImpl
import es.in2.wallet.user.utils.ApplicationUtils
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.CredentialRepresentation
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import javax.ws.rs.core.Response

@SpringBootTest
@ActiveProfiles("test")
class KeycloakServiceImplTest {

    // Mocked utility class.
    private val applicationUtils: ApplicationUtils = mockk(relaxed = true)
    private val mockKeycloak = mockk<Keycloak>()
    private val mockRealm = mockk<RealmResource>()
    private val mockUsers = mockk<UsersResource>()
    private val mockResponse = mockk<Response>()

    // Initializing service under test and mocked dependencies.
    private val serviceSpy = spyk(TestableKeycloakService(applicationUtils))

    // This subclass allows us to override the method getKeycloakClient, so we can use the mocked version.
    private inner class TestableKeycloakService(applicationUtils: ApplicationUtils)
        : KeycloakServiceImpl(
            "https://example.com",
            "example",
            "admin",
            "admin",
            "password",
            "1234",
            "openID",
            applicationUtils
    ) {
        // This method will return the mocked Keycloak client instead of creating a new instance.
        override fun getKeycloakClient(token: String): Keycloak {
            return mockKeycloak
        }
    }


    // This method sets up the environment before each test.
    @BeforeEach
    fun setUp() {
        // Clearing all the mocked behaviors.
        clearAllMocks()

        // Mocking the private method.
        every { serviceSpy["getKeycloakAdminToken"]() } returns "mockedToken"


        // Setting up mocked behaviors.
        every { mockKeycloak.realm(any()) } returns mockRealm
        every { mockRealm.users() } returns mockUsers
        every { mockUsers.create(any()) } returns mockResponse
        every { mockResponse.status } returns 201
        every { mockKeycloak.close() } just Runs
        every { mockResponse.readEntity(String::class.java) } returns "entity response"
    }

    // Test to verify user creation in Keycloak.
    @Test
    fun testCreateUser() {
        // Test data.
        val token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJRcXlubjlWcH"
        val response = "{\"access_token\":\"$token\",\"expires_in\":300,\"refresh_expires_in\":1799,\"refresh_token\":\"eyJhbGciOiJIUzI1N\",\"token_type\":\"Bearer\",\"not-before-policy\":0,\"session_state\":\"9381cfbf-446c-4165-9ff8-0fbc7e68ffc9\",\"scope\":\"profile email\"}"

        val credential = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = "abc123"
            isTemporary = false
        }
        val credentials = listOf(credential)

        val userData = KeycloakUserDTO(
                username = "test",
                email = "test-email@test.test",
                id = null,
                credentials = credentials,
                enabled = true
        )
        // Mocked behavior for utility methods.
        every {
            applicationUtils.buildUrlEncodedFormDataRequestBody(any())
        } returns "example"
        every {
            applicationUtils.postRequest(any(), any(), any())
        } returns response
        every {
            serviceSpy.getKeycloakIdByUsername("mockedToken", userData.username)
        } returns "uuid12345"
        // Calling the method under test.
        val userId = serviceSpy.createUserInKeycloak(userData = userData)
        assertEquals(userId, "uuid12345")

        // Verifying that certain methods were called.
        verify { mockUsers.create(any()) }
    }

    @Test
    fun testGetUserToken() {
        // Test data.
        val adminToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJRcXlubjlWcH"
        val adminResponse = "{\"access_token\":\"$adminToken\",\"expires_in\":300,\"refresh_expires_in\":1799,\"refresh_token\":\"eyJhbGciOiJIUzI1N\",\"token_type\":\"Bearer\",\"not-before-policy\":0,\"session_state\":\"9381cfbf-446c-4165-9ff8-0fbc7e68ffc9\",\"scope\":\"profile email\"}"
        val expectedUserToken =
                "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJRcXlubjlWcHFyU2F3Wk5yTWp3Sk1jVHNtRGI5R1JVRUNXVGNoS2lIWi1NIn0.eyJleHAiOjE2OTYzMzg0MDEsImlhdCI6MTY5NjMzODEwMSwianRpIjoiZjAzMjMzMWItMDc3Ni00Mjk5LWIzYmYtNDExNGQ0Y2RiYmE5IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDg0L3JlYWxtcy9FQUFQcm92aWRlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIzODM1MTIxMS1kYTIwLTRlODAtODA0OS1hMGFkZTcwNzA0YTgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvaWRjNHZjaS1jbGllbnQiLCJzZXNzaW9uX3N0YXRlIjoiNzI5MGZiNTAtZTk1Yy00ZDI2LWEzZDktMzA5YzI1ZWU2NjE1IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjQyMDEvKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJkZWZhdWx0LXJvbGVzLWVhYXByb3ZpZGVyIiwidW1hX2F1dGhvcml6YXRpb24iLCJ1c2VyIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwic2lkIjoiNzI5MGZiNTAtZTk1Yy00ZDI2LWEzZDktMzA5YzI1ZWU2NjE1IiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJtYW51ZWwiLCJlbWFpbCI6Im1hbnVlbEBnbWFpbC5jb20ifQ.OCqxU8ZTIfQMRjTGPSK1L3xFDFDlMugX7ZzqAVO2qxJWxEJ7z9foJZnhs6PJf7BIxqXzMgiHKvz6xFSzD8NTMxat1-Kkv5f4xtcanHLpPrtU3R8Z9BNasinUA1ItoYTJCmPQhfqucfuOBRWXRRCn9At-ApOakWDL0y8eVRRD_YBfX4rO31Y4rm7DQAMD6C4e7bQ9kaCgC2VtEtQtYljV9PdfIh3654tgNCOJhkAX1GDP-I3xOxTmNkAKX4PIv86IOcWrGPcpY9U5Mh1EA2OQdU1sqkbQiJqtBVR2N80fu_uOZQ1a-9g2LLM0rpc2iJAzaFrnrrHzXMFbKAw-63NAhw"
        val userResponse = "{\"access_token\":\"$expectedUserToken\",\"expires_in\":300,\"refresh_expires_in\":1799,\"refresh_token\":\"eyJhbGciOiJIUzI1N\",\"token_type\":\"Bearer\",\"not-before-policy\":0,\"session_state\":\"9381cfbf-446c-4165-9ff8-0fbc7e68ffc9\",\"scope\":\"profile email\"}"

        val credential = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = "abc123"
            isTemporary = false
        }
        val credentials = listOf(credential)

        val userData = KeycloakUserDTO(
                username = "newUser",
                email = "user-email@test.test",
                id = null,
                credentials = credentials,
                enabled = true
        )

        // Setting up mocked behaviors based on different conditions.
        every {
            applicationUtils.buildUrlEncodedFormDataRequestBody(any())
        } answers {
            val formDataMap = this.firstArg<Map<String, String?>>()
            if (formDataMap["username"] == "newUser") {
                return@answers "username=newUser"
            } else {
                return@answers "username=admin"
            }
        }

        every {
            applicationUtils.postRequest(any(), any(), match { it.contains("username=newUser") })
        } returns userResponse

        every {
            applicationUtils.postRequest(any(), any(), match { it.contains("username=admin") })
        } returns adminResponse

        every {
            serviceSpy.getKeycloakIdByUsername("mockedToken", userData.username)
        } returns "uuid12345"
        // Method call under test.
        serviceSpy.createUserInKeycloak(userData)
        verify { mockUsers.create(any()) }

        // Additional checks.
        val token = serviceSpy.getKeycloakUserToken("newUser", "abc123")
        assertTrue(token.length > 512)
        assertEquals(token, expectedUserToken)
    }

}