package com.ticket.support_management_system_api.features.ticket.dto;

import com.ticket.support_management_system_api.features.priority.enums.EPriorityColorKey;
import com.ticket.support_management_system_api.features.priority.enums.EPriorityIconKey;
import com.ticket.support_management_system_api.features.status.enums.StatusGroup;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TicketListResponse {
    private UUID id;
    private String ticketId;
    private String title;
    private UUID projectId;
    private String projectName;
    private UUID currentStatusId;
    private String currentStatusName;
    private StatusGroup currentStatusGroup;
    private UUID statusFlowId;
    private String statusFlowName;
    private UUID priorityId;
    private String priorityName;
    private EPriorityIconKey priorityIconShape;
    private EPriorityColorKey priorityIconColor;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private List<TicketAssigneeSummary> assignees;
}
