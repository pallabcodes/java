package com.backend.designpatterns.behavioral.template;

import java.util.List;

public class TakeoutDemo {

    public static void main(String[] args) {
        System.out.println("=== L5 Template Method Demo (Google Takeout Engine) ===\n");

        List<Step01_GoogleTakeoutProcessor> jobs = List.of(
            new Step02_GmailTakeoutProcessor(),
            new Step02_GoogleDriveTakeoutProcessor()
        );

        String userAccount = "sundar@google.com";

        for (Step01_GoogleTakeoutProcessor job : jobs) {
            // Because exportData() is `final`, we guarantee the execution order is perfectly identical
            // for every product. This is essential for enterprise security and auditing.
            job.exportData(userAccount);
        }

        System.out.println("\n[L5 ACHIEVEMENT]: The rigid architectural skeleton (Authentication, Compression, Notification) " +
                           "was preserved natively. Product teams only injected Domain Logic via hooks.");
    }
}
