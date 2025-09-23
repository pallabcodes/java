package com.example.account;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
    private final AccountRepository repository;

    public AccountController(AccountRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasAuthority('SCOPE_accounts:read')")
    @GetMapping("/accounts/{id}")
    public Account get(@PathVariable String id) {
        return repository.findById(id);
    }
}


