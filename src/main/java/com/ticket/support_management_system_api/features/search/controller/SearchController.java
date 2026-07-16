package com.ticket.support_management_system_api.features.search.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.search.dto.SearchResult;
import com.ticket.support_management_system_api.features.search.dto.SearchResultType;
import com.ticket.support_management_system_api.features.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/v1/search")
    public ResponseEntity<ApiResponse<List<SearchResult>>> search(
            @RequestParam String q,
            @RequestParam List<SearchResultType> types,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(searchService.search(q, types, limit, user)));
    }
}
