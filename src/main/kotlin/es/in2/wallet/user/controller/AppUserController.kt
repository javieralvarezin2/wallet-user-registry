package es.in2.wallet.user.controller

import es.in2.wallet.user.model.dto.AppUserRequestDTO
import es.in2.wallet.user.model.dto.AppUserResponseDTO
import es.in2.wallet.user.model.mappers.AppUserMapper
import es.in2.wallet.user.service.AppUserService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.websocket.server.PathParam
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Users", description = "Users management API")
@RestController
@RequestMapping("/api/users")
class AppUserController(
        private val appUserService: AppUserService,
        private val mapper: AppUserMapper
) {

    private val log: Logger = LoggerFactory.getLogger(AppUserController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody appUserRequestDTO: AppUserRequestDTO) {
        log.debug("AppUserController.registerUser()")
        appUserService.registerUser(mapper.toEntity(appUserRequestDTO))
    }

    @GetMapping
    fun getAllUsers(): List<AppUserResponseDTO> {
        log.debug("AppUserController.getAllUsers()")
        return appUserService.getUsers().map { mapper.toDto(it) }
    }

    @GetMapping("/uuid")
    fun getUserByUUID(@PathParam("uuid") uuid: String): Optional<AppUserResponseDTO> {
        log.debug("AppUserController.getUserByUUID()")
        return appUserService.getUserById(UUID.fromString(uuid)).map { mapper.toDto(it) }
    }

    @GetMapping("/username")
    fun getUserByUsername(@PathParam("username") username: String): Optional<AppUserResponseDTO> {
        log.debug("AppUserController.getUserByUsername()")
        return appUserService.getUserByUsername(username).map { mapper.toDto(it) }
    }

}