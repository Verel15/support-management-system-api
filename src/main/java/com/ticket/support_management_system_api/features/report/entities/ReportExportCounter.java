package com.ticket.support_management_system_api.features.report.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report_export_counters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportExportCounter {

    @Id
    @Column(name = "day_key", nullable = false, length = 8)
    private String dayKey;

    @Column(name = "last_seq", nullable = false)
    private Integer lastSeq;
}
