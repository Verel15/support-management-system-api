package com.ticket.support_management_system_api.features.ticket_type.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketTypeSelectorResponse {

    private UUID id;
    private String name;
    private List<CategoryItem> categories;

    @Data
    @Builder
    public static class CategoryItem {
        private UUID id;
        private String name;
        private UUID statusFlowId;
        private String statusFlowName;
        private List<SubCategoryItem> subCategories;
    }

    @Data
    @Builder
    public static class SubCategoryItem {
        private UUID id;
        private String name;
        private UUID priorityLevelId;
        private String priorityLevelName;
        private UUID positionId;
        private String positionName;
    }
}
