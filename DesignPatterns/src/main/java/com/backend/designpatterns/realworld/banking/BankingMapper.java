package com.backend.designpatterns.realworld.banking;

// Mapper
public class BankingMapper {

    public String toXml(TransferRequest request) {
        return String.format("<transfer><from>%s</from><to>%s</to><amt>%.2f</amt></transfer>", 
                request.fromAccount(), request.toAccount(), request.amount());
    }

    public TransactionReceipt fromXml(String xmlResponse) {
        // Simple parsing simulation
        boolean success = xmlResponse.contains("<status>00</status>");
        String ref = "UNKNOWN";
        if (success) {
            int start = xmlResponse.indexOf("<ref>") + 5;
            int end = xmlResponse.indexOf("</ref>");
            ref = xmlResponse.substring(start, end);
        }
        return new TransactionReceipt(success, ref);
    }
}
