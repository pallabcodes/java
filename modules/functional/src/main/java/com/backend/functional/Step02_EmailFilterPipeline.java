package com.backend.functional;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Step 02: Advanced Composition
 * 
 * L5 Principles:
 * 1. Composability: Chains small, specialized functions into complex pipelines.
 * 2. Declarative: Describes "what" to do rather than "how" to do it.
 * 3. Reusability: Generic filters can be reused across different pipelines.
 */
public class Step02_EmailFilterPipeline {

    public record EmailMetadata(String sender, String content, int priority) {}

    public static void main(String[] args) {
        System.out.println("=== Step 02: Advanced Composition ===");

        EmailMetadata email = new EmailMetadata("marketing@store.com", "Buy now! Discount inside.", 1);

        // 1. Define atomic predicates (Filters)
        Predicate<EmailMetadata> isSpam = m -> m.content().toLowerCase().contains("buy now");
        Predicate<EmailMetadata> fromInternal = m -> m.sender().endsWith("@google.com");
        Predicate<EmailMetadata> isHighPriority = m -> m.priority() > 5;

        // 2. Compose complex predicates
        Predicate<EmailMetadata> shouldAlert = isHighPriority.or(fromInternal.and(isSpam.negate()));

        System.out.println("Should alert for marketing email? " + shouldAlert.test(email));

        // 3. Define transformations
        Function<EmailMetadata, String> extractSnippet = m -> m.content().substring(0, Math.min(10, m.content().length())) + "...";
        Function<String, String> wrapInLogs = s -> "[GMAIL-LOG] " + s;
        Function<String, String> toUpperCase = String::toUpperCase;

        // 4. Compose transformation pipeline
        Function<EmailMetadata, String> loggingPipeline = extractSnippet
                .andThen(toUpperCase)
                .andThen(wrapInLogs);

        System.out.println("Pipeline Result: " + loggingPipeline.apply(email));
    }
}
