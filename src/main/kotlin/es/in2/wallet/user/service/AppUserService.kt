package es.in2.wallet.user.service

import es.in2.wallet.user.model.entity.AppUser
import java.util.*

interface AppUserService {
    fun getUserWithContextAuthentication(): AppUser
    fun registerUser(appUser: AppUser)
    fun getUsers(): List<AppUser>
    fun getUserById(uuid: UUID): Optional<AppUser>
    fun getUserByUsername(username: String): Optional<AppUser>
    fun getUserByEmail(email: String): Optional<AppUser>
    fun checkIfUserExists(username: String): AppUser
}

