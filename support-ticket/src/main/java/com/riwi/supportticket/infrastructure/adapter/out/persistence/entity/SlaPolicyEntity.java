package com.riwi.supportticket.infrastructure.adapter.out.persistence.entity;

import com.riwi.supportticket.domain.model.ticket.Priority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sla_policies")
@Getter
@Setter
@NoArgsConstructor
public class SlaPolicyEntity {

    // Una fila por prioridad: la prioridad es la llave
    @Id
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "ticket_priority")
    private Priority priority;

    @Column(name = "resolution_hours", nullable = false)
    private int resolutionHours;
}
