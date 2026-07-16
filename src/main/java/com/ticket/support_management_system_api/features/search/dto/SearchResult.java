package com.ticket.support_management_system_api.features.search.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResult {
    private SearchResultType type;
    private String id;
    private String title;
    private String subtitle;
    private String meta;
}
