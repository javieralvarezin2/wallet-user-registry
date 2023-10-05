package es.in2.wallet.user.service.impl

import es.in2.wallet.user.exception.EmailAlreadyExistsException
import es.in2.wallet.user.exception.UsernameAlreadyExistsException
import es.in2.wallet.user.model.entity.AppUser
import es.in2.wallet.user.model.repository.AppUserRepository
import es.in2.wallet.user.service.AppUserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AppUserServiceImpl(
        private val appUserRepository: AppUserRepository
) : AppUserService {

    private val log: Logger = LoggerFactory.getLogger(AppUserServiceImpl::class.java)

    override fun checkIfUserExists(username: String): AppUser {
        val userFound = getUserByUsername(username)
        if (userFound.isPresent) {
            return userFound.get()
        } else {
            throw NoSuchElementException("The username $username does not exist.")
        }
    }

    override fun getUserWithContextAuthentication(): AppUser {
        log.info("AppUserServiceImpl.getUserWithContextAuthentication()")
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        return getUserByUsername(authentication.name).get()
    }

    override fun registerUser(appUser: AppUser) {
        log.info("AppUserServiceImpl.registerUser()")
        checkIfUsernameAlreadyExist(appUser)
        checkIfEmailAlreadyExist(appUser)

        log.info(appUser.id.toString())
        saveUser(appUser)
    }

    override fun getUsers(): List<AppUser> {
        log.info("AppUserServiceImpl.getUsers()")
        return listOf(appUserRepository.findAll()).flatten()
    }

    override fun getUserById(uuid: UUID): Optional<AppUser> {
        log.info("AppUserServiceImpl.getUserById()")
        return appUserRepository.findById(uuid)
    }

    override fun getUserByUsername(username: String): Optional<AppUser> {
        log.info("AppUserServiceImpl.getUserByUsername()")
        return appUserRepository.findAppUserByUsername(username)
    }

    override fun getUserByEmail(email: String): Optional<AppUser> {
        log.info("AppUserServiceImpl.getUserByEmail()")
        return appUserRepository.findAppUserByEmail(email)
    }

    private fun saveUser(appUser: AppUser) {
        log.info("AppUserServiceImpl.saveUser()")
        appUserRepository.save(appUser)
    }

    private fun checkIfUsernameAlreadyExist(appUser: AppUser) {
        log.info("AppUserServiceImpl.checkIfUsernameAlreadyExist()")
        if (getUserByUsername(appUser.username).isPresent) {
            throw UsernameAlreadyExistsException("Username already exists: ${appUser.username}")
        }
    }

    private fun checkIfEmailAlreadyExist(appUser: AppUser) {
        log.info("AppUserServiceImpl.checkIfEmailAlreadyExist()")
        if (getUserByEmail(appUser.email).isPresent) {
            throw EmailAlreadyExistsException("Email already exists: ${appUser.email}")
        }
    }

}