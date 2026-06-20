package com.ticket.support_management_system_api.domain.user;

import java.util.UUID;

import com.ticket.support_management_system_api.domain.company.Company;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class CustomerDetails {
    @Id
    private UUID userId;          

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;  
}
