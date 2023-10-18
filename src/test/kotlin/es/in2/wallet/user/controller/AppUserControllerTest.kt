package es.in2.wallet.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import es.in2.wallet.user.model.dto.AppUserRequestDTO
import es.in2.wallet.user.service.KeycloakService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@SpringJUnitConfig
@SpringBootTest
@ActiveProfiles("test")
class AppUserControllerTest {

    private lateinit var mockMvc: MockMvc

    @InjectMocks
    private lateinit var appUserController: AppUserController

    @Mock
    private lateinit var keycloakService: KeycloakService

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appUserController).build()
        println("keycloakService mock: $keycloakService")

    }

    @Test
    fun testRegisterUser() {
        val appUserRequestDTO = AppUserRequestDTO("username", "email", "password")
        val requestJson = ObjectMapper().writeValueAsString(appUserRequestDTO)

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        )
                .andExpect(status().isCreated)
                .andReturn()
    }
}