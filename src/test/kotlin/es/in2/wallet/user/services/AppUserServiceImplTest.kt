package es.in2.wallet.user.services

import es.in2.wallet.user.exception.EmailAlreadyExistsException
import es.in2.wallet.user.exception.UsernameAlreadyExistsException
import es.in2.wallet.user.model.entity.AppUser
import es.in2.wallet.user.model.repository.AppUserRepository
import es.in2.wallet.user.service.impl.AppUserServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class AppUserServiceImplTest {

    @Mock
    private lateinit var appUserRepository: AppUserRepository

    private lateinit var appUserServiceImpl: AppUserServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        appUserServiceImpl = AppUserServiceImpl(appUserRepository)
    }

    @Test
    fun testRegisterUser() {
        //val appUserRequestDTO = AppUserRequestDTO(username = "jdoe", email = "jdoe@example.com", password = "1234")
        val appUser = AppUser(UUID.randomUUID(), "jdoe", "jdoe@example.com", "hashedPassword")
        `when`(appUserRepository.findAppUserByUsername(appUser.username)).thenReturn(Optional.empty())
        `when`(appUserRepository.findAppUserByEmail(appUser.email)).thenReturn(Optional.empty())
        `when`(appUserRepository.save(any(AppUser::class.java))).thenReturn(appUser)
        appUserServiceImpl.registerUser(appUser)
        verify(appUserRepository).findAppUserByUsername(appUser.username)
        verify(appUserRepository).findAppUserByEmail(appUser.email)
        verify(appUserRepository).save(any(AppUser::class.java))
    }

    @Test
    fun testRegisterUser_UsernameAlreadyExists() {
        //val appUserRequestDTO = AppUserRequestDTO(username = "jdoe", email = "jdoe@example.com", password = "1234")
        val existingUser = AppUser(UUID.randomUUID(), "jdoe", "jdoe@example.com", "hashedPassword")
        `when`(appUserRepository.findAppUserByUsername(existingUser.username)).thenReturn(Optional.of(existingUser))
        try {
            appUserServiceImpl.registerUser(existingUser)
        } catch (e: UsernameAlreadyExistsException) {
            assertThat(e.message).isEqualTo("Username already exists: ${existingUser.username}")
        }
        verify(appUserRepository).findAppUserByUsername(existingUser.username)
        verifyNoMoreInteractions(appUserRepository)
    }

    @Test
    fun testRegisterUser_EmailAlreadyExists() {
        //val appUserRequestDTO = AppUserRequestDTO(username = "jdoe", email = "jdoe@example.com", password = "1234")
        val existingUser = AppUser(UUID.randomUUID(), "jdoe", "jdoe@example.com", "hashedPassword")
        `when`(appUserRepository.findAppUserByUsername(existingUser.username)).thenReturn(Optional.empty())
        `when`(appUserRepository.findAppUserByEmail(existingUser.email)).thenReturn(Optional.of(existingUser))
        try {
            appUserServiceImpl.registerUser(existingUser)
        } catch (e: EmailAlreadyExistsException) {
            assertThat(e.message).isEqualTo("Email already exists: ${existingUser.email}")
        }
        verify(appUserRepository).findAppUserByUsername(existingUser.username)
        verify(appUserRepository).findAppUserByEmail(existingUser.email)
        verifyNoMoreInteractions(appUserRepository)
    }

    @Test
    fun testGetUserById() {
        // Mock the behavior of the appUserRepository
        val userId = UUID.randomUUID()
        val user = AppUser(userId, "user", "user@example.com", "password")
        `when`(appUserRepository.findById(userId)).thenReturn(Optional.of(user))
        // Call the getUserById method
        val result = appUserServiceImpl.getUserById(userId)
        // Verify the result
        Assertions.assertTrue(result.isPresent)
        assertEquals(user, result.get())
    }

}
