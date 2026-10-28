package com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper;

import com.riwi.supportticket.domain.model.technician.Technician;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Solo lectura: los técnicos vienen del seed y no se editan
@Mapper
public interface TechnicianPersistenceMapper {

    @Mapping(source = "user.id", target = "userId")
    Technician toDomain(TechnicianEntity entity);
}
