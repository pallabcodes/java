package com.netflix.productivity.sla;

import com.netflix.productivity.api.ApiResponse;
import com.netflix.productivity.api.ResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issues/{key}/sla")
@Tag(name = "SLA")
public class SlaController {
    private final SlaService sla;
    private final ResponseMapper responses;

    @PostMapping("/due")
    @Operation(summary = "Set due date for an issue")
    public ResponseEntity<ApiResponse<Void>> setDue(@RequestHeader("X-Tenant-ID") String tenantId,
                                                    @PathVariable String key,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime due,
                                                    @RequestHeader(value = "X-User-ID", required = false) String userId) {
        sla.setDueDate(tenantId, key, due, userId == null ? "unknown" : userId);
        return responses.noContent();
    }
}

