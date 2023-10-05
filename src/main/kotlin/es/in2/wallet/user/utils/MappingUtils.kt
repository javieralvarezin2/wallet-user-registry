package es.in2.wallet.user.utils

import es.in2.wallet.user.model.dto.AppUserRequestDTO
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

object MappingUtils {


    const val GENERATE_UUID_EXPRESSION = "java(es.in2.wallet.user.utils.MappingUtils.generateUuid())"
    const val GENERATE_ENCRYPTED_PASSWORD = "java(es.in2.wallet.user.utils.MappingUtils.encryptPassword(dto))"

    @JvmStatic
    fun generateUuid(): UUID {
        return UUID.randomUUID()
    }
    @JvmStatic
    fun encryptPassword(dto: AppUserRequestDTO): String {
        return BCryptPasswordEncoder().encode(dto.password)
    }
}
