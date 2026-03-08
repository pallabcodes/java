package com.yourorg.platform.clean.interfaceadapter.in.rest;

import jakarta.validation.constraints.NotBlank;

public record CreateSampleRequest(@NotBlank String name) {}
