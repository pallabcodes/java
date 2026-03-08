package com.yourorg.platform.hexagonal.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;

public record CreateSampleRequest(@NotBlank String name) {}
