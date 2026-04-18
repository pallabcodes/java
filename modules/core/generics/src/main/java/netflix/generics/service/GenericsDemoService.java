package netflix.generics.service;

import lombok.extern.slf4j.Slf4j;
import netflix.generics.advanced.BoundsExamples;
import netflix.generics.advanced.VarianceExamples;
import netflix.generics.advanced.WildcardExamples;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to orchestrate Java Generics demonstrations.
 */
@Slf4j
@Service
public class GenericsDemoService {

    /**
     * Runs all major generics demonstrations.
     */
    public void runAllGenericsDemonstrations() {
        log.info("Starting Java Generics Demonstrations...");

        // Bounds Examples
        log.info("--- Running Bounds Examples ---");
        List<Integer> integers = new ArrayList<>(List.of(1, 2, 3));
        BoundsExamples.processUpperBound(integers);
        BoundsExamples.processLowerBound(new ArrayList<Number>());
        BoundsExamples.processRecursiveBound(new ArrayList<>(List.of("A", "B", "C")));

        // Wildcard Examples
        log.info("--- Running Wildcard Examples ---");
        WildcardExamples.processUnboundedWildcard(List.of("Hello", 123));
        WildcardExamples.processUpperBoundedWildcard(List.of(1.1, 2.2));
        WildcardExamples.processLowerBoundedWildcard(new ArrayList<Number>());

        // Variance Examples
        log.info("--- Running Variance Examples ---");
        VarianceExamples.demonstrateArrayCovariance();
        VarianceExamples.demonstrateGenericContravariance();
        VarianceExamples.demonstrateGenericInvariance();

        log.info("Java Generics Demonstrations completed successfully.");
    }
}
