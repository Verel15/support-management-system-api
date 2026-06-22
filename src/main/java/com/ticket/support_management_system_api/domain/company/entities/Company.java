package com.ticket.support_management_system_api.domain.company.entities;

import jakarta.persistence.*;
import lombok.*;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.common.enums.CommonStatus;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {
    
    @Column(name ="name", nullable = false, unique = true)
    private String name;

    @Column(name = "logo_image_url")
    private String logoImageUrl;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CommonStatus status;
    
}
