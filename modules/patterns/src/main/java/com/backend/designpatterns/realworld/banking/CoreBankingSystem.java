package com.backend.designpatterns.realworld.banking;

// External Legacy System (Mainframe)
public class CoreBankingSystem {

    public String process_txn(String xml_data) {
        System.out.println("Mainframe: Processing XML -> " + xml_data);
        // Simulate processing
        return "<response><status>00</status><ref>TXN_999</ref></response>"; // 00 = Success
    }
}
