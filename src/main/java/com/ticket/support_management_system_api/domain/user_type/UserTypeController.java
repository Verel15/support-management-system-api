package com.ticket.support_management_system_api.domain.user_type;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.domain.user_type.dto.UserTypeRequest;
import com.ticket.support_management_system_api.domain.user_type.dto.UserTypeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-types")
@RequiredArgsConstructor
public class UserTypeController {

    private final UserTypeService userTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserTypeResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(userTypeService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserTypeResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userTypeService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserTypeResponse>> create(@Valid @RequestBody UserTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างประเภทผู้ใช้สำเร็จ", userTypeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserTypeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตประเภทผู้ใช้สำเร็จ", userTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ลบประเภทผู้ใช้สำเร็จ", null));
    }
}
