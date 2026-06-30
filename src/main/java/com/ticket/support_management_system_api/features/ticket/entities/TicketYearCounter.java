package com.ticket.support_management_system_api.features.ticket.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_year_counters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketYearCounter {

    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_seq", nullable = false)
    private Integer lastSeq;
}
