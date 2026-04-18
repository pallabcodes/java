package com.backend.designpatterns.realworld.banking;

public class BankingDemo {

    public static void main(String[] args) {
        
        // The rest of the application ONLY talks to the ACL interface
        // It knows nothing about XML or the Mainframe
        AntiCorruptionLayer bankingService = new MainframeAdapter();

        TransferRequest request = new TransferRequest("ACC_123", "ACC_456", 500.00);
        
        System.out.println("Initiating Transfer...");
        TransactionReceipt receipt = bankingService.transferFunds(request);
        
        System.out.println("Transfer Result: Success=" + receipt.success() + ", Ref=" + receipt.referenceId());
    }
}
