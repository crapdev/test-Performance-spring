package com.riwi.supportticket.infrastructure.adapter.out.persistence.mapper;

import com.riwi.supportticket.domain.model.ticket.SlaPolicy;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.SlaPolicyEntity;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.riwi.supportticket.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// El dominio guarda ids (requesterId, technicianId); la entidad guarda relaciones.
// Las relaciones las arma el adaptador con getReferenceById, por eso aquí se ignoran.
@Mapper
public interface TicketPersistenceMapper {

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "technician.id", target = "technicianId")
    Ticket toDomain(TicketEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "technician", ignore = true)
    @Mapping(target = "history", ignore = true)
    void updateEntity(Ticket ticket, @MappingTarget TicketEntity entity);

    @Mapping(source = "ticket.id", target = "ticketId")
    TicketHistory toDomain(TicketHistoryEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    TicketHistoryEntity toEntity(TicketHistory history);

    SlaPolicy toDomain(SlaPolicyEntity entity);
}
