package com.ticket.support_management_system_api.features.user_type.dto;

import com.ticket.support_management_system_api.features.user_type.enums.EMyTicketAccess;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserTypeRequest {

    @NotBlank(message = "ชื่อประเภทผู้ใช้ต้องไม่ว่าง")
    @Size(max = 100, message = "ชื่อประเภทผู้ใช้ต้องไม่เกิน 100 ตัวอักษร")
    private String name;

    @NotNull(message = "กรุณาเลือกสิทธิ์การเข้าถึง Tickets ของฉัน")
    private EMyTicketAccess myTicketAccess;

    private boolean allProjectAccess;
    private boolean notificationAccess;
    private boolean dashboardAccess;
    private boolean allTicketAccess;
    private boolean manageProjectAccess;
    private boolean manageUserAccess;
    private boolean manageCompanyAccess;
    private boolean manageDataAccess;
}
