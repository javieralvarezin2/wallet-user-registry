package es.in2.wallet.user.controller

import es.in2.wallet.user.model.dto.AppUserResponseDTO
import es.in2.wallet.user.model.entity.AppUser
import es.in2.wallet.user.model.mappers.AppUserMapper
import es.in2.wallet.user.service.AppUserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
class AppUserControllerTest {

    @Mock
    private lateinit var appUserService: AppUserService

    @Mock
    private lateinit var mapper: AppUserMapper

    @InjectMocks
    private lateinit var appUserController: AppUserController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(AppUserControllerTest::class.java)
        mockMvc = MockMvcBuilders.standaloneSetup(appUserController).build()
    }

    // Data Test

    private val uuid = UUID.randomUUID()
    private val uuid2 = UUID.randomUUID()
    private val appUser = AppUser(
            id = uuid,
            username = "jdoe",
            email = "jdoe@example.com",
            password = BCryptPasswordEncoder().encode("1234")
    )
    private val appUser2 = AppUser(
            id = uuid2,
            username = "janeDoe",
            email = "janedoe@example.com",
            password = BCryptPasswordEncoder().encode("1234")
    )
    private val userResponseDTO = AppUserResponseDTO(
            uuid = uuid.toString(),
            username = "jdoe",
            email = "jdoe@example.com"

    )

    // @Post /api/users

    @Test
    fun testRegisterUser() {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"jdoe\",\"email\":\"jdoe@example.com\",\"password\":\"1234\"}")
        ).andExpect(status().isCreated)
    }


    // @Get /api/users/{id}

    @Test
    fun testGetUserByUUID() {
        given(appUserService.getUserById(uuid)).willReturn(Optional.of(appUser))
        given(mapper.toDto(appUser)).willReturn(userResponseDTO)

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/$uuid"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.username").value("jdoe"))
                .andExpect(jsonPath("$.email").value("jdoe@example.com"))
    }

}