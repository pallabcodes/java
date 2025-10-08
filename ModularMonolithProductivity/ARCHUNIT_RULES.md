# ArchUnit Rules Plan

- packages must match `..modules.<module>.(api|application|domain|infrastructure)..`
- api may depend on application only
- application may depend on domain and platform
- domain must not depend on infrastructure or api
- infrastructure depends on domain only
- forbid cycles between modules

Sample rule expression sketch

```
classes().that().resideInAPackage("..modules..api..").should().onlyDependOnClassesThat().resideInAnyPackage("..modules..application..", "java..", "org.springframework..")
```
