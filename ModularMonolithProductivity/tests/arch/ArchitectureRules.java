package com.netflix.productivity.tests.arch;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

public class ArchitectureRules {
    @Test
    void api_layer_rule() {
        var classes = new ClassFileImporter().importPackages("com.netflix.productivity.modules");
        var rule = ArchRuleDefinition.classes()
            .that().resideInAPackage("..modules..api..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "..modules..application..", "java..", "jakarta..", "org.springframework..", "com.netflix.productivity.platform.."
            );
        rule.check(classes);
    }
}


