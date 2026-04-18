package com.backend.designpatterns.realworld.banking;

// ACL / Adapter Interface
interface AntiCorruptionLayer {
    TransactionReceipt transferFunds(TransferRequest request);
}

// Concrete Adapter Implementation
public class MainframeAdapter implements AntiCorruptionLayer {
    private final CoreBankingSystem mainframe;
    private final BankingMapper mapper;

    public MainframeAdapter() {
        this.mainframe = new CoreBankingSystem();
        this.mapper = new BankingMapper();
    }

    @Override
    public TransactionReceipt transferFunds(TransferRequest request) {
        // 1. Translate Domain -> Legacy
        String xmlPayload = mapper.toXml(request);

        // 2. Call Legacy System
        String xmlResponse = mainframe.process_txn(xmlPayload);

        // 3. Translate Legacy -> Domain
        return mapper.fromXml(xmlResponse);
    }
}
