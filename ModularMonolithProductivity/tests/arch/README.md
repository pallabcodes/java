# ArchUnit Tests

This directory contains guidance and skeletons for architecture tests to enforce module boundaries and layering.

## Scope

* Packages must follow `..modules.<module>.(api|application|domain|infrastructure)..`
* Api may depend on application only
* Application may depend on domain and platform
* Domain must not depend on infrastructure or api
* Infrastructure depends on domain only
* No cycles between modules

## Example sketch

```java
// package tests.arch;
// import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
//
// class ArchitectureRules {
//     @org.junit.jupiter.api.Test
//     void api_layer_rule() {
//         var classes = new ClassFileImporter().importPackages("com.netflix.productivity.modules");
//         var rule = ArchRuleDefinition.classes()
//             .that().resideInAPackage("..modules..api..")
//             .should().onlyDependOnClassesThat().resideInAnyPackage(
//                 "..modules..application..", "java..", "org.springframework.."
//             );
//         rule.check(classes);
//     }
// }
```
