package com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper;

import com.riwi.supportticket.domain.model.user.User;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface UserPersistenceMapper {

    User toDomain(UserEntity entity);

    // Copia los datos del dominio sobre una entidad nueva o ya cargada
    void updateEntity(User user, @MappingTarget UserEntity entity);
}
