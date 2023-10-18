package es.in2.wallet.user.controller

import es.in2.wallet.user.exception.EmailAlreadyExistsException
import es.in2.wallet.user.exception.FailedCommunicationException
import es.in2.wallet.user.exception.UsernameAlreadyExistsException
import es.in2.wallet.user.exception.handler.ApiExceptionHandler
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiExceptionHandlerTest {

    @Test
    fun testHandleEmailAlreadyExistsException() {
        val exception = EmailAlreadyExistsException("Email already exists")
        val response = ApiExceptionHandler().handleEmailAlreadyExistsException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun testHandleUsernameAlreadyExistsException() {
        val exception = UsernameAlreadyExistsException("Username already exists")
        val response = ApiExceptionHandler().handleUsernameAlreadyExistsException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun testFailedCommunicationException() {
        val exception = FailedCommunicationException("Communication failed")
        val response = ApiExceptionHandler().failedCommunicationException(exception)
        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

}