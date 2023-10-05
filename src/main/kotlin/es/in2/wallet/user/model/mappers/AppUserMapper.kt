package es.in2.wallet.user.model.mappers

import es.in2.wallet.user.model.dto.AppUserRequestDTO
import es.in2.wallet.user.model.dto.AppUserResponseDTO
import es.in2.wallet.user.model.entity.AppUser
import es.in2.wallet.user.utils.MappingUtils
import org.mapstruct.Mapper
import org.mapstruct.Mapping


@Mapper(componentModel = "spring")
interface AppUserMapper {
    @Mapping(source = "id", target = "uuid")
    fun toDto(appUser: AppUser): AppUserResponseDTO

    @Mapping(target = "id", expression = MappingUtils.GENERATE_UUID_EXPRESSION)
    @Mapping(target = "password", expression = MappingUtils.GENERATE_ENCRYPTED_PASSWORD)
    fun toEntity(dto: AppUserRequestDTO): AppUser

}

