package com.riwi.supportticket.infrastructure.adapter.in.rest.mapper;

import com.riwi.supportticket.application.port.in.ticket.ChangeTicketStatusCommand;
import com.riwi.supportticket.application.port.in.ticket.CreateTicketCommand;
import com.riwi.supportticket.domain.model.ticket.Ticket;
import com.riwi.supportticket.domain.model.ticket.TicketHistory;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.ChangeStatusRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.CreateTicketRequest;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.TicketHistoryResponse;
import com.riwi.supportticket.infrastructure.adapter.in.rest.dto.TicketResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TicketWebMapper {

    CreateTicketCommand toCommand(CreateTicketRequest request);

    ChangeTicketStatusCommand toCommand(ChangeStatusRequest request);

    TicketResponse toResponse(Ticket ticket);

    List<TicketResponse> toResponseList(List<Ticket> tickets);

    TicketHistoryResponse toResponse(TicketHistory history);

    List<TicketHistoryResponse> toHistoryResponseList(List<TicketHistory> history);
}
