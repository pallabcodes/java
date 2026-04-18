package netflix.functional.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import netflix.functional.config.FunctionalConfig;
import netflix.functional.lambda.LambdaExpressionsExamples;
import netflix.functional.streams.StreamApiExamples;
import netflix.functional.optional.OptionalExamples;
import netflix.functional.interfaces.FunctionalInterfacesExamples;
import netflix.functional.async.CompletableFutureExamples;
import org.springframework.stereotype.Service;

/**
 * Functional Demo Service - Orchestrates all functional programming demonstrations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FunctionalDemoService {

    private final FunctionalConfig config;

    /**
     * Runs all functional programming demonstrations based on configuration.
     */
    public void runAllFunctionalDemonstrations() {
        log.info("Starting All Functional Programming Demonstrations...");

        if (config.getExamples().getEnableLambdaExpressions()) {
            LambdaExpressionsExamples.demonstrateBasicLambdas();
            LambdaExpressionsExamples.demonstrateLambdaReturns();
            LambdaExpressionsExamples.demonstrateLambdaWithCollections();
            LambdaExpressionsExamples.demonstrateLambdaWithStreams();
            LambdaExpressionsExamples.demonstrateLambdaWithFunctionalInterfaces();
            LambdaExpressionsExamples.demonstrateLambdaWithMethodReferences();
            LambdaExpressionsExamples.demonstrateLambdaWithExceptionHandling();
            LambdaExpressionsExamples.demonstrateLambdaWithVariableCapture();
            LambdaExpressionsExamples.demonstrateLambdaWithThisAndSuper();
            LambdaExpressionsExamples.demonstrateLambdaWithGenerics();
            LambdaExpressionsExamples.demonstrateLambdaWithVarargs();
            LambdaExpressionsExamples.demonstrateLambdaWithArrays();
            LambdaExpressionsExamples.demonstrateLambdaWithNestedClasses();
            LambdaExpressionsExamples.demonstrateLambdaWithAnonymousClasses();
        }

        if (config.getExamples().getEnableStreams()) {
            StreamApiExamples.demonstrateStreamCreation();
            StreamApiExamples.demonstrateIntermediateOperations();
            StreamApiExamples.demonstrateTerminalOperations();
            StreamApiExamples.demonstrateParallelStreams();
            StreamApiExamples.demonstrateCustomCollectors();
            StreamApiExamples.demonstratePrimitiveStreams();
            StreamApiExamples.demonstrateStreamWithOptional();
            StreamApiExamples.demonstrateGroupingAndPartitioning();
            StreamApiExamples.demonstrateReduction();
            StreamApiExamples.demonstrateMatching();
            StreamApiExamples.demonstrateIteration();
            StreamApiExamples.demonstrateStatistics();
            StreamApiExamples.demonstrateCustomOperations();
        }

        if (config.getExamples().getEnableOptional()) {
            OptionalExamples.demonstrateOptionalCreation();
            OptionalExamples.demonstrateMonadicOperations();
            OptionalExamples.demonstrateOptionalChaining();
            OptionalExamples.demonstrateOptionalWithCollections();
            OptionalExamples.demonstrateOptionalWithExceptionHandling();
            OptionalExamples.demonstrateCustomOperations();
            OptionalExamples.demonstratePerformanceConsiderations();
            OptionalExamples.demonstrateNullSafety();
            OptionalExamples.demonstrateDefaultValues();
            OptionalExamples.demonstrateConditionalOperations();
            OptionalExamples.demonstrateTransformation();
            OptionalExamples.demonstrateValidation();
            OptionalExamples.demonstrateErrorHandling();
            OptionalExamples.demonstrateLoggingAndMonitoring();
            OptionalExamples.demonstrateTesting();
        }

        if (config.getExamples().getEnableFunctionalInterfaces()) {
            FunctionalInterfacesExamples.demonstrateFunction();
            FunctionalInterfacesExamples.demonstrateBiFunction();
            FunctionalInterfacesExamples.demonstratePredicate();
            FunctionalInterfacesExamples.demonstrateBiPredicate();
            FunctionalInterfacesExamples.demonstrateConsumer();
            FunctionalInterfacesExamples.demonstrateBiConsumer();
            FunctionalInterfacesExamples.demonstrateSupplier();
            FunctionalInterfacesExamples.demonstrateUnaryOperator();
            FunctionalInterfacesExamples.demonstrateBinaryOperator();
            FunctionalInterfacesExamples.demonstrateCustomFunctionalInterfaces();
            FunctionalInterfacesExamples.demonstrateHigherOrderFunctions();
        }

        if (config.getExamples().getEnableCompletableFuture()) {
            CompletableFutureExamples.demonstrateCompletableFutureCreation();
            CompletableFutureExamples.demonstrateAsyncComposition();
            CompletableFutureExamples.demonstrateExceptionHandling();
            CompletableFutureExamples.demonstrateParallelExecution();
            CompletableFutureExamples.demonstrateTimeoutAndCancellation();
            CompletableFutureExamples.demonstrateCustomThreadPools();
        }

        log.info("Finished All Functional Programming Demonstrations.");
    }
}
