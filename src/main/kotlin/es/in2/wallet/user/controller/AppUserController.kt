package es.in2.wallet.user.controller

import es.in2.wallet.user.model.dto.AppUserRequestDTO
import es.in2.wallet.user.service.KeycloakService
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Users", description = "Users management API")
@RestController
@RequestMapping("/api/users")
class AppUserController(
        private val keycloakService: KeycloakService
) {

    private val log: Logger = LoggerFactory.getLogger(AppUserController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody appUserRequestDTO: AppUserRequestDTO) {
        log.debug("AppUserController.registerUser()")
        keycloakService.registerUserInKeycloak(appUserRequestDTO)
        //TODO -- Register data into wallet-data
    }




}