package com.backend.designpatterns.realworld.banking;

// Domain Object
public record TransferRequest(String fromAccount, String toAccount, double amount) {}
