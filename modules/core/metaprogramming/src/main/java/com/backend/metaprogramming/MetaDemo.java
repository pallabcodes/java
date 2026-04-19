package com.backend.metaprogramming;

/**
 * L7 Metaprogramming Mastery Demo (Google -> Netflix Prep)
 */
public class MetaDemo {

    public static void main(String[] args) throws Throwable {
        System.out.println("\n--- STARTING L7 METAPROGRAMMING MASTERY DEMO ---\n");

        Step01_ReflectionDeepDive.main(null);
        System.out.println();

        Step02_DynamicProxies.main(null);
        System.out.println();

        Step03_MethodHandles.main(null);
        System.out.println();

        Step04_TypeTokens.main(null);
        System.out.println();

        Step05_AnnotationMastery.main(null);

        System.out.println("\n--- L7 METAPROGRAMMING MASTERY DEMO COMPLETE ---");
    }
}
