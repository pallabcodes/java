package com.netflix.productivity.outbox.controller;

import com.netflix.productivity.outbox.entity.OutboxEvent;
import com.netflix.productivity.outbox.service.OutboxAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/outbox")
@RequiredArgsConstructor
public class OutboxAdminController {

    private final OutboxAdminService service;

    @GetMapping("/dlq")
    public ResponseEntity<List<OutboxEvent>> listDlq() {
        return ResponseEntity.ok(service.listDlq());
    }

    @PostMapping("/dlq/replay")
    public ResponseEntity<String> replayDlq() {
        int count = service.replayDlqBatch();
        return ResponseEntity.ok("Replayed " + count + " DLQ events");
    }
}
